package cn.oyzh.easyshell.event.postgresql.function;

import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL函数设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlFunctionDesignEvent extends Event<PostgresqlFunction> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().getName();
    }

    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
