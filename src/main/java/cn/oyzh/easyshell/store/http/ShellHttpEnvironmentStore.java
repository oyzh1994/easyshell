package cn.oyzh.easyshell.store.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.DeleteParam;
import cn.oyzh.store.jdbc.param.QueryParam;

import java.util.List;
import java.util.Map;

/**
 * HTTP 环境变量存储。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpEnvironmentStore extends JdbcStandardStore<ShellHttpEnvironment> {

    public static final ShellHttpEnvironmentStore INSTANCE = new ShellHttpEnvironmentStore();

    /**
     * 加载连接下的环境。
     *
     * @param iid 连接id
     * @return 环境列表
     */
    public List<ShellHttpEnvironment> list(String iid) {
        if (StringUtil.isBlank(iid)) {
            return List.of();
        }
        return super.selectList(QueryParam.of("iid", iid));
    }

    /**
     * 新增或更新环境。
     *
     * @param model 环境
     * @return 结果
     */
    public boolean replace(ShellHttpEnvironment model) {
        if (model == null) {
            return false;
        }
        return super.exist(model.getId()) ? super.update(model) : super.insert(model);
    }

    /**
     * 根据连接id删除环境。
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
    protected Map<String, Object> toRecord(ShellHttpEnvironment model) throws Exception {
        model.syncJson();
        return super.toRecord(model);
    }

    @Override
    protected Class<ShellHttpEnvironment> modelClass() {
        return ShellHttpEnvironment.class;
    }
}
