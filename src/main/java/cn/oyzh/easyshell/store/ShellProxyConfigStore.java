package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理配置存储
 *
 * @author oyzh
 * @since 2025/04/14
 */
public class ShellProxyConfigStore extends JdbcStandardStore<ShellProxyConfig> {

    /**
     * 当前实例
     */
    public static final ShellProxyConfigStore INSTANCE = new ShellProxyConfigStore();

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellProxyConfig model) {
        // 根据id判断
        String id = model.getId();
        if (StringUtil.isNotBlank(id) || super.exist(id)) {
            return super.update(model);
        }
        // 根据iid判断
        String iid = model.getIid();
        if (StringUtil.isNotBlank(iid)) {
            Map<String, Object> params = new HashMap<>();
            params.put("iid", iid);
            if (super.exist(params)) {
                return super.update(model);
            }
        }
        return this.insert(model);
    }

    /**
     * 根据shell连接id获取配置
     *
     * @param iid shell连接id
     * @return sasl配置
     */
    public ShellProxyConfig getByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        return super.selectOne(QueryParam.of("iid", iid));
    }

    /**
     * 根据iid删除
     *
     * @param iid shell连接id
     * @return 结果
     */
    public boolean deleteByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return false;
        }
        DeleteParam param = new DeleteParam();
        param.addQueryParam(new QueryParam("iid", iid));
        return super.delete(param);
    }

    @Override
    protected Class<ShellProxyConfig> modelClass() {
        return ShellProxyConfig.class;
    }
}
