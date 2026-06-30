package cn.oyzh.easyshell.event.mongo.database;

import cn.oyzh.easyshell.mongo.database.MongoDatabase;
import cn.oyzh.easyshell.mongo.trees.root.ShellMongoRootTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMongoDatabaseAddedEvent extends Event<MongoDatabase> implements EventFormatter {

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
