package cn.oyzh.easyshell.event.sqlserver.view;

import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer视图已变更事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerViewAlertedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellSqlServerSchemaTreeItem dbItem;

    public ShellSqlServerSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellSqlServerSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
