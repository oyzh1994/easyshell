package cn.oyzh.easyshell.event.mysql.event;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql事件已重命名事件
 *
 * @author oyzh
 * @since 2024/01/23
 */
public class ShellMysqlEventRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 新事件名称
     */
    private String newEventName;

    public String getNewEventName() {
        return newEventName;
    }

    public void setNewEventName(String newEventName) {
        this.newEventName = newEventName;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    public String eventName() {
        return this.data();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem.dbName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
