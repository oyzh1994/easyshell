package cn.oyzh.easyshell.store.redis;//package cn.oyzh.easyredis.store;
//
//import cn.oyzh.easyshell.domain.redis.RedisSSHConfig;
//import cn.oyzh.store.jdbc.DeleteParam;
//import cn.oyzh.store.jdbc.JdbcStandardStore;
//import cn.oyzh.store.jdbc.QueryParam;
//
///**
// * @author oyzh
// * @since 2024/09/26
// */
//public class RedisSSHConfigStore extends JdbcStandardStore<RedisSSHConfig> {
//
//    /**
//     * 当前实例
//     */
//    public static final RedisSSHConfigStore INSTANCE = new RedisSSHConfigStore();
//
//    public boolean replace(RedisSSHConfig model) {
//        String iid = model.getIid();
//        if (super.exist(iid)) {
//            return super.update(model);
//        }
//        return this.insert(model);
//    }
//
//    @Override
//    protected Class<RedisSSHConfig> modelClass() {
//        return RedisSSHConfig.class;
//    }
//
//    public void deleteByIid(String iid) {
//        DeleteParam param = new DeleteParam();
//        param.addQueryParam(QueryParam.of("iid", iid));
//        super.delete(param);
//    }
//
//    public RedisSSHConfig getByIid(String iid) {
//        return super.selectOne(QueryParam.of("iid", iid));
//    }
//}
