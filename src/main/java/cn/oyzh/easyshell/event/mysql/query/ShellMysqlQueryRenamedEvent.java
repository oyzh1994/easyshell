package cn.oyzh.easyshell.event.mysql.query;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.query.ShellMysqlQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mysql查询已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlQueryRenamedEvent extends Event<ShellMysqlQueryTreeItem> implements EventFormatter {

    /**
     * 查询名称
     */
    private String queryName;

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String newQueryName() {
        return this.data().queryName();
    }

    public String queryId() {
        return this.data().value().getUid();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.data().dbName();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] renamed, new name:%s", I18nHelper.query(), this.getQueryName(), this.newQueryName());
    }
}
