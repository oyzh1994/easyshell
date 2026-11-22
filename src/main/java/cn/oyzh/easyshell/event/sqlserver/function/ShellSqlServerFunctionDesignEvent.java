package cn.oyzh.easyshell.event.sqlserver.function;

import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer函数设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerFunctionDesignEvent extends Event<SqlServerFunction> {

    /**
     * 数据库节点
     */
    private ShellSqlServerSchemaTreeItem dbItem;

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().getName();
    }

    public ShellSqlServerSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellSqlServerSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
