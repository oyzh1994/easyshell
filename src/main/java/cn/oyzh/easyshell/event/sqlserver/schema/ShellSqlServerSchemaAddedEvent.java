package cn.oyzh.easyshell.event.sqlserver.schema;

import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.trees.sqlserver.root.ShellSqlServerRootTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer模式已新增事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerSchemaAddedEvent extends Event<SqlServerSchema> {

    /**
     * 连接节点
     */
    private ShellSqlServerRootTreeItem connectItem;

    public ShellSqlServerRootTreeItem getConnectItem() {
        return connectItem;
    }

    public void setConnectItem(ShellSqlServerRootTreeItem connectItem) {
        this.connectItem = connectItem;
    }
}
