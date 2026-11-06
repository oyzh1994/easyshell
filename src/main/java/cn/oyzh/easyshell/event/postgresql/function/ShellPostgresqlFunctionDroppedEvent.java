package cn.oyzh.easyshell.event.postgresql.function;

import cn.oyzh.easyshell.trees.postgresql.function.ShellPostgresqlFunctionTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL函数已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlFunctionDroppedEvent extends Event<ShellPostgresqlFunctionTreeItem>   {

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
    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
