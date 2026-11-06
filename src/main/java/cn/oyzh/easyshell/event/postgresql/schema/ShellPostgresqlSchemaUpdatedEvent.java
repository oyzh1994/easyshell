package cn.oyzh.easyshell.event.postgresql.schema;

import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.trees.postgresql.root.ShellPostgresqlRootTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL模式已更新事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlSchemaUpdatedEvent extends Event<PostgresqlSchema> {

    /**
     * 连接节点
     */
    private ShellPostgresqlRootTreeItem connectItem;

    public ShellPostgresqlRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellPostgresqlRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
