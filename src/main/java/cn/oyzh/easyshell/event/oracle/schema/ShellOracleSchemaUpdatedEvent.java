package cn.oyzh.easyshell.event.oracle.schema;

import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.trees.oracle.root.ShellOracleRootTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle模式已更新事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleSchemaUpdatedEvent extends Event<OracleSchema> {

    /**
     * 连接节点
     */
    private ShellOracleRootTreeItem connectItem;

    public ShellOracleRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellOracleRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
