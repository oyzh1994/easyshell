package cn.oyzh.easyshell.event.dameng.table;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/17
 */
public class ShellDamengTableAlertedEvent extends Event<String> {

    private ShellDamengSchemaTreeItem dbItem;

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
