package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellFileCollect;
import cn.oyzh.store.jdbc.sqlite.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.sqlite.OrderByParam;
import cn.oyzh.store.jdbc.param.QueryParam;
import cn.oyzh.store.jdbc.param.SelectParam;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * shell文件收藏存储
 *
 * @author oyzh
 * @since 2025/06/03
 */
public class ShellFileCollectStore extends JdbcStandardStore<ShellFileCollect> {

    /**
     * 最大数量
     */
    public static int Max_Size = 20;

    /**
     * 当前实例
     */
    public static final ShellFileCollectStore INSTANCE = new ShellFileCollectStore();

    @Override
    protected Class<ShellFileCollect> modelClass() {
        return ShellFileCollect.class;
    }

    /**
     * 是否存在
     *
     * @param iid  连接id
     * @param path 路径
     */
    public boolean exist(String iid, String path) {
        Map<String, Object> params = new HashMap<>();
        params.put("iid", iid);
        params.put("content", path);
        return this.exist(params);
    }

    /**
     * 删除
     *
     * @param iid  连接id
     * @param path 路径
     */
    public boolean delete(String iid, String path) {
        DeleteParam param = new DeleteParam();
        param.addQueryParam(QueryParam.of("iid", iid));
        param.addQueryParam(QueryParam.of("content", path));
        return this.delete(param);
    }


    /**
     * 替换
     *
     * @param model 数据
     * @return 结果
     */
    public boolean replace(ShellFileCollect model) {
        boolean result = this.exist(model.getIid(), model.getContent()) || this.insert(model);
        if (!result) {
            return false;
        }
        // 查询总数
        SelectParam selectParam = new SelectParam();
        selectParam.addQueryColumn("id");
        selectParam.setLimit(1L);
        selectParam.setOffset((long) Max_Size - 1);
        selectParam.addQueryParam(new QueryParam("iid", model.getIid()));
        selectParam.addOrderByParam(new OrderByParam("saveTime", "asc"));
        ShellFileCollect data = super.selectOne(selectParam);
        // 删除超出限制的数据
        if (data != null) {
            super.delete(data.getId());
        }
        return true;
    }

    /**
     * 根据iid删除
     *
     * @param iid shell连接id
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

    /**
     * 根据shell连接id获取配置
     *
     * @param iid shell连接id
     * @return shell文件收藏
     */
    public List<ShellFileCollect> loadByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        List<ShellFileCollect> results = super.selectList(QueryParam.of("iid", iid));
        // 执行排序
        results.sort(Comparator.comparingLong(ShellFileCollect::getSaveTime));
        return results.reversed();
    }
}
