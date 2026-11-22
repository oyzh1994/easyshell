package cn.oyzh.easyshell.event.sqlserver.sequence;

import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.sequence.ShellSqlServerSequenceTreeItem;
import cn.oyzh.event.Event;

/**
 * SqlServer序列已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerSequenceDroppedEvent extends Event<ShellSqlServerSequenceTreeItem>   {

    /**
     * 获取序列名称
     *
     * @return 序列名称
     */
    public String sequenceName() {
        return this.data().sequenceName();
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellSqlServerSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
