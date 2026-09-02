package cn.oyzh.easyshell.event.dameng.procedure;

import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.ShellDamengProcedureTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellDamengProcedureDroppedEvent extends Event<ShellDamengProcedureTreeItem>   {

    public String procedureName() {
        return this.data().procedureName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
