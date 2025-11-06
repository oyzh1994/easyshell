package cn.oyzh.easyshell.event.mysql.database;

import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;

/**
 * @author oyzh
 * @since 2024/01/26
 */
public class MysqlDatabaseClosedEvent extends Event<MysqlDatabaseTreeItem> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s] 数据库已关闭", this.data().value());
    }
}
