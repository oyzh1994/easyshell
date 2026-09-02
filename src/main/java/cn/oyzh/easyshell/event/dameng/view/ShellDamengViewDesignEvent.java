package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengViewDesignEvent extends Event<DamengView> {

    private ShellDamengSchemaTreeItem dbItem;

    public String viewName() {
        return this.data().getName();
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
