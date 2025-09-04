package cn.oyzh.easyshell.store.zk;//package cn.oyzh.easyzk.store;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.domain.zk.ZKProxyConfig;
//import cn.oyzh.store.jdbc.DeleteParam;
//import cn.oyzh.store.jdbc.JdbcStandardStore;
//import cn.oyzh.store.jdbc.QueryParam;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * zk代理配置存储
// *
// * @author oyzh
// * @since 2025/05/19
// */
//public class ZKProxyConfigStore extends JdbcStandardStore<ZKProxyConfig> {
//
//    /**
//     * 当前实例
//     */
//    public static final ZKProxyConfigStore INSTANCE = new ZKProxyConfigStore();
//
//    /**
//     * 替换
//     *
//     * @param model 模型
//     * @return 结果
//     */
//    public boolean replace(ZKProxyConfig model) {
//        // 根据id判断
//        String id = model.getId();
//        if (StringUtil.isNotBlank(id) || super.exist(id)) {
//            return super.update(model);
//        }
//        // 根据iid判断
//        String iid = model.getIid();
//        if (StringUtil.isNotBlank(iid)) {
//            Map<String, Object> params = new HashMap<>();
//            params.put("iid", iid);
//            if (super.exist(params)) {
//                return super.update(model);
//            }
//        }
//        return this.insert(model);
//    }
//
//    /**
//     * 根据shell连接id获取配置
//     *
//     * @param iid shell连接id
//     * @return sasl配置
//     */
//    public ZKProxyConfig getByIid(String iid) {
//        if (StringUtil.isEmpty(iid)) {
//            return null;
//        }
//        return super.selectOne(QueryParam.of("iid", iid));
//    }
//
//    /**
//     * 根据iid删除
//     *
//     * @param iid shell连接id
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
//    @Override
//    protected Class<ZKProxyConfig> modelClass() {
//        return ZKProxyConfig.class;
//    }
//}
