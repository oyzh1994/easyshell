package cn.oyzh.easyshell.event.mysql.function;

import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.MysqlFunctionTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlFunctionDroppedEvent extends Event<MysqlFunctionTreeItem>   {

    public String functionName() {
        return this.data().functionName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
