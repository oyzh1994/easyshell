package cn.oyzh.easyshell.event.mariadb.function;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.function.ShellMariadbFunctionTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB函数已删除事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbFunctionDroppedEvent extends Event<ShellMariadbFunctionTreeItem>   {

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().functionName();
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
