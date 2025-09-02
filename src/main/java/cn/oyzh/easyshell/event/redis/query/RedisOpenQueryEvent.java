// package cn.oyzh.easyshell.event.redis.query;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.domain.redis.RedisQuery;
// import cn.oyzh.easyshell.redis.RedisClient;
// import cn.oyzh.event.Event;
//
// /**
//  * @author oyzh
//  * @since 2024-11-18
//  */
// public class RedisOpenQueryEvent extends Event<RedisQuery> {
//
//     private RedisClient client;
//
//     public RedisClient getClient() {
//         return client;
//     }
//
//     public void setClient(RedisClient client) {
//         this.client = client;
//     }
//
//     public ShellConnect redisConnect() {
//         return this.client.shellConnect();
//     }
// }
