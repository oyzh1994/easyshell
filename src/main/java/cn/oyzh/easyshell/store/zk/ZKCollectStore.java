package cn.oyzh.easyshell.store.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKCollect;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * zk收藏存储
 *
 * @author oyzh
 * @since 2024/09/26
 */
public class ZKCollectStore extends JdbcStandardStore<ZKCollect> {

    /**
     * 当前实例
     */
    public static final ZKCollectStore INSTANCE = new ZKCollectStore();

    /**
     * 根据zk连接id加载列表
     *
     * @param iid zk连接id
     * @return 收藏列表
     */
    public List<ZKCollect> loadByIid(String iid) {
        QueryParam param = new QueryParam();
        param.setName("iid");
        param.setData(iid);
        return super.selectList(param);
    }

    /**
     * 替换
     *
     * @param iid  zk连接id
     * @param path zk路径
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean replace(String iid, String path) {
        return this.replace(new ZKCollect(iid, path));
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ZKCollect model) {
        if (model != null && !this.exist(model.getIid(), model.getPath())) {
            return this.insert(model);
        }
        return false;
    }

    /**
     * 根据zk连接id删除收藏
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

    /**
     * 根据zk连接id和路径删除收藏
     *
     * @param iid  zk连接id
     * @param path zk路径
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean delete(String iid, String path) {
        if (StringUtil.isEmpty(iid) && StringUtil.isEmpty(path)) {
            DeleteParam param = new DeleteParam();
            param.addQueryParam(QueryParam.of("iid", iid));
            param.addQueryParam(QueryParam.of("path", path));
            return this.delete(path);
        }
        return false;
    }

    /**
     * 是否存在
     *
     * @param iid  zk连接id
     * @param path zk路径
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean exist(String iid, String path) {
        if (StringUtil.isNotBlank(iid) && StringUtil.isNotBlank(path)) {
            Map<String, Object> params = new HashMap<>();
            params.put("iid", iid);
            params.put("path", path);
            return super.exist(params);
        }
        return false;
    }

    @Override
    protected Class<ZKCollect> modelClass() {
        return ZKCollect.class;
    }
}
