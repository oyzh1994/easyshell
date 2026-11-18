package cn.oyzh.easyshell.event.oracle.table;

import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle表已变更事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTableAlertedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    public ShellOracleSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
