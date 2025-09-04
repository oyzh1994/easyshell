package cn.oyzh.easyshell.store.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.redis.ShellRedisQuery;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.List;

/**
 * zk查询存储
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class RedisQueryStore extends JdbcStandardStore<ShellRedisQuery> {

    /**
     * 当前实例
     */
    public static final RedisQueryStore INSTANCE = new RedisQueryStore();

    /**
     * 根据zk连接id加载列表
     *
     * @param iid zk连接id
     * @return 收藏列表
     */
    public List<ShellRedisQuery> list(String iid) {
        QueryParam param = new QueryParam();
        param.setName("iid");
        param.setData(iid);
        return super.selectList(param);
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellRedisQuery model) {
        if (model != null) {
            if (!this.exist(model.getUid())) {
                return this.insert(model);
            }
            return this.update(model);
        }
        return false;
    }

    /**
     * 根据zk连接id删除查询
     *
     * @param iid zk连接id
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean deleteByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            DeleteParam param = new DeleteParam();
            param.addQueryParam(QueryParam.of("iid", iid));
            return this.delete(param);
        }
        return false;
    }

    @Override
    protected Class<ShellRedisQuery> modelClass() {
        return ShellRedisQuery.class;
    }
}
