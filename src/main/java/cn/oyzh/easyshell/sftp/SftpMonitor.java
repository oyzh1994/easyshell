package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.thread.ThreadUtil;
import com.jcraft.jsch.SftpProgressMonitor;

/**
 * @author oyzh
 * @since 2025-03-21
 */
public abstract class SftpMonitor implements SftpProgressMonitor {

    protected long total;

    protected long current;

    protected long startTime;

    protected transient boolean ended;

    protected transient boolean cancelled;

    public boolean isEnded() {
        return ended;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public long getTotal() {
        return total;
    }

    public long getCurrent() {
        return current;
    }

    @Override
    public void init(int current, String s, String s1, long total) {
        this.total = total;
        this.current = current;
        this.startTime = System.currentTimeMillis();
    }

    public abstract void cancel();

    public synchronized boolean isFinished() {
        return this.ended || this.cancelled;
    }
}
