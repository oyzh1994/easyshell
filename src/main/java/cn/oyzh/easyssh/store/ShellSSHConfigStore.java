package cn.oyzh.easyssh.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.domain.ShellSSHConfig;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

/**
 * shell ssh配置存储
 *
 * @author oyzh
 * @since 2025/03/15
 */
public class ShellSSHConfigStore extends JdbcStandardStore<ShellSSHConfig> {

    /**
     * 当前实例
     */
    public static final ShellSSHConfigStore INSTANCE = new ShellSSHConfigStore();

    public boolean replace(ShellSSHConfig model) {
        String iid = model.getIid();
        if (super.exist(iid)) {
            return super.update(model);
        }
        return this.insert(model);
    }

    @Override
    protected Class<ShellSSHConfig> modelClass() {
        return ShellSSHConfig.class;
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
    public ShellSSHConfig getByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        return super.selectOne(QueryParam.of("iid", iid));
    }
}
