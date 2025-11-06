package cn.oyzh.easyshell.event.mysql.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/23
 */
public class MysqlQueryRenamedEvent extends Event<ShellQuery> {

    private MysqlDatabaseTreeItem dbItem;

    public String queryName() {
        return this.data().getName();
    }

    public String queryId() {
        return this.data().getUid();
    }

    public String dbName() {
        return this.dbItem.dbName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
