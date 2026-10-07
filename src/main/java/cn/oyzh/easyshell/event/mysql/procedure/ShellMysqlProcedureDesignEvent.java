package cn.oyzh.easyshell.event.mysql.procedure;

import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql存储过程设计事件
 *
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMysqlProcedureDesignEvent extends Event<MysqlProcedure> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
        return this.data().getName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
