package cn.oyzh.easyshell.event.mariadb.procedure;

import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB存储过程设计事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbProcedureDesignEvent extends Event<MariadbProcedure> {

    /**
     * 数据库节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
        return this.data().getName();
    }

    public ShellMariadbDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMariadbDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
