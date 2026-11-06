package cn.oyzh.easyshell.event.postgresql.table;

import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTableTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL表已截断事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTableTruncatedEvent extends Event<ShellPostgresqlTableTreeItem> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.data().tableName();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.dbItem.schema();
    }

    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
