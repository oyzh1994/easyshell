package cn.oyzh.easyshell.event.mysql.table;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTableTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql表已删除事件
 *
 * @author oyzh
 * @since 2024/01/24
 */
public class ShellMysqlTableDroppedEvent extends Event<ShellMysqlTableTreeItem> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.data().tableName();
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
