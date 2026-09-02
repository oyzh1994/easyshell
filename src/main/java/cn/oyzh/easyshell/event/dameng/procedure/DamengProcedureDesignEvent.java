package cn.oyzh.easyshell.event.dameng.procedure;

import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class DamengProcedureDesignEvent extends Event<DamengProcedure> {

    private DamengSchemaTreeItem dbItem;

    public String procedureName() {
        return this.data().getName();
    }

    public DamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(DamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
