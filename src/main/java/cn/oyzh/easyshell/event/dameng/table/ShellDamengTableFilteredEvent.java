package cn.oyzh.easyshell.event.dameng.table;

import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTableTreeItem;
import cn.oyzh.event.Event;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/06/26
 */
public class ShellDamengTableFilteredEvent extends Event<ShellDamengTableTreeItem> {

    private List<DamengRecordFilter> filters;

    private ShellDamengSchemaTreeItem dbItem;

    public String tableName() {
        return this.data().tableName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    public List<DamengRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<DamengRecordFilter> filters) {
        this.filters = filters;
    }
}
