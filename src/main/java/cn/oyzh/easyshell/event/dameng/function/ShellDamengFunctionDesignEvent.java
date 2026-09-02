package cn.oyzh.easyshell.event.dameng.function;

import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellDamengFunctionDesignEvent extends Event<DamengFunction> {

    private ShellDamengSchemaTreeItem dbItem;

    public String functionName() {
        return this.data().getName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
