package cn.oyzh.easyshell.internal;

import org.apache.curator.framework.state.ConnectionState;

/**
 * shell连接状态
 *
 * @author oyzh
 * @since 2023/07/1
 */
public enum ShellConnState {

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
    },
    /**
     * 重连
     */
    RECONNECTED {
        public boolean isConnected() {
            return true;
        }
    };

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public abstract boolean isConnected();

    /**
     * 从zk状态获取
     *
     * @param state zk状态
     * @return ShellConnState
     */
    public static ShellConnState valueOfZK(ConnectionState state) {
        return switch (state) {
            case ConnectionState.CONNECTED, ConnectionState.READ_ONLY -> CONNECTED;
            case ConnectionState.RECONNECTED -> RECONNECTED;
            case ConnectionState.LOST -> CLOSED;
            case ConnectionState.SUSPENDED -> INTERRUPT;
        };
    }
}
