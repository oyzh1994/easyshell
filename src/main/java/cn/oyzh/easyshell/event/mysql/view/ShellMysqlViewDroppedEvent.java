package cn.oyzh.easyshell.event.mysql.view;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql视图已删除事件
 *
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlViewDroppedEvent extends Event<ShellMysqlViewTreeItem>   {

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
    public ShellMysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
