package cn.oyzh.easyshell.event.mariadb.event;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.event.ShellMariadbEventTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB事件已删除事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEventDroppedEvent extends Event<ShellMariadbEventTreeItem>   {

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    public String eventName() {
        return this.data().eventName();
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellMariadbDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
