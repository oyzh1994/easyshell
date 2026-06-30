// package cn.oyzh.easyshell.store;
//
// import cn.oyzh.easyshell.domain.ShellSync;
// import cn.oyzh.store.jdbc.JdbcKeyValueStore;
//
//
// /**
//  * shell 同步存储
//  *
//  * @author oyzh
//  * @since 2025/10/11
//  */
// public class ShellSyncStore extends JdbcKeyValueStore<ShellSync> {
//
//     /**
//      * 当前实例
//      */
//     public static final ShellSyncStore INSTANCE = new ShellSyncStore();
//
//     /**
//      * 当前设置
//      */
//     public static final ShellSync SYNC = INSTANCE.load();
//
//     /**
//      * 加载
//      *
//      * @return shell同步
//      */
//     public ShellSync load() {
//         ShellSync sync = null;
//         try {
//             sync = super.select();
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         if (sync == null) {
//             sync = new ShellSync();
//         }
//         return sync;
//     }
//
//     /**
//      * 替换
//      *
//      * @param model 模型
//      * @return 结果
//      */
//     public boolean replace(ShellSync model) {
//         if (model != null) {
//             return this.update(model);
//         }
//         return false;
//     }
//
//     @Override
//     protected Class<ShellSync> modelClass() {
//         return ShellSync.class;
//     }
// }
