package cn.oyzh.easyshell.event.dameng.sequence;

import cn.oyzh.easyshell.dameng.sequence.DamengSequence;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦序列设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengSequenceDesignEvent extends Event<DamengSequence> {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    /**
     * 获取序列名称
     *
     * @return 序列名称
     */
    public String sequenceName() {
        return this.data().getName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
