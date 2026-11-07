package cn.oyzh.easyshell.fx.valkey;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.store.valkey.ValkeyKeyFilterHistoryStore;
import cn.oyzh.fx.plus.controls.popup.SearchHistoryPopup;

import java.util.List;

/**
 * redis键过滤历史弹窗
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyKeyFilterHistoryPopup extends SearchHistoryPopup {

    /**
     * 过滤历史储存
     */
    private final ValkeyKeyFilterHistoryStore historyStore = ValkeyKeyFilterHistoryStore.INSTANCE;

    @Override
    public List<String> getHistories() {
        List<String> list = this.historyStore.getPatterns();
        if (CollectionUtil.isNotEmpty(list)) {
            return list.reversed();
        }
        return list;
    }
}
