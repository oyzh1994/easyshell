package cn.oyzh.easyshell.event.dameng.function;

import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellDamengFunctionDroppedEvent extends Event<ShellDamengFunctionTreeItem>   {

    public String functionName() {
        return this.data().functionName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
