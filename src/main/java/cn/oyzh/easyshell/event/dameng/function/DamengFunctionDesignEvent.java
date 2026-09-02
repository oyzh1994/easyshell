package cn.oyzh.easyshell.event.dameng.function;

import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class DamengFunctionDesignEvent extends Event<DamengFunction> {

    private DamengSchemaTreeItem dbItem;

    public String functionName() {
        return this.data().getName();
    }

    public DamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(DamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
