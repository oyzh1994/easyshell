// package cn.oyzh.easyshell.store.redis;
//
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.easyshell.domain.redis.RedisJumpConfig;
// import cn.oyzh.ssh.domain.SSHConnect;
// import cn.oyzh.store.jdbc.DeleteParam;
// import cn.oyzh.store.jdbc.JdbcStandardStore;
// import cn.oyzh.store.jdbc.QueryParam;
//
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
//
// /**
//  * redis跳板配置存储
//  *
//  * @author oyzh
//  * @since 2025/05/20
//  */
// public class RedisJumpConfigStore extends JdbcStandardStore<RedisJumpConfig> {
//
//     /**
//      * 当前实例
//      */
//     public static final RedisJumpConfigStore INSTANCE = new RedisJumpConfigStore();
//
//     public boolean replace(List<RedisJumpConfig> models) {
//         try {
//             for (RedisJumpConfig model : models) {
//                 this.replace(model);
//             }
//             return true;
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return false;
//     }
//
//     public boolean replace(RedisJumpConfig model) {
//         if (super.exist(model.getId())) {
//             return super.update(model);
//         }
//         return this.insert(model);
//     }
//
//     @Override
//     protected Class<RedisJumpConfig> modelClass() {
//         return RedisJumpConfig.class;
//     }
//
//     /**
//      * 根据iid删除
//      *
//      * @param iid shell连接id
//      * @return 结果
//      */
//     public boolean deleteByIid(String iid) {
//         if (StringUtil.isEmpty(iid)) {
//             return false;
//         }
//         DeleteParam param = new DeleteParam();
//         param.addQueryParam(new QueryParam("iid", iid));
//         return super.delete(param);
//     }
//
//     /**
//      * 根据shell连接id获取配置
//      *
//      * @param iid shell连接id
//      * @return ssh跳板配置
//      */
//     public List<RedisJumpConfig> loadByIid(String iid) {
//         if (StringUtil.isEmpty(iid)) {
//             return null;
//         }
//         List<RedisJumpConfig> configs = super.selectList(QueryParam.of("iid", iid));
//         // 过滤历史原因造成的无效配置
//         List<RedisJumpConfig> results = new ArrayList<>();
//         for (RedisJumpConfig config : configs) {
//             if (StringUtil.isNotBlank(config.getUser(), config.getHost())) {
//                 results.add(config);
//             }
//         }
//         // 执行排序
//         results.sort(Comparator.comparingInt(SSHConnect::getOrder));
//         return results;
//     }
// }
