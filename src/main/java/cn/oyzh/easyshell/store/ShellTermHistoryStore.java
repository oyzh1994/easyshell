package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellTermHistory;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.OrderByParam;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * shell终端历史存储
 *
 * @author oyzh
 * @since 2025/05/31
 */
public class ShellTermHistoryStore extends JdbcStandardStore<ShellTermHistory> {

    /**
     * 最大历史数量
     */
    public static int His_Max_Size = 50;

    /**
     * 当前实例
     */
    public static final ShellTermHistoryStore INSTANCE = new ShellTermHistoryStore();

    @Override
    protected Class<ShellTermHistory> modelClass() {
        return ShellTermHistory.class;
    }

    /**
     * 保存
     * @param model 数据
     * @return 结果
     */
    public boolean save(ShellTermHistory model) {
        boolean result = this.insert(model);
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
     * @return shell终端历史
     */
    public List<ShellTermHistory> loadByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        List<ShellTermHistory> results = super.selectList(QueryParam.of("iid", iid));
        // 执行排序
        results.sort(Comparator.comparingLong(ShellTermHistory::getSaveTime));
        return results.reversed();
    }
}
