package cn.oyzh.easyshell.event.mongo.database;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb数据库已关闭事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoDatabaseClosedEvent extends Event<ShellMongoDatabaseTreeItem> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] closed", I18nHelper.database(), this.data().value());
    }
}
