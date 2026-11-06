package cn.oyzh.easyshell.event.postgresql.sequence;

import cn.oyzh.easyshell.postgresql.sequence.PostgresqlSequence;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL序列设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlSequenceDesignEvent extends Event<PostgresqlSequence> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 获取序列名称
     *
     * @return 序列名称
     */
    public String sequenceName() {
        return this.data().getName();
    }

    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
