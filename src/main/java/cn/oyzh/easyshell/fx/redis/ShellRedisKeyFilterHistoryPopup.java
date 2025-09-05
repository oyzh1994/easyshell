package cn.oyzh.easyshell.fx.redis;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.store.redis.RedisKeyFilterHistoryStore;
import cn.oyzh.fx.plus.controls.popup.SearchHistoryPopup;

import java.util.List;

/**
 * redis键过滤历史弹窗
 *
 * @author oyzh
 * @since 2023/07/19
 */
public class ShellRedisKeyFilterHistoryPopup extends SearchHistoryPopup {

    /**
     * 过滤历史储存
     */
    private final RedisKeyFilterHistoryStore historyStore = RedisKeyFilterHistoryStore.INSTANCE;

    @Override
    public List<String> getHistories() {
        List<String> list = this.historyStore.getPatterns();
        if (CollectionUtil.isNotEmpty(list)) {
            return list.reversed();
        }
        return list;
    }
}
