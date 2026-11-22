package cn.oyzh.easyshell.event.sqlserver.query;

import cn.oyzh.easyshell.trees.sqlserver.query.ShellSqlServerQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * SqlServer查询已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerQueryDeletedEvent extends Event<ShellSqlServerQueryTreeItem> implements EventFormatter {

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
