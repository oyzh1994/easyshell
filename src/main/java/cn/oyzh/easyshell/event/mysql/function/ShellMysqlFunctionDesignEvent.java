package cn.oyzh.easyshell.event.mysql.function;

import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMysqlFunctionDesignEvent extends Event<MysqlFunction> {

    private ShellMysqlDatabaseTreeItem dbItem;

    public String functionName() {
        return this.data().getName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
