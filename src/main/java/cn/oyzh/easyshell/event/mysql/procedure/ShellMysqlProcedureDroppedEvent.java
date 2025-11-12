package cn.oyzh.easyshell.event.mysql.procedure;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.ShellMysqlProcedureTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlProcedureDroppedEvent extends Event<ShellMysqlProcedureTreeItem>   {

    public String procedureName() {
        return this.data().procedureName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
