package cn.oyzh.easyshell.event.mysql.table;

import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/08/07
 */
public class ShellMysqlTableDesignEvent extends Event<MysqlTable> {

    private ShellMysqlDatabaseTreeItem dbItem;

    public String dbName() {
        return this.dbItem.dbName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    public String tableName() {
        return this.data().getName();
    }
}
