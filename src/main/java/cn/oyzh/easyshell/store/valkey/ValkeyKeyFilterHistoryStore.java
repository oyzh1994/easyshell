package cn.oyzh.easyshell.store.valkey;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.valkey.ShellValkeyKeyFilterHistory;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.OrderByParam;
import cn.oyzh.store.jdbc.param.PageParam;
import cn.oyzh.store.jdbc.param.QueryParam;
import cn.oyzh.store.jdbc.param.SelectParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * redis过滤历史存储
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ValkeyKeyFilterHistoryStore extends JdbcStandardStore<ShellValkeyKeyFilterHistory> {

    /**
     * 最大历史数量
     */
    public static int Max_Size = 50;

    /**
     * 当前实例
     */
    public static final ValkeyKeyFilterHistoryStore INSTANCE = new ValkeyKeyFilterHistoryStore();

    /**
     * 加载历史列表
     *
     * @return 历史列表
     */
    public List<ShellValkeyKeyFilterHistory> load() {
        return super.selectList();
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellValkeyKeyFilterHistory model) {
        if (model == null) {
            return false;
        }
        boolean result = this.insert(model);
        if (result) {
            // 查询超出部分
            SelectParam selectParam = new SelectParam();
            selectParam.setLimit(1L);
            selectParam.setOffset((long) Max_Size - 1);
            selectParam.addQueryColumn("uid");
            selectParam.addQueryParam(new QueryParam("iid", model.getIid()));
            selectParam.addQueryParam(new QueryParam("pattern", model.getPattern()));
            selectParam.addOrderByParam(new OrderByParam("saveTime", "asc"));
            ShellValkeyKeyFilterHistory data = super.selectOne(selectParam);
            // 删除超出限制的数据
            if (data != null) {
                this.delete(data.getUid());
            }
        }
        return result;
    }
//
//    public boolean delete(String iid, String kw) {
//        if (StringUtil.isNotBlank(kw) && StringUtil.isNotBlank(iid)) {
//            DeleteParam param = new DeleteParam();
//            param.addQueryParam(new QueryParam("iid", iid));
//            param.addQueryParam(new QueryParam("pattern", kw));
//            return this.delete(param);
//        }
//        return false;
//    }

    /**
     * 分页查询历史
     *
     * @param pageNo 页码
     * @param limit  每页数量
     * @param kw     关键词
     * @return 分页数据
     */
    public Paging<ShellValkeyKeyFilterHistory> getPage(long pageNo, int limit, String kw) {
        PageParam pageParam = new PageParam(limit, pageNo * limit);
        List<ShellValkeyKeyFilterHistory> list = this.selectPage(kw, List.of("pattern"), pageParam);
        Paging<ShellValkeyKeyFilterHistory> paging;
        if (CollectionUtil.isNotEmpty(list)) {
            long count = this.selectCount(kw, List.of("pattern"));
            paging = new Paging<>(list, limit, count);
            paging.currentPage(pageNo);
        } else {
            paging = new Paging<>(limit);
        }
        return paging;
    }

    /**
     * 是否存在
     *
     * @param kw 关键词
     * @return 结果
     */
    public boolean exist(String kw) {
        if (StringUtil.isNotBlank(kw)) {
            Map<String, Object> params = new HashMap<>();
            params.put("pattern", kw);
            return super.exist(params);
        }
        return false;
    }

    @Override
    protected Class<ShellValkeyKeyFilterHistory> modelClass() {
        return ShellValkeyKeyFilterHistory.class;
    }

    /**
     * 获取全部模式
     *
     * @return 模式列表
     */
    public List<String> getPatterns() {
        List<ShellValkeyKeyFilterHistory> histories = this.load();
        return histories.parallelStream().map(ShellValkeyKeyFilterHistory::getPattern).collect(Collectors.toList());
    }
}
