// package cn.oyzh.easyshell.event.redis;
//
// import cn.oyzh.easyshell.trees.redis.RedisZSetKeyTreeItem;
// import cn.oyzh.event.Event;
// import cn.oyzh.event.EventFormatter;
// import cn.oyzh.i18n.I18nHelper;
//
// /**
//  * @author oyzh
//  * @since 2023/11/20
//  */
// public class ShellRedisZSetMemberAddedEvent extends Event<RedisZSetKeyTreeItem> implements EventFormatter {
//
//     private String key;
//
//     public String getKey() {
//         return key;
//     }
//
//     public void setKey(String key) {
//         this.key = key;
//     }
//
//     public Double getScore() {
//         return score;
//     }
//
//     public void setScore(Double score) {
//         this.score = score;
//     }
//
//     public String getMember() {
//         return member;
//     }
//
//     public void setMember(String member) {
//         this.member = member;
//     }
//
//     private Double score;
//
//     private String member;
//
//     @Override
//     public String eventFormat() {
//         return String.format(
//                 "[%s] " + I18nHelper.key() + ":%s(db%s) " + I18nHelper.memberAdded() + ":%s " + I18nHelper.score() + ":%s",
//                 this.data().infoName(), this.key, this.data().dbIndex(), this.member, this.score
//         );
//     }
// }
