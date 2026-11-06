package cn.oyzh.easyshell.event.postgresql.procedure;

import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL存储过程已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlProcedureRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 新存储过程名称
     */
    private String newProcedureName;

    public String getNewProcedureName() {
        return newProcedureName;
    }

    public void setNewProcedureName(String newProcedureName) {
        this.newProcedureName = newProcedureName;
    }

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
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
