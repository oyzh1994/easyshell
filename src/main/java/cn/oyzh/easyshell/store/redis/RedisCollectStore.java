package cn.oyzh.easyshell.store.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.redis.ShellRedisCollect;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * redis收藏存储
 *
 * @author oyzh
 * @since 2024/09/26
 */
public class RedisCollectStore extends JdbcStandardStore<ShellRedisCollect> {

    /**
     * 当前实例
     */
    public static final RedisCollectStore INSTANCE = new RedisCollectStore();

    public List<ShellRedisCollect> loadByIid(String iid) {
        QueryParam param = new QueryParam();
        param.setName("iid");
        param.setData(iid);
        return super.selectList(param);
//        List<ShellRedisCollect> collects = super.selectList(param);
//        if (CollectionUtil.isNotEmpty(collects)) {
//            return collects.parallelStream().map(ShellRedisCollect::getKey).collect(Collectors.toList());
//        }
//        return Collections.emptyList();
    }

    public boolean replace(String iid, int dbIndex, String key) {
        return this.replace(new ShellRedisCollect(iid, dbIndex, key));
    }

    public boolean replace(ShellRedisCollect model) {
        if (model != null && !this.exist(model.getIid(), model.getDbIndex(), model.getKey())) {
            return this.insert(model);
        }
        return false;
    }

    // /**
    //  * 删除
    //  *
    //  * @param iid 连接id
    //  * @return 结果
    //  */
    // public boolean delete(String iid) {
    //     if (StringUtil.isNotBlank(iid)) {
    //         DeleteParam param = new DeleteParam();
    //         param.addQueryParam(QueryParam.of("iid", iid));
    //         return this.delete(param);
    //     }
    //     return false;
    // }

    /**
     * 删除
     *
     * @param iid     连接id
     * @param dbIndex db索引
     * @param key     键
     * @return 结果
     */
    public boolean delete(String iid, Integer dbIndex, String key) {
        if (StringUtil.isNotBlank(iid) && StringUtil.isNotBlank(key)) {
            DeleteParam param = new DeleteParam();
            param.addQueryParam(QueryParam.of("key", key));
            param.addQueryParam(QueryParam.of("iid", iid));
            param.addQueryParam(QueryParam.of("dbIndex", dbIndex));
            return this.delete(param);
        }
        return false;
    }

    /**
     * 是否存在
     *
     * @param iid     数据id
     * @param dbIndex db索引
     * @param key     键
     * @return 结果
     */
    public boolean exist(String iid, int dbIndex, String key) {
        if (StringUtil.isNotBlank(iid) && StringUtil.isNotBlank(key)) {
            Map<String, Object> params = new HashMap<>();
            params.put("iid", iid);
            params.put("key", key);
            params.put("dbIndex", dbIndex);
            return super.exist(params);
        }
        return false;
    }

    @Override
    protected Class<ShellRedisCollect> modelClass() {
        return ShellRedisCollect.class;
    }

    /**
     * 根据iid删除
     *
     * @param iid 连接id
     */
    public void deleteByIid(String iid) {
        DeleteParam param = new DeleteParam();
        param.addQueryParam(QueryParam.of("iid", iid));
        super.delete(param);
    }
}
