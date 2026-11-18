package cn.oyzh.easyshell.event.oracle.sequence;

import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.sequence.ShellOracleSequenceTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle序列已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleSequenceDroppedEvent extends Event<ShellOracleSequenceTreeItem>   {

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
    public ShellOracleSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
