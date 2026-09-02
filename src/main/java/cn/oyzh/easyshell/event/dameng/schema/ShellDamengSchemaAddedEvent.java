package cn.oyzh.easyshell.event.dameng.schema;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.trees.dameng.root.ShellDamengRootTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellDamengSchemaAddedEvent extends Event<DamengSchema> implements EventFormatter {

    private ShellDamengRootTreeItem connectItem;

    @Override
    public String eventFormat() {
        return String.format("[%s] 数据库已新增", this.data().getName());
    }

    public ShellDamengRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellDamengRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
