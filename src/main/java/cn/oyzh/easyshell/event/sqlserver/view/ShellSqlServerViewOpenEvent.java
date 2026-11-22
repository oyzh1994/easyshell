package cn.oyzh.easyshell.event.sqlserver.view;

import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer视图打开事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerViewOpenEvent extends Event<ShellSqlServerViewTreeItem> {

    /**
     * 数据库节点
     */
    private ShellSqlServerSchemaTreeItem dbItem;

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data().viewName();
    }

    public ShellSqlServerSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellSqlServerSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
