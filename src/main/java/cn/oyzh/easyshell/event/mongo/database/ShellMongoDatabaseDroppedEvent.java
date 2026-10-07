package cn.oyzh.easyshell.event.mongo.database;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb数据库已删除事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoDatabaseDroppedEvent extends Event<ShellMongoDatabaseTreeItem> implements EventFormatter {

    /**
     * 格式化事件描述
     *
     * @return 事件描述
     */
    @Override
    public String eventFormat() {
        return String.format("[%s:%s] deleted", I18nHelper.database(), this.data().dbName());
    }
}
