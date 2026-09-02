package cn.oyzh.easyshell.event.dameng.table;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTableTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/23
 */
public class ShellDamengTableRenamedEvent extends Event<String> {

    private ShellDamengSchemaTreeItem dbItem;

    private String newTableName;

    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    public String tableName() {
        return this.data();
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
