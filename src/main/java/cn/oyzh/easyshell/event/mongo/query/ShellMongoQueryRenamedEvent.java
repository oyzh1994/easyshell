package cn.oyzh.easyshell.event.mongo.query;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.query.ShellMongoQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb查询已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoQueryRenamedEvent extends Event<ShellMongoQueryTreeItem> implements EventFormatter {

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
