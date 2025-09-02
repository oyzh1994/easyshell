// package cn.oyzh.easyshell.event.redis.connection;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.redis.RedisClient;
// import cn.oyzh.event.Event;
// import cn.oyzh.event.EventFormatter;
// import cn.oyzh.i18n.I18nHelper;
//
// /**
//  * @author oyzh
//  * @since 2023/11/28
//  */
// public class RedisConnectionClosedEvent extends Event<RedisClient> implements EventFormatter {
//
//     @Override
//     public String eventFormat() {
//         return String.format("[%s] " + I18nHelper.connectionDisconnected(), this.data().connectName());
//     }
//
//     public ShellConnect shellConnect() {
//         return this.data().shellConnect();
//     }
// }
