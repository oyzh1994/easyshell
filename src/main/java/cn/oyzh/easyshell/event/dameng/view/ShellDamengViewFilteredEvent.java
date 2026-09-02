package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.event.Event;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/06/26
 */
public class ShellDamengViewFilteredEvent extends Event<ShellDamengViewTreeItem> {

    private List<DamengRecordFilter> filters;

    private ShellDamengSchemaTreeItem dbItem;

    public String viewName() {
        return this.data().viewName();
    }

    public List<DamengRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<DamengRecordFilter> filters) {
        this.filters = filters;
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
