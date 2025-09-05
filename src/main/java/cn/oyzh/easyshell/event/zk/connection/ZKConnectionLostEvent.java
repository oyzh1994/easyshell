// package cn.oyzh.easyshell.event.zk.connection;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.zk.ZKClient;
// import cn.oyzh.event.Event;
// import cn.oyzh.event.EventFormatter;
// import cn.oyzh.i18n.I18nHelper;
//
// /**
//  * @author oyzh
//  * @since 2023/9/19
//  */
// public class ZKConnectionLostEvent extends Event<ZKClient> implements EventFormatter {
//
//     @Override
//     public String eventFormat() {
//         return String.format("[%s:%s lost] ", I18nHelper.connect(), this.data().connectName());
//     }
//
//     public ShellConnect connect() {
//         return this.data().zkConnect();
//     }
// }
