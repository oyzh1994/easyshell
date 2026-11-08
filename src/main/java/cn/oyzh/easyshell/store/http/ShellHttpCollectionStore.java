package cn.oyzh.easyshell.store.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.DeleteParam;
import cn.oyzh.store.jdbc.param.QueryParam;

import java.util.List;

/**
 * HTTP 请求集合存储。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpCollectionStore extends JdbcStandardStore<ShellHttpCollection> {

    public static final ShellHttpCollectionStore INSTANCE = new ShellHttpCollectionStore();

    /**
     * 加载连接下的集合。
     *
     * @param iid 连接id
     * @return 集合列表
     */
    public List<ShellHttpCollection> list(String iid) {
        if (StringUtil.isBlank(iid)) {
            return List.of();
        }
        return super.selectList(QueryParam.of("iid", iid));
    }

    /**
     * 新增或更新集合。
     *
     * @param model 集合
     * @return 结果
     */
    public boolean replace(ShellHttpCollection model) {
        if (model == null) {
            return false;
        }
        return super.exist(model.getId()) ? super.update(model) : super.insert(model);
    }

    /**
     * 根据连接id删除集合。
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
    protected Class<ShellHttpCollection> modelClass() {
        return ShellHttpCollection.class;
    }
}
