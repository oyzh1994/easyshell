package cn.oyzh.easyshell.event.mongo.user;

import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.user.ShellMongoUserTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/01/24
 */
public class ShellMongoUserDeletedEvent extends Event<ShellMongoUserTreeItem> implements EventFormatter {

    public String userName() {
        return this.data().userName();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] deleted", I18nHelper.user(), this.userName());
    }
}
