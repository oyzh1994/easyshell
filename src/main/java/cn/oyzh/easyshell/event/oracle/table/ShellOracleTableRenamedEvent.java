package cn.oyzh.easyshell.event.oracle.table;

import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTableTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle表已重命名事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTableRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 新表名称
     */
    private String newTableName;

    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
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

    public ShellOracleSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
