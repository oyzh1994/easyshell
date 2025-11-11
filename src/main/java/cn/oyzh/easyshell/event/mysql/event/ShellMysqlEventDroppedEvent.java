package cn.oyzh.easyshell.event.mysql.event;

import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.MysqlEventTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlEventDroppedEvent extends Event<MysqlEventTreeItem>   {

    public String eventName() {
        return this.data().eventName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
