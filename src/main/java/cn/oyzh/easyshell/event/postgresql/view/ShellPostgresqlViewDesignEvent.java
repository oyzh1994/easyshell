package cn.oyzh.easyshell.event.postgresql.view;

import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL视图设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlViewDesignEvent extends Event<PostgresqlView> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data().getName();
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
