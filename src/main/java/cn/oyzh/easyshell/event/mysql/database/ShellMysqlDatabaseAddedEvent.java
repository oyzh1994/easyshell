package cn.oyzh.easyshell.event.mysql.database;

import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlDatabaseAddedEvent extends Event<MysqlDatabase> {

    private ShellMysqlRootTreeItem connectItem;

    public ShellMysqlRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellMysqlRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
