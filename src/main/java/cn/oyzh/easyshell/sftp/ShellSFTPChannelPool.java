package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.Pool;
import cn.oyzh.easyshell.exception.ShellException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * sftp通道管理器
 *
 * @author oyzh
 * @since 2025-06-07
 */
public class ShellSFTPChannelPool extends Pool<ShellSFTPChannel> implements AutoCloseable {

    private static final long DEFAULT_BORROW_TIMEOUT_MILLIS = 15_000L;

    /**
     * 客户端
     */
    private volatile ShellSFTPClient client;

    private final Set<ShellSFTPChannel> channels = ConcurrentHashMap.newKeySet();

    private final Set<ShellSFTPChannel> borrowed = ConcurrentHashMap.newKeySet();

    private final Deque<ShellSFTPChannel> idle = new ArrayDeque<>();

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition available = lock.newCondition();

    /**
     * 已创建且尚未销毁的通道数量
     */
    private int created;

    private volatile boolean closed;

    public ShellSFTPChannelPool(ShellSFTPClient client) {
        super(1, 5);
        this.client = client;
        super.setWaitingBorrow(true);
    }

    @Override
    public void returnObject(ShellSFTPChannel channel) {
        if (channel == null) {
            return;
        }
        if (!this.borrowed.remove(channel)) {
            if (!this.closed) {
                JulLog.warn("sftp channel is returned more than once: {}", channel);
            }
            return;
        }
        if (this.closed || !channel.isConnected() || channel.isClosed()) {
            this.discard(channel);
            return;
        }
        ShellSFTPChannel toClose = null;
        this.lock.lock();
        try {
            if (this.closed) {
                this.discardLocked(channel);
                toClose = channel;
                return;
            }
            this.idle.addLast(channel);
            this.available.signal();
        } finally {
            this.lock.unlock();
            this.closeQuietly(toClose);
        }
    }

    @Override
    protected ShellSFTPChannel newObject() {
        return this.client == null ? null : this.client.newSFTPChannel();
    }

    @Override
    public ShellSFTPChannel borrowObject() {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(this.borrowTimeoutMillis());
        while (true) {
            ShellSFTPChannel channel;
            ShellSFTPChannel toClose = null;
            boolean create = false;
            this.lock.lock();
            try {
                while (true) {
                    if (this.closed) {
                        return null;
                    }
                    channel = this.idle.pollFirst();
                    if (channel != null) {
                        if (channel.isConnected() && !channel.isClosed()) {
                            this.borrowed.add(channel);
                            return channel;
                        }
                        this.discardLocked(channel);
                        toClose = channel;
                        break;
                    }
                    if (this.created < this.getMaxSize()) {
                        this.created++;
                        create = true;
                        break;
                    }
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0) {
                        JulLog.warn("sftp channel borrow timeout");
                        return null;
                    }
                    this.available.awaitNanos(remaining);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                this.lock.unlock();
                this.closeQuietly(toClose);
            }
            if (toClose != null) {
                continue;
            }
            if (create) {
                return this.createChannel();
            }
        }
    }

    @Override
    public void close() {
        List<ShellSFTPChannel> toClose;
        this.lock.lock();
        try {
            if (this.closed) {
                return;
            }
            this.closed = true;
            toClose = new ArrayList<>(this.channels);
            this.channels.clear();
            this.borrowed.clear();
            this.idle.clear();
            this.created = 0;
            this.available.signalAll();
        } finally {
            this.lock.unlock();
        }
        for (ShellSFTPChannel channel : toClose) {
            this.closeQuietly(channel);
        }
        super.clear();
        this.client = null;
    }

    @Override
    public int size() {
        this.lock.lock();
        try {
            return this.idle.size();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        this.lock.lock();
        try {
            return this.idle.isEmpty();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean isFull() {
        this.lock.lock();
        try {
            return this.created >= this.getMaxSize();
        } finally {
            this.lock.unlock();
        }
    }

    private ShellSFTPChannel createChannel() {
        ShellSFTPClient currentClient = this.client;
        if (currentClient == null) {
            this.creationFailed(null);
            return null;
        }
        ShellSFTPChannel channel = null;
        try {
            channel = currentClient.newSFTPChannel();
            if (channel == null) {
                this.creationFailed(null);
                return null;
            }
            this.lock.lock();
            try {
                if (this.closed) {
                    this.decrementCreated();
                    this.available.signal();
                } else {
                    this.channels.add(channel);
                    this.borrowed.add(channel);
                    return channel;
                }
            } finally {
                this.lock.unlock();
            }
            channel.close();
            return null;
        } catch (RuntimeException ex) {
            if (channel != null) {
                this.channels.remove(channel);
                try {
                    channel.close();
                } catch (Exception closeEx) {
                    ex.addSuppressed(closeEx);
                }
            }
            this.creationFailed(ex);
            throw ex instanceof ShellException ? ex : new ShellException(ex);
        }
    }

    private void creationFailed(Throwable cause) {
        this.lock.lock();
        try {
            this.decrementCreated();
            this.available.signal();
        } finally {
            this.lock.unlock();
        }
        if (!this.closed) {
            JulLog.warn("create sftp channel failed", cause);
        }
    }

    private void discardLocked(ShellSFTPChannel channel) {
        this.borrowed.remove(channel);
        if (this.channels.remove(channel)) {
            this.decrementCreated();
        }
        this.available.signal();
    }

    private void decrementCreated() {
        if (this.created > 0) {
            this.created--;
        }
    }

    private void discard(ShellSFTPChannel channel) {
        boolean detached = false;
        this.lock.lock();
        try {
            if (this.channels.contains(channel)) {
                this.discardLocked(channel);
                detached = true;
            }
        } finally {
            this.lock.unlock();
        }
        if (detached) {
            this.closeQuietly(channel);
        }
    }

    private long borrowTimeoutMillis() {
        ShellSFTPClient currentClient = this.client;
        int timeout = currentClient == null ? 0 : currentClient.connectTimeout();
        return timeout > 0 ? timeout : DEFAULT_BORROW_TIMEOUT_MILLIS;
    }

    private void closeQuietly(ShellSFTPChannel channel) {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (Exception ex) {
            JulLog.warn("close sftp channel failed", ex);
        }
    }
}
