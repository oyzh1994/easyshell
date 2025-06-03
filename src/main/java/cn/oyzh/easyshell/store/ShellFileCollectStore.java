package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellFileCollect;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.OrderByParam;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.ArrayList;
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
    public static int His_Max_Size = 20;

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
        List<QueryParam> queryParams = new ArrayList<>(4);
        queryParams.add(new QueryParam("iid", model.getIid()));
        long count = super.selectCount(queryParams);
        // 删除超出限制的数据
        if (count > His_Max_Size) {
            DeleteParam deleteParam = new DeleteParam();
            deleteParam.addQueryParams(queryParams);
            deleteParam.addOrderByParam(new OrderByParam("saveTime", "desc"));
            deleteParam.setLimit(1L);
            super.delete(deleteParam);
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
