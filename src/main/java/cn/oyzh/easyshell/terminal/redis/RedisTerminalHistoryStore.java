// package cn.oyzh.easyshell.terminal.redis;
//
// import cn.oyzh.easyshell.terminal.redis.RedisTerminalHistory;
// import cn.oyzh.store.jdbc.JdbcStandardStore;
//
// /**
//  * @author oyzh
//  * @since 2024-11-25
//  */
// public class RedisTerminalHistoryStore extends JdbcStandardStore<RedisTerminalHistory> {
//
//     /**
//      * 当前实例
//      */
//     public static final RedisTerminalHistoryStore INSTANCE = new RedisTerminalHistoryStore();
//
//     public boolean replace(RedisTerminalHistory model) {
//         return this.insert(model);
//     }
//
//     @Override
//     protected RedisTerminalHistory newModel() {
//         return new RedisTerminalHistory();
//     }
//
//     @Override
//     protected Class<RedisTerminalHistory> modelClass() {
//         return RedisTerminalHistory.class;
//     }
// }
