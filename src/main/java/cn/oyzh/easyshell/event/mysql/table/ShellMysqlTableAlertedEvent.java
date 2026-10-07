package cn.oyzh.easyshell.event.mysql.table;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql表已变更事件
 *
 * @author oyzh
 * @since 2024/01/17
 */
public class ShellMysqlTableAlertedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
