package cn.oyzh.easyshell.event.sqlserver.view;

import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer视图已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerViewDroppedEvent extends Event<ShellSqlServerViewTreeItem>   {

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data().viewName();
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellSqlServerSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
