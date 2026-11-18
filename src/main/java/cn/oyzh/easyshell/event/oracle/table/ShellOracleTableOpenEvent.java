package cn.oyzh.easyshell.event.oracle.table;

import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTableTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle表打开事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTableOpenEvent extends Event<ShellOracleTableTreeItem> {

    /**
     * 数据库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.data().tableName();
    }

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
}
