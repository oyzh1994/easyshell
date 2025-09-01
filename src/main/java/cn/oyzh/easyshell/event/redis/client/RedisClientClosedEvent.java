package cn.oyzh.easyshell.event.redis.client;// package cn.oyzh.easyredis.event;
//
// import cn.oyzh.easyshell.domain.redis.RedisInfo;
// import cn.oyzh.easyshell.redis.RedisClient;
// import cn.oyzh.event.Event;
// import cn.oyzh.event.EventFormatter;
//
// /**
//  * @author oyzh
//  * @since 2023/11/28
//  */
// public class RedisClientClosedEvent extends Event<RedisClient> implements EventFormatter {
//
//     @Override
//     public String eventFormat() {
//         return String.format("[%s] 客户端已断开", this.data().infoName());
//     }
//
//     public RedisInfo info() {
//         return this.data().redisInfo();
//     }
// }
