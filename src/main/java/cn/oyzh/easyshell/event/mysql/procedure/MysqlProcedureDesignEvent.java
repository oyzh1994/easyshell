package cn.oyzh.easyshell.event.mysql.procedure;

import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class MysqlProcedureDesignEvent extends Event<MysqlProcedure> {

    private MysqlDatabaseTreeItem dbItem;

    public String procedureName() {
        return this.data().getName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
