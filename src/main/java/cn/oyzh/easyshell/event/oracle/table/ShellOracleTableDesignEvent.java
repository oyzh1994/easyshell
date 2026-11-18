package cn.oyzh.easyshell.event.oracle.table;

import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle表设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTableDesignEvent extends Event<OracleTable> {

    /**
     * 数据库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.dbItem.schema();
    }

    public ShellOracleSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.data().getName();
    }
}
