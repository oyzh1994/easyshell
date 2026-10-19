package cn.oyzh.easyshell.event.mariadb.view;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB视图已变更事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewAlertedEvent extends Event<String> {

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
