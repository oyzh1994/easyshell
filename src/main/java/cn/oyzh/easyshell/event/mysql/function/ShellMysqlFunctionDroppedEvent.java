package cn.oyzh.easyshell.event.mysql.function;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.ShellMysqlFunctionTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlFunctionDroppedEvent extends Event<ShellMysqlFunctionTreeItem>   {

    public String functionName() {
        return this.data().functionName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
