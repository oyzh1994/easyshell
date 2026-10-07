package cn.oyzh.easyshell.event.mysql.function;

import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql函数设计事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlFunctionDesignEvent extends Event<MysqlFunction> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().getName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
