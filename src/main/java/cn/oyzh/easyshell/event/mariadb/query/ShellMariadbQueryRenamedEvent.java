package cn.oyzh.easyshell.event.mariadb.query;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.query.ShellMariadbQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * MariaDB查询已重命名事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryRenamedEvent extends Event<ShellMariadbQueryTreeItem> implements EventFormatter {

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
