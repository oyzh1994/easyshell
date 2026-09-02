package cn.oyzh.easyshell.event.dameng.schema;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.trees.dameng.root.DBRootTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class DamengSchemaUpdatedEvent extends Event<DamengSchema> implements EventFormatter {

    private DBRootTreeItem connectItem;

    @Override
    public String eventFormat() {
        return String.format("[%s] 数据库已修改", this.data().getName());
    }

    public DBRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(DBRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
