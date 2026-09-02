package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class DamengViewDesignEvent extends Event<DamengView> {

    private DamengSchemaTreeItem dbItem;

    public String viewName() {
        return this.data().getName();
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
