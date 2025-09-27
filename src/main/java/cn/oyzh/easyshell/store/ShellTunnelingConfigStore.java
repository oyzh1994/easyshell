package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.store.jdbc.sqlite.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.QueryParam;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-16
 */
public class ShellTunnelingConfigStore extends JdbcStandardStore<ShellTunnelingConfig> {

    /**
     * 当前实例
     */
    public static final ShellTunnelingConfigStore INSTANCE = new ShellTunnelingConfigStore();

    public boolean replace(List<ShellTunnelingConfig> models) {
        try {
            for (ShellTunnelingConfig model : models) {
                this.replace(model);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean replace(ShellTunnelingConfig model) {
        if (super.exist(model.getId())) {
            return super.update(model);
        }
        return this.insert(model);
    }

    @Override
    protected Class<ShellTunnelingConfig> modelClass() {
        return ShellTunnelingConfig.class;
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

    /**
     * 根据shell连接id获取配置
     *
     * @param iid shell连接id
     * @return ssh配置
     */
    public List<ShellTunnelingConfig> loadByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        return super.selectList(QueryParam.of("iid", iid));
    }
}
