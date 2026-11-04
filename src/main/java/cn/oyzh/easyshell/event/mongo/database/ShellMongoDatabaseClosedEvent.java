package cn.oyzh.easyshell.event.mongo.database;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * mongodb数据库已关闭事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoDatabaseClosedEvent extends Event<ShellMongoDatabaseTreeItem> implements EventFormatter {

    @Override
    public String eventFormat() {
return ShellI18nHelper.eventDatabaseClosed(String.valueOf(this.data().value()));
    }
}
