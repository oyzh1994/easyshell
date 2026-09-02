package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.DamengViewTreeItem;
import cn.oyzh.event.Event;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/06/26
 */
public class DamengViewFilteredEvent extends Event<DamengViewTreeItem> {

    private List<DamengRecordFilter> filters;

    private DamengSchemaTreeItem dbItem;

    public String viewName() {
        return this.data().viewName();
    }

    public List<DamengRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<DamengRecordFilter> filters) {
        this.filters = filters;
    }

    public DamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(DamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
