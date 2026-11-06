package cn.oyzh.easyshell.event.postgresql.view;

import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL视图已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlViewDroppedEvent extends Event<ShellPostgresqlViewTreeItem>   {

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
    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
