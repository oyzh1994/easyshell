package cn.oyzh.easyshell.event.mysql.event;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.ShellMysqlEventTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlEventDroppedEvent extends Event<ShellMysqlEventTreeItem>   {

    public String eventName() {
        return this.data().eventName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
