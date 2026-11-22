package cn.oyzh.easyshell.event.sqlserver.function;

import cn.oyzh.easyshell.trees.sqlserver.function.ShellSqlServerFunctionTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer函数已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerFunctionDroppedEvent extends Event<ShellSqlServerFunctionTreeItem>   {

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().functionName();
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
