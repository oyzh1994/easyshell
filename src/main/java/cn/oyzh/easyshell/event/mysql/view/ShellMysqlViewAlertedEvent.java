package cn.oyzh.easyshell.event.mysql.view;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql视图已变更事件
 *
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMysqlViewAlertedEvent extends Event<String> {

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
