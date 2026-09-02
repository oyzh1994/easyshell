package cn.oyzh.easyshell.event.dameng.table;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTableTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/24
 */
public class ShellDamengTableDroppedEvent extends Event<ShellDamengTableTreeItem> {

    private ShellDamengSchemaTreeItem dbItem;

    public String tableName() {
        return this.data().tableName();
    }

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
