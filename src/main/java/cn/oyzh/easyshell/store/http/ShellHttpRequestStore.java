package cn.oyzh.easyshell.store.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.DeleteParam;
import cn.oyzh.store.jdbc.param.QueryParam;
import cn.oyzh.store.jdbc.param.SelectParam;

import java.util.List;
import java.util.Map;

/**
 * HTTP 请求存储。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpRequestStore extends JdbcStandardStore<ShellHttpRequest> {

    public static final ShellHttpRequestStore INSTANCE = new ShellHttpRequestStore();

    /**
     * 加载连接下的请求。
     *
     * @param iid 连接id
     * @return 请求列表
     */
    public List<ShellHttpRequest> list(String iid) {
        if (StringUtil.isBlank(iid)) {
            return List.of();
        }
        return super.selectList(QueryParam.of("iid", iid));
    }

    /**
     * 加载集合下的请求。
     *
     * @param collectionId 集合id
     * @return 请求列表
     */
    public List<ShellHttpRequest> listByCollection(String collectionId) {
        if (StringUtil.isBlank(collectionId)) {
            return List.of();
        }
        SelectParam param = new SelectParam();
        param.addQueryParam(QueryParam.of("collectionId", collectionId));
        return super.selectList(param);
    }

    /**
     * 新增或更新请求。
     *
     * @param model 请求
     * @return 结果
     */
    public boolean replace(ShellHttpRequest model) {
        if (model == null) {
            return false;
        }
        return super.exist(model.getId()) ? super.update(model) : super.insert(model);
    }

    /**
     * 根据连接id删除请求。
     *
     * @param iid 连接id
     * @return 结果
     */
    public boolean deleteByIid(String iid) {
        if (StringUtil.isBlank(iid)) {
            return false;
        }
        DeleteParam param = new DeleteParam();
        param.addQueryParam(QueryParam.of("iid", iid));
        return super.delete(param);
    }

    /**
     * 根据集合id删除请求。
     *
     * @param collectionId 集合id
     * @return 结果
     */
    public boolean deleteByCollectionId(String collectionId) {
        if (StringUtil.isBlank(collectionId)) {
            return false;
        }
        DeleteParam param = new DeleteParam();
        param.addQueryParam(QueryParam.of("collectionId", collectionId));
        return super.delete(param);
    }

    @Override
    protected Map<String, Object> toRecord(ShellHttpRequest model) throws Exception {
        model.syncJson();
        return super.toRecord(model);
    }

    @Override
    protected Class<ShellHttpRequest> modelClass() {
        return ShellHttpRequest.class;
    }
}
