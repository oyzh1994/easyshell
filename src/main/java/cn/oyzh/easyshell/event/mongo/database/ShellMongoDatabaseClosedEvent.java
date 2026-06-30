package cn.oyzh.easyshell.event.mongo.database;

import cn.oyzh.easyshell.mongo.trees.database.MongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/01/26
 */
public class ShellMongoDatabaseClosedEvent extends Event<MongoDatabaseTreeItem> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] closed", I18nHelper.database(), this.data().value());
    }
}
