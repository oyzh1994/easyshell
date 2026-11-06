package cn.oyzh.easyshell.event.postgresql.query;

import cn.oyzh.easyshell.trees.postgresql.query.ShellPostgresqlQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * PostgreSQL查询已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlQueryRenamedEvent extends Event<ShellPostgresqlQueryTreeItem> implements EventFormatter {

    /**
     * 查询名称
     */
    private String queryName;

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String newQueryName() {
        return this.data().queryName();
    }

    public String queryId() {
        return this.data().value().getUid();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.data().schema();
    }

    @Override
    public String eventFormat() {
return ShellI18nHelper.eventQueryRenamed(this.getQueryName(), this.newQueryName());
    }
}
