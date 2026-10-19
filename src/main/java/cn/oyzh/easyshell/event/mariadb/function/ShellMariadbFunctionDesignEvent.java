package cn.oyzh.easyshell.event.mariadb.function;

import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB函数设计事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbFunctionDesignEvent extends Event<MariadbFunction> {

    /**
     * 数据库节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().getName();
    }

    public ShellMariadbDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMariadbDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
