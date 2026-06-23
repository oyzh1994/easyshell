package cn.oyzh.easyshell.event.mysql.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/01/23
 */
public class ShellMysqlQueryRenamedEvent extends Event<String> implements EventFormatter {

    private ShellMysqlDatabaseTreeItem dbItem;

    private String queryName;

    private String newQueryName;

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getNewQueryName() {
        return newQueryName;
    }

    public void setNewQueryName(String newQueryName) {
        this.newQueryName = newQueryName;
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

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] renamed, new name:%s", I18nHelper.query(), this.getQueryName(), this.getNewQueryName());
    }
}
