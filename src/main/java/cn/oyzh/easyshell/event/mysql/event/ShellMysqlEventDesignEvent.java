package cn.oyzh.easyshell.event.mysql.event;

import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql事件设计事件
 *
 * @author oyzh
 * @since 2024/09/09
 */
public class ShellMysqlEventDesignEvent extends Event<MysqlEvent> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    public String eventName() {
        return this.data().getName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
