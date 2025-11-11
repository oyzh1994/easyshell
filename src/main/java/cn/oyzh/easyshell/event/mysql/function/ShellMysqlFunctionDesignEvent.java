package cn.oyzh.easyshell.event.mysql.function;

import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMysqlFunctionDesignEvent extends Event<MysqlFunction> {

    private MysqlDatabaseTreeItem dbItem;

    public String functionName() {
        return this.data().getName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
