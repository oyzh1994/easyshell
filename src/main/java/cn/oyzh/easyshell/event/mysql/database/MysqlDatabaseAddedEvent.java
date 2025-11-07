package cn.oyzh.easyshell.event.mysql.database;

import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.trees.mysql.root.MysqlRootTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class MysqlDatabaseAddedEvent extends Event<MysqlDatabase> {

    private MysqlRootTreeItem connectItem;

    public MysqlRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(MysqlRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
