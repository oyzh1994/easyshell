package cn.oyzh.easyshell.event.dameng.procedure;

import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦存储过程设计事件
 *
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellDamengProcedureDesignEvent extends Event<DamengProcedure> {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
        return this.data().getName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
