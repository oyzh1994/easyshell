package cn.oyzh.easyshell.event.mongo.database;

import cn.oyzh.easyshell.mongo.database.MongoDatabase;
import cn.oyzh.easyshell.trees.mongo.root.ShellMongoRootTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb数据库已新增事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoDatabaseAddedEvent extends Event<MongoDatabase> implements EventFormatter {

    /**
     * 连接节点
     */
    private ShellMongoRootTreeItem connectItem;

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] added", I18nHelper.database(), this.data().getName());
    }

    public ShellMongoRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellMongoRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
