package cn.oyzh.easyshell.event.mongo.database;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMongoDatabaseDroppedEvent extends Event<ShellMongoDatabaseTreeItem> {

    public String eventFormat() {
        return String.format("[%s:%s] deleted", I18nHelper.database(), this.data().dbName());
    }
}
