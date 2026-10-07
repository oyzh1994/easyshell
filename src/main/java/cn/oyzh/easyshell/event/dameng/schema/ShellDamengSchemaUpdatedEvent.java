package cn.oyzh.easyshell.event.dameng.schema;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.trees.dameng.root.ShellDamengRootTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦模式已更新事件
 *
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellDamengSchemaUpdatedEvent extends Event<DamengSchema> {

    /**
     * 连接节点
     */
    private ShellDamengRootTreeItem connectItem;

    public ShellDamengRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellDamengRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
