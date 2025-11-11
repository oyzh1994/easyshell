package cn.oyzh.easyshell.event.mysql.procedure;

import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.MysqlProcedureTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlProcedureDroppedEvent extends Event<MysqlProcedureTreeItem>   {

    public String procedureName() {
        return this.data().procedureName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
