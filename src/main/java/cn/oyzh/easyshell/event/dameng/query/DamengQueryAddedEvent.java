package cn.oyzh.easyshell.event.dameng.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class DamengQueryAddedEvent extends Event<ShellQuery> {

    private DamengSchemaTreeItem dbItem;

    public DamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(DamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
