package cn.oyzh.easyshell.event.mysql.procedure;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/23
 */
public class ShellMysqlProcedureRenamedEvent extends Event<String> {

    private ShellMysqlDatabaseTreeItem dbItem;

    private String newProcedureName;

    public String getNewProcedureName() {
        return newProcedureName;
    }

    public void setNewProcedureName(String newProcedureName) {
        this.newProcedureName = newProcedureName;
    }

    public String procedureName() {
        return this.data();
    }

    public String dbName() {
        return this.dbItem.dbName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
