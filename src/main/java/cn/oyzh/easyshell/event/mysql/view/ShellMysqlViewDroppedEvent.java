package cn.oyzh.easyshell.event.mysql.view;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlViewDroppedEvent extends Event<ShellMysqlViewTreeItem>   {

    public String viewName() {
        return this.data().viewName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
