package cn.oyzh.easyshell.event.dameng.sequence;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.sequence.ShellDamengSequenceTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦序列已删除事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengSequenceDroppedEvent extends Event<ShellDamengSequenceTreeItem>   {

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
    public ShellDamengSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
