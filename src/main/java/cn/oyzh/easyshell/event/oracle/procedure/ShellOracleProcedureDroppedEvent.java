package cn.oyzh.easyshell.event.oracle.procedure;

import cn.oyzh.easyshell.trees.oracle.function.ShellOracleFunctionTreeItem;
import cn.oyzh.easyshell.trees.oracle.procedure.ShellOracleProcedureTreeItem;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle存储过程已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleProcedureDroppedEvent extends Event<ShellOracleProcedureTreeItem>   {

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
    public ShellOracleSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
