// package cn.oyzh.easyshell.zk;
//
// import org.apache.curator.framework.state.ConnectionState;
//
// /**
//  * zk连接状态
//  *
//  * @author oyzh
//  * @since 2023/6/1
//  */
// public enum ZKConnState {
//     /**
//      * 已连接
//      */
//     CONNECTED {
//         public boolean isConnected() {
//             return true;
//         }
//     },
//     /**
//      * 连接中
//      */
//     CONNECTING {
//         public boolean isConnected() {
//             return false;
//         }
//     },
//     /**
//      * 已中断
//      */
//     SUSPENDED {
//         public boolean isConnected() {
//             return false;
//         }
//     },
//     /**
//      * 已重连
//      */
//     RECONNECTED {
//         public boolean isConnected() {
//             return true;
//         }
//     },
//     /**
//      * 连接丢失
//      */
//     LOST {
//         public boolean isConnected() {
//             return false;
//         }
//     },
//     /**
//      * 只读模式
//      */
//     READ_ONLY {
//         public boolean isConnected() {
//             return true;
//         }
//     },
//     /**
//      * 已关闭
//      */
//     CLOSED {
//         public boolean isConnected() {
//             return false;
//         }
//     },
//     /**
//      * 连接失败
//      */
//     FAILED {
//         public boolean isConnected() {
//             return false;
//         }
//     };
//
//     /**
//      * 是否已连接
//      *
//      * @return 结果
//      */
//     public abstract boolean isConnected();
//
//     /**
//      * 从ConnectionState创建对象
//      *
//      * @param state 连接状态
//      * @return zk连接状态
//      */
//     public static ZKConnState valueOf(ConnectionState state) {
//         if (state == null) {
//             return ZKConnState.CLOSED;
//         }
//         return switch (state) {
//             case LOST -> ZKConnState.LOST;
//             case CONNECTED -> ZKConnState.CONNECTED;
//             case READ_ONLY -> ZKConnState.READ_ONLY;
//             case SUSPENDED -> ZKConnState.SUSPENDED;
//             case RECONNECTED -> ZKConnState.RECONNECTED;
//         };
//     }
// }
