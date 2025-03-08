package cn.oyzh.easyssh.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.domain.SSHX11Config;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

/**
 * x11配置存储
 *
 * @author oyzh
 * @since 2025/03/08
 */
public class SSHX11ConfigStore extends JdbcStandardStore<SSHX11Config> {

    /**
     * 当前实例
     */
    public static final SSHX11ConfigStore INSTANCE = new SSHX11ConfigStore();

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(SSHX11Config model) {
        String iid = model.getIid();
        if (super.exist(iid)) {
            return super.update(model);
        }
        return this.insert(model);
    }

    /**
     * 根据zk连接id获取配置
     *
     * @param iid zk连接id
     * @return sasl配置
     */
    public SSHX11Config getByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        return super.selectOne(QueryParam.of("iid", iid));
    }

    /**
     * 根据iid删除
     *
     * @param iid zk连接id
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
    protected Class<SSHX11Config> modelClass() {
        return SSHX11Config.class;
    }
}
