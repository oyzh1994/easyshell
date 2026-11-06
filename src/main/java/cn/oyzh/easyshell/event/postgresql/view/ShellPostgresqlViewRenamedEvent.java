package cn.oyzh.easyshell.event.postgresql.view;

import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL视图已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlViewRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 新视图名称
     */
    private String newViewName;

    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data();
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
