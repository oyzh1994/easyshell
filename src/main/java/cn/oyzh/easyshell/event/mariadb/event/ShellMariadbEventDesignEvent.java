package cn.oyzh.easyshell.event.mariadb.event;

import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB事件设计事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEventDesignEvent extends Event<MariadbEvent> {

    /**
     * 数据库节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    public String eventName() {
        return this.data().getName();
    }

    public ShellMariadbDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMariadbDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
