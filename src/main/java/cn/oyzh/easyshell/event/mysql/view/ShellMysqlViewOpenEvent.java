package cn.oyzh.easyshell.event.mysql.view;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql视图打开事件
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMysqlViewOpenEvent extends Event<ShellMysqlViewTreeItem> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data().viewName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
