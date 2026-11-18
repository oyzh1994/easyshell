package cn.oyzh.easyshell.event.oracle.query;

import cn.oyzh.easyshell.trees.oracle.query.ShellOracleQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * Oracle查询已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleQueryDeletedEvent extends Event<ShellOracleQueryTreeItem> implements EventFormatter {

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.data().value().getUid();
    }

    @Override
    public String eventFormat() {
return ShellI18nHelper.eventQueryDeleted(this.data().queryName());
    }
}
