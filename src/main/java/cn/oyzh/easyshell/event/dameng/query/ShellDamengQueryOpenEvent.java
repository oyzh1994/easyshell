package cn.oyzh.easyshell.event.dameng.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengQueryOpenEvent extends Event<ShellQuery> {

    private ShellDamengSchemaTreeItem dbItem;

    public String queryId() {
        return this.data().getUid();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
