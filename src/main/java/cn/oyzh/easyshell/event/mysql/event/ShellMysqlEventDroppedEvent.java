package cn.oyzh.easyshell.event.mysql.event;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.ShellMysqlEventTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql事件已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlEventDroppedEvent extends Event<ShellMysqlEventTreeItem>   {

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
    public ShellMysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
