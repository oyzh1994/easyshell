package cn.oyzh.easyshell.file;

import cn.oyzh.common.thread.ThreadUtil;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class ShellFileCompetitor {

    private transient Object obj;

    public boolean tryLock(Object obj) {
        while (this.obj != null) {
            ThreadUtil.sleep(5);
        }
        synchronized (this) {
            this.obj = obj;
        }
        return true;
    }

    public synchronized boolean release(Object obj) {
        if (obj == this.obj) {
            synchronized (this) {
                this.obj = null;
            }
            return true;
        }
        return false;
    }
}
