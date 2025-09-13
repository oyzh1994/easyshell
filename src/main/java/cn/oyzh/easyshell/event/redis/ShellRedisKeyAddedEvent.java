// package cn.oyzh.easyshell.event.redis;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.event.Event;
// import cn.oyzh.event.EventFormatter;
// import cn.oyzh.i18n.I18nHelper;
//
// /**
//  * @author oyzh
//  * @since 2023/11/20
//  */
// public class ShellRedisKeyAddedEvent extends Event<ShellConnect> implements EventFormatter {
//     public String getType() {
//         return type;
//     }
//
//     public void setType(String type) {
//         this.type = type;
//     }
//
//     public String getKey() {
//         return key;
//     }
//
//     public void setKey(String key) {
//         this.key = key;
//     }
//
//     public int getDbIndex() {
//         return dbIndex;
//     }
//
//     public void setDbIndex(int dbIndex) {
//         this.dbIndex = dbIndex;
//     }
//
//     private String type;
//
//     private String key;
//
//     private int dbIndex;
//
//     @Override
//     public String eventFormat() {
//         return String.format(
//                 "[%s] " + I18nHelper.addKey() + "[%s-db%s] " + I18nHelper.keyType() + ":[%s] ",
//                 this.data().getName(), this.dbIndex, this.key, this.type
//         );
//     }
// }
