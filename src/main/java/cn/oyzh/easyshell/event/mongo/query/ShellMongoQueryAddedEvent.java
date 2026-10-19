package cn.oyzh.easyshell.event.mongo.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb查询已新增事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoQueryAddedEvent extends Event<ShellQuery> implements EventFormatter {

    /**
     * 数据库节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] added", I18nHelper.query(), this.data().getName());
    }
}
