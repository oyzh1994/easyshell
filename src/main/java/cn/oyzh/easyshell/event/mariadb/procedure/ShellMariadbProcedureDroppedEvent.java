package cn.oyzh.easyshell.event.mariadb.procedure;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.procedure.ShellMariadbProcedureTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB存储过程已删除事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbProcedureDroppedEvent extends Event<ShellMariadbProcedureTreeItem>   {

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
    public ShellMariadbDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
