package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.store.jdbc.sqlite.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.QueryParam;

/**
 * x11配置存储
 *
 * @author oyzh
 * @since 2025/03/08
 */
public class ShellX11ConfigStore extends JdbcStandardStore<ShellX11Config> {

    /**
     * 当前实例
     */
    public static final ShellX11ConfigStore INSTANCE = new ShellX11ConfigStore();

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellX11Config model) {
        String iid = model.getIid();
        if (super.exist(iid)) {
            return super.update(model);
        }
        return this.insert(model);
    }

    /**
     * 根据shell连接id获取配置
     *
     * @param iid shell连接id
     * @return sasl配置
     */
    public ShellX11Config getByIid(String iid) {
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
    protected Class<ShellX11Config> modelClass() {
        return ShellX11Config.class;
    }
}
