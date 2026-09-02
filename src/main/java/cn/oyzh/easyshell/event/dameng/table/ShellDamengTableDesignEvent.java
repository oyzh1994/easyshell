package cn.oyzh.easyshell.event.dameng.table;

import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/08/07
 */
public class ShellDamengTableDesignEvent extends Event<DamengTable> {

    private ShellDamengSchemaTreeItem dbItem;

    public String schema() {
        return this.dbItem.schema();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    public String tableName() {
        return this.data().getName();
    }
}
