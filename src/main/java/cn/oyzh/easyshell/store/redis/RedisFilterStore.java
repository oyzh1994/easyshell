package cn.oyzh.easyshell.store.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.redis.RedisFilter;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;
import cn.oyzh.store.jdbc.SelectParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/09/24
 */
public class RedisFilterStore extends JdbcStandardStore<RedisFilter> {

    /**
     * 当前实例
     */
    public static final RedisFilterStore INSTANCE = new RedisFilterStore();

    /**
     * 加载已启用的数据列表
     *
     * @param iid redis连接id
     * @return 已启用的数据列表
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public List<RedisFilter> loadEnable(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return Collections.emptyList();
        }
        SelectParam selectParam = new SelectParam();
        selectParam.addQueryParam(QueryParam.of("enable", 1));
        selectParam.addQueryParam(QueryParam.of("iid", iid));
        return super.selectList(selectParam);
    }

    public List<RedisFilter> loadByIid(String iid) {
        return super.selectList(QueryParam.of("iid", iid));
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(RedisFilter model) {
        boolean result = false;
        if (model != null) {
            if (this.exist(model.getKw(), model.getIid())) {
                result = this.update(model);
            } else {
                result = this.insert(model);
            }
        }
        return result;
    }

    /**
     * 根据iid删除数据
     *
     * @param iid redis连接id
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean deleteByIid(String iid) {
        if (StringUtil.isNotBlank(iid)) {
            DeleteParam param = new DeleteParam();
            param.addQueryParam(new QueryParam("iid", iid));
            return this.delete(param);
        }
        return false;
    }

    /**
     * 判断是否存在
     *
     * @param kw  关键字
     * @param iid redis连接id
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean exist(String kw, String iid) {
        if (StringUtil.isNotBlank(kw)) {
            Map<String, Object> params = new HashMap<>();
            params.put("kw", kw);
            params.put("iid", iid);
            return super.exist(params);
        }
        return false;
    }

    @Override
    protected Class<RedisFilter> modelClass() {
        return RedisFilter.class;
    }

}
