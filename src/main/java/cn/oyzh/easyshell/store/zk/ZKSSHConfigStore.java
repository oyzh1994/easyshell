package cn.oyzh.easyshell.store.zk;//package cn.oyzh.easyzk.store;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.domain.zk.ZKSSHConfig;
//import cn.oyzh.store.jdbc.DeleteParam;
//import cn.oyzh.store.jdbc.JdbcStandardStore;
//import cn.oyzh.store.jdbc.QueryParam;
//
///**
// * zk ssh配置存储
// *
// * @author oyzh
// * @since 2024/09/26
// */
//public class ZKSSHConfigStore extends JdbcStandardStore<ZKSSHConfig> {
//
//    /**
//     * 当前实例
//     */
//    public static final ZKSSHConfigStore INSTANCE = new ZKSSHConfigStore();
//
//    public boolean replace(ZKSSHConfig model) {
//        String iid = model.getIid();
//        if (super.exist(iid)) {
//            return super.update(model);
//        }
//        return this.insert(model);
//    }
//
//    @Override
//    protected Class<ZKSSHConfig> modelClass() {
//        return ZKSSHConfig.class;
//    }
//
//    /**
//     * 根据iid删除
//     *
//     * @param iid zk连接id
//     * @return 结果
//     */
//    public boolean deleteByIid(String iid) {
//        if (StringUtil.isEmpty(iid)) {
//            return false;
//        }
//        DeleteParam param = new DeleteParam();
//        param.addQueryParam(new QueryParam("iid", iid));
//        return super.delete(param);
//    }
//
//    /**
//     * 根据zk连接id获取配置
//     *
//     * @param iid zk连接id
//     * @return ssh配置
//     */
//    public ZKSSHConfig getByIid(String iid) {
//        if (StringUtil.isEmpty(iid)) {
//            return null;
//        }
//        return super.selectOne(QueryParam.of("iid", iid));
//    }
//}
