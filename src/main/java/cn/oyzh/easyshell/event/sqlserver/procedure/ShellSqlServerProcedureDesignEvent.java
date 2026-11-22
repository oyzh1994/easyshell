package cn.oyzh.easyshell.event.sqlserver.procedure;

import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer存储过程设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerProcedureDesignEvent extends Event<SqlServerProcedure> {

    /**
     * 数据库节点
     */
    private ShellSqlServerSchemaTreeItem dbItem;

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
        return this.data().getName();
    }

    public ShellSqlServerSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellSqlServerSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
