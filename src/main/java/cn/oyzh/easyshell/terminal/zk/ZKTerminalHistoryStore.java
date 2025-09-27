// package cn.oyzh.easyshell.terminal.zk;
//
// import cn.oyzh.store.jdbc.JdbcStandardStore;
//
// /**
//  * @author oyzh
//  * @since 2024-11-25
//  */
// public class ZKTerminalHistoryStore extends JdbcStandardStore<ZKTerminalHistory> {
//
//     /**
//      * 当前实例
//      */
//     public static final ZKTerminalHistoryStore INSTANCE = new ZKTerminalHistoryStore();
//
//     public boolean replace(ZKTerminalHistory model) {
//         return this.insert(model);
//     }
//
//     @Override
//     protected Class<ZKTerminalHistory> modelClass() {
//         return ZKTerminalHistory.class;
//     }
// }
