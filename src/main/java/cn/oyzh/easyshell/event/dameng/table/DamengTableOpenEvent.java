package cn.oyzh.easyshell.event.dameng.table;

import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.DamengTableTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class DamengTableOpenEvent extends Event<DamengTableTreeItem> {

    private DamengSchemaTreeItem dbItem;

    public String tableName() {
        return this.data().tableName();
    }

    public String schema() {
        return this.dbItem.schema();
    }

    public DamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(DamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
