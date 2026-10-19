package cn.oyzh.easyshell.event.mariadb.table;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTableTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB表打开事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTableOpenEvent extends Event<ShellMariadbTableTreeItem> {

    /**
     * 数据库节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.data().tableName();
    }

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
}
