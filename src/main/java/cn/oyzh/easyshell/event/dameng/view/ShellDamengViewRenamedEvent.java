package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/23
 */
public class ShellDamengViewRenamedEvent extends Event<String> {

    private ShellDamengSchemaTreeItem dbItem;

    private String newViewName;

    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    public String viewName() {
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
