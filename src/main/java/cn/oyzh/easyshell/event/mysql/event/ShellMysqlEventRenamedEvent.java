package cn.oyzh.easyshell.event.mysql.event;

import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.MysqlEventTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/23
 */
public class ShellMysqlEventRenamedEvent extends Event<MysqlEventTreeItem> {

    private MysqlDatabaseTreeItem dbItem;

    public String eventName() {
        return this.data().eventName();
    }

    public String dbName() {
        return this.dbItem.dbName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
