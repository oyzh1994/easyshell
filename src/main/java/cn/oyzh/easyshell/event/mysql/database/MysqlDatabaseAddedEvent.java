package cn.oyzh.easyshell.event.mysql.database;

import cn.oyzh.easyshell.mysql.DBDatabase;
import cn.oyzh.easyshell.trees.mysql.root.DBRootTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class MysqlDatabaseAddedEvent extends Event<DBDatabase> {

    private DBRootTreeItem connectItem;

    public DBRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(DBRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
