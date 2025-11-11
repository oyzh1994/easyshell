package cn.oyzh.easyshell.event.mysql.view;

import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.MysqlViewTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlViewDroppedEvent extends Event<MysqlViewTreeItem>   {

    public String viewName() {
        return this.data().viewName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
