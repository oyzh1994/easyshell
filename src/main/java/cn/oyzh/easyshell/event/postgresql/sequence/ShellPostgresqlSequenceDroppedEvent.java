package cn.oyzh.easyshell.event.postgresql.sequence;

import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.trees.postgresql.sequence.ShellPostgresqlSequenceTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL序列已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlSequenceDroppedEvent extends Event<ShellPostgresqlSequenceTreeItem>   {

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
    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
