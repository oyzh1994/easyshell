// package cn.oyzh.easyshell.store.redis;
//
// import cn.oyzh.common.log.JulLog;
// import cn.oyzh.easyshell.domain.redis.RedisSetting;
// import cn.oyzh.store.jdbc.JdbcKeyValueStore;
//
//
// /**
//  * redis设置储存
//  *
//  * @author oyzh
//  * @since 2024/12/22
//  */
// public class RedisSettingStore extends JdbcKeyValueStore<RedisSetting> {
//
//     /**
//      * 当前实例
//      */
//     public static final RedisSettingStore INSTANCE = new RedisSettingStore();
//
//     /**
//      * 当前设置
//      */
//     public static final RedisSetting SETTING = INSTANCE.load();
//
//     public RedisSetting load() {
//         RedisSetting setting = null;
//         try {
//             setting = super.select();
//         } catch (Exception ex) {
//             ex.printStackTrace();
//             JulLog.warn("load setting error", ex);
//         }
//         if (setting == null) {
//             setting = new RedisSetting();
//         }
//         return setting;
//     }
//
//     public boolean replace(RedisSetting model) {
//         if (model != null) {
//             return this.update(model);
//         }
//         return false;
//     }
//
//     @Override
//     protected Class<RedisSetting> modelClass() {
//         return RedisSetting.class;
//     }
// }
