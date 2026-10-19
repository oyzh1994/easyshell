package cn.oyzh.easyshell.event.mariadb.table;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB表已变更事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTableAlertedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    public ShellMariadbDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMariadbDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
