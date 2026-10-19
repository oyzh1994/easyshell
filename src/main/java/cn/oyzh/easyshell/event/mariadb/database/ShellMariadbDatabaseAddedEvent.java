package cn.oyzh.easyshell.event.mariadb.database;

import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.trees.mariadb.root.ShellMariadbRootTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB数据库已新增事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDatabaseAddedEvent extends Event<MariadbDatabase> {

    /**
     * 连接节点
     */
    private ShellMariadbRootTreeItem connectItem;

    public ShellMariadbRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellMariadbRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
