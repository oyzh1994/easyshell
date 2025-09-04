// package cn.oyzh.easyshell.event.redis.query;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.domain.redis.ShellRedisQuery;
// import cn.oyzh.easyshell.redis.ShellRedisClient;
// import cn.oyzh.event.Event;
//
// /**
//  * @author oyzh
//  * @since 2024-11-18
//  */
// public class RedisOpenQueryEvent extends Event<ShellRedisQuery> {
//
//     private ShellRedisClient client;
//
//     public ShellRedisClient getClient() {
//         return client;
//     }
//
//     public void setClient(ShellRedisClient client) {
//         this.client = client;
//     }
//
//     public ShellConnect redisConnect() {
//         return this.client.shellConnect();
//     }
// }
