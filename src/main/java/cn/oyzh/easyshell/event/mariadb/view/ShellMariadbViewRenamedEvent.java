package cn.oyzh.easyshell.event.mariadb.view;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB视图已重命名事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 新视图名称
     */
    private String newViewName;

    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
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

    public ShellMariadbDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMariadbDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
