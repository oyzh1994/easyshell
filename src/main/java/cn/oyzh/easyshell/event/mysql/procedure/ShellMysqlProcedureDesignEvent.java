package cn.oyzh.easyshell.event.mysql.procedure;

import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMysqlProcedureDesignEvent extends Event<MysqlProcedure> {

    private ShellMysqlDatabaseTreeItem dbItem;

    public String procedureName() {
        return this.data().getName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
