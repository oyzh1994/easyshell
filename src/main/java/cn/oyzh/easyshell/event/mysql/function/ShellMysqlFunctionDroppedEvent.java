package cn.oyzh.easyshell.event.mysql.function;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.ShellMysqlFunctionTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql函数已删除事件
 *
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMysqlFunctionDroppedEvent extends Event<ShellMysqlFunctionTreeItem>   {

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
    public ShellMysqlDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
