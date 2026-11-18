package cn.oyzh.easyshell.event.oracle.function;

import cn.oyzh.easyshell.trees.oracle.function.ShellOracleFunctionTreeItem;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle函数已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleFunctionDroppedEvent extends Event<ShellOracleFunctionTreeItem>   {

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
    public ShellOracleSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
