package cn.oyzh.easyshell.event.sqlserver.procedure;

import cn.oyzh.easyshell.trees.sqlserver.function.ShellSqlServerFunctionTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.procedure.ShellSqlServerProcedureTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer存储过程已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerProcedureDroppedEvent extends Event<ShellSqlServerProcedureTreeItem>   {

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
        return this.data().procedureName();
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
