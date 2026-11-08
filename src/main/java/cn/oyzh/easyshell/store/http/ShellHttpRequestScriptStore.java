package cn.oyzh.easyshell.store.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpRequestScript;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.DeleteParam;
import cn.oyzh.store.jdbc.param.QueryParam;


/**
 * HTTP 请求脚本存储。
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpRequestScriptStore extends JdbcStandardStore<ShellHttpRequestScript> {

    public static final ShellHttpRequestScriptStore INSTANCE = new ShellHttpRequestScriptStore();

    /**
     * 根据请求id获取脚本。
     *
     * @param requestId 请求id
     * @return 脚本
     */
    public ShellHttpRequestScript getByRequestId(String requestId) {
        if (StringUtil.isBlank(requestId)) {
            return null;
        }
        return super.selectOne(QueryParam.of("requestId", requestId));
    }

    /**
     * 新增或更新脚本。
     *
     * @param model 脚本
     * @return 结果
     */
    public boolean replace(ShellHttpRequestScript model) {
        if (model == null) {
            return false;
        }
        ShellHttpRequestScript saved = this.getByRequestId(model.getRequestId());
        if (saved != null) {
            model.setId(saved.getId());
            return super.update(model);
        }
        return super.insert(model);
    }

    /**
     * 根据请求id删除脚本。
     *
     * @param requestId 请求id
     * @return 结果
     */
    public boolean deleteByRequestId(String requestId) {
        if (StringUtil.isBlank(requestId)) {
            return false;
        }
        DeleteParam param = new DeleteParam();
        param.addQueryParam(QueryParam.of("requestId", requestId));
        return super.delete(param);
    }

    /**
     * 根据连接id删除脚本。
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

    @Override
    protected Class<ShellHttpRequestScript> modelClass() {
        return ShellHttpRequestScript.class;
    }
}
