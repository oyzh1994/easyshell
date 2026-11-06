package cn.oyzh.easyshell.event.postgresql.function;

import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL函数已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlFunctionRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 新函数名称
     */
    private String newFunctionName;

    public String getNewFunctionName() {
        return newFunctionName;
    }

    public void setNewFunctionName(String newFunctionName) {
        this.newFunctionName = newFunctionName;
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
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
