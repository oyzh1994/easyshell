package cn.oyzh.easyshell.ssh;

/**
 * ssh连接状态
 *
 * @author oyzh
 * @since 2023/07/1
 */
public enum ShellSSHConnState {

    /**
     * 未初始化
     */
    NOT_INITIALIZED {
        public boolean isConnected() {
            return false;
        }
    },
    /**
     * 已连接
     */
    CONNECTED {
        public boolean isConnected() {
            return true;
        }
    },
    /**
     * 连接中
     */
    CONNECTING {
        public boolean isConnected() {
            return false;
        }
    },
    /**
     * 已关闭
     */
    CLOSED {
        public boolean isConnected() {
            return false;
        }
    },
    /**
     * 失败
     */
    FAILED {
        public boolean isConnected() {
            return false;
        }
    },
    /**
     * 中断
     */
    INTERRUPT {
        public boolean isConnected() {
            return false;
        }
    };

    public abstract boolean isConnected();
}
