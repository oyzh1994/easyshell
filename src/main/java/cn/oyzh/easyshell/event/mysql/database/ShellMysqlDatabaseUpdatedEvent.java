package cn.oyzh.easyshell.event.mysql.database;

import cn.oyzh.easyshell.mysql.database.MysqlDatabase;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql数据库已更新事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlDatabaseUpdatedEvent extends Event<MysqlDatabase> {

    /**
     * 连接节点
     */
    private ShellMysqlRootTreeItem connectItem;

    public ShellMysqlRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellMysqlRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
