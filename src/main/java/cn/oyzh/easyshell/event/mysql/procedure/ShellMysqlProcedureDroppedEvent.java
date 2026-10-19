package cn.oyzh.easyshell.event.mysql.procedure;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.ShellMysqlProcedureTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql存储过程已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlProcedureDroppedEvent extends Event<ShellMysqlProcedureTreeItem>   {

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
        return this.data().procedureName();
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
