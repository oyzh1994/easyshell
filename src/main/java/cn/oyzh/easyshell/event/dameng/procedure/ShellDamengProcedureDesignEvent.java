package cn.oyzh.easyshell.event.dameng.procedure;

import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellDamengProcedureDesignEvent extends Event<DamengProcedure> {

    private ShellDamengSchemaTreeItem dbItem;

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
