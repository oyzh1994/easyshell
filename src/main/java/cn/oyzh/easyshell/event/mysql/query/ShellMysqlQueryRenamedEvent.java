package cn.oyzh.easyshell.event.mysql.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/23
 */
public class ShellMysqlQueryRenamedEvent extends Event<ShellQuery> {

    private ShellMysqlDatabaseTreeItem dbItem;

    public String queryName() {
        return this.data().getName();
    }

    public String queryId() {
        return this.data().getUid();
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
