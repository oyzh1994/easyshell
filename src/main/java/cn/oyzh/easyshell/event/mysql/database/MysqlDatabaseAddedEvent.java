package cn.oyzh.easyshell.event.mysql.database;

import cn.oyzh.easyshell.mysql.DBDatabase;
import cn.oyzh.easyshell.trees.mysql.root.DBRootTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class MysqlDatabaseAddedEvent extends Event<DBDatabase> implements EventFormatter {

    private DBRootTreeItem connectItem;

    @Override
    public String eventFormat() {
        return String.format("[%s] 数据库已新增", this.data().getName());
    }

    public DBRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(DBRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
