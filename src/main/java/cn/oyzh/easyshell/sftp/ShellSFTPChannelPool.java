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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * sftp通道管理器
 *
 * @author oyzh
 * @since 2025-06-07
 */
public class ShellSFTPChannelPool extends Pool<ShellSFTPChannel> implements AutoCloseable {

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
        this.lock.lock();
        try {
            if (this.closed) {
                this.discardLocked(channel);
                return;
            }
            this.idle.addLast(channel);
            this.available.signal();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    protected ShellSFTPChannel newObject() {
        return this.client == null ? null : this.client.newSFTPChannel();
    }

    @Override
    public ShellSFTPChannel borrowObject() {
        while (true) {
            ShellSFTPChannel channel;
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
                        continue;
                    }
                    if (this.created < this.getMaxSize()) {
                        this.created++;
                        create = true;
                        break;
                    }
                    this.available.await();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                this.lock.unlock();
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
            try {
                channel.close();
            } catch (Exception ex) {
                JulLog.warn("close sftp channel failed", ex);
            }
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
        this.channels.remove(channel);
        this.decrementCreated();
        this.available.signal();
        channel.close();
    }

    private void decrementCreated() {
        if (this.created > 0) {
            this.created--;
        }
    }

    private void discard(ShellSFTPChannel channel) {
        this.lock.lock();
        try {
            this.discardLocked(channel);
        } finally {
            this.lock.unlock();
        }
    }
}
