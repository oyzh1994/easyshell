package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengViewOpenEvent extends Event<ShellDamengViewTreeItem> {

    private ShellDamengSchemaTreeItem dbItem;

    public String viewName() {
        return this.data().viewName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
