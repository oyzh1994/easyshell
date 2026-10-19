package cn.oyzh.easyshell.event.mariadb.view;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB视图已删除事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewDroppedEvent extends Event<ShellMariadbViewTreeItem>   {

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data().viewName();
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
