package cn.oyzh.easyshell.event.mysql.event;

import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/09/09
 */
public class MysqlEventDesignEvent extends Event<MysqlEvent> {

    private MysqlDatabaseTreeItem dbItem;

    public String eventName() {
        return this.data().getName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
