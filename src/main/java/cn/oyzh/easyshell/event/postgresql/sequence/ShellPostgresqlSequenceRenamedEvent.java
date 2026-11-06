package cn.oyzh.easyshell.event.postgresql.sequence;

import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL序列已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlSequenceRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 新序列名称
     */
    private String newSequenceName;

    public String getNewSequenceName() {
        return newSequenceName;
    }

    public void setNewSequenceName(String newSequenceName) {
        this.newSequenceName = newSequenceName;
    }

    /**
     * 获取序列名称
     *
     * @return 序列名称
     */
    public String sequenceName() {
        return this.data();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.dbItem.schema();
    }

    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
