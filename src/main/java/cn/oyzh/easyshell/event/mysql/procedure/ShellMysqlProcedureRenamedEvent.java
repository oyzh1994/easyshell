package cn.oyzh.easyshell.event.mysql.procedure;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql存储过程已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlProcedureRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 新存储过程名称
     */
    private String newProcedureName;

    public String getNewProcedureName() {
        return newProcedureName;
    }

    public void setNewProcedureName(String newProcedureName) {
        this.newProcedureName = newProcedureName;
    }

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
        return this.data();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem.dbName();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
