package cn.oyzh.easyshell.event.postgresql.procedure;

import cn.oyzh.easyshell.trees.postgresql.function.ShellPostgresqlFunctionTreeItem;
import cn.oyzh.easyshell.trees.postgresql.procedure.ShellPostgresqlProcedureTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL存储过程已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlProcedureDroppedEvent extends Event<ShellPostgresqlProcedureTreeItem>   {

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
    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
