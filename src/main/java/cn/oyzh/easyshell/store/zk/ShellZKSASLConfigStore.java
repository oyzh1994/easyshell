package cn.oyzh.easyshell.store.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ShellZKSASLConfig;
import cn.oyzh.store.jdbc.sqlite.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.QueryParam;

/**
 * zk sasl配置存储
 *
 * @author oyzh
 * @since 2024/09/26
 */
public class ShellZKSASLConfigStore extends JdbcStandardStore<ShellZKSASLConfig> {

    /**
     * 当前实例
     */
    public static final ShellZKSASLConfigStore INSTANCE = new ShellZKSASLConfigStore();

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellZKSASLConfig model) {
        if (super.exist(model.getId())) {
            return super.update(model);
        }
        return this.insert(model);
    }

    @Override
    protected Class<ShellZKSASLConfig> modelClass() {
        return ShellZKSASLConfig.class;
    }

    /**
     * 根据zk连接id获取配置
     *
     * @param iid zk连接id
     * @return sasl配置
     */
    public ShellZKSASLConfig getByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        return super.selectOne(QueryParam.of("iid", iid));
    }

    /**
     * 根据zk连接id删除配置
     *
     * @param iid zk连接id
     * @return 结果
     */
    public boolean deleteByIid(String iid) {
        DeleteParam param = new DeleteParam();
        param.addQueryParam(QueryParam.of("iid", iid));
        return super.delete(param);
    }
}
