// package cn.oyzh.easyshell.event.redis.connect;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.redis.RedisClient;
// import cn.oyzh.event.Event;
//
// /**
//  * @author oyzh
//  * @since 2023/9/18
//  */
// public class RedisConnectOpenedEvent extends Event<RedisDatabaseTreeItem>  {
//
//     public RedisClient client() {
//         return this.data().client();
//     }
//
//     public ShellConnect redisConnect() {
//         return this.data().client().redisConnect();
//     }
// }
