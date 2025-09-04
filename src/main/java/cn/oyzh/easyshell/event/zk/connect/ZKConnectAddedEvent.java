// package cn.oyzh.easyshell.event.zk.connect;
//
// import cn.oyzh.easyshell.domain.zk.ZKConnect;
// import cn.oyzh.event.Event;
// import cn.oyzh.event.EventFormatter;
// import cn.oyzh.i18n.I18nHelper;
//
// /**
//  * @author oyzh
//  * @since 2023/9/18
//  */
// public class ZKConnectAddedEvent extends Event<ZKConnect> implements EventFormatter {
//
//     @Override
//     public String eventFormat() {
//         return String.format("[%s:%s] ", I18nHelper.connect(), this.data().getName());
//     }
// }
