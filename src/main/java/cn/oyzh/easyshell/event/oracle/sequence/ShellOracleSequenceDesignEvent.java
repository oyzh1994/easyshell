package cn.oyzh.easyshell.event.oracle.sequence;

import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle序列设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleSequenceDesignEvent extends Event<OracleSequence> {

    /**
     * 数据库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 获取序列名称
     *
     * @return 序列名称
     */
    public String sequenceName() {
        return this.data().getName();
    }

    public ShellOracleSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
