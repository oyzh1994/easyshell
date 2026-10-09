package cn.oyzh.easyshell.event.mariadb.table;

import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB表设计事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTableDesignEvent extends Event<MariadbTable> {

    /**
     * 数据库节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem.dbName();
    }

    public ShellMariadbDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMariadbDatabaseTreeItem dbItem) {
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
