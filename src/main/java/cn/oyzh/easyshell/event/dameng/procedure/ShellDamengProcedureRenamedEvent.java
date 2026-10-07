package cn.oyzh.easyshell.event.dameng.procedure;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦存储过程已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengProcedureRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

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
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.dbItem.schema();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
