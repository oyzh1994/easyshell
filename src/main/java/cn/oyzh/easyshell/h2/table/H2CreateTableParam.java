package cn.oyzh.easyshell.h2.table;

import cn.oyzh.easyshell.h2.check.H2Checks;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKeys;
import cn.oyzh.easyshell.h2.index.H2Indexes;
import cn.oyzh.easyshell.h2.trigger.H2Triggers;

/**
 * H2 创建表参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2CreateTableParam {

    public String dbName() {
        return this.table.getDbName();
    }

    private H2Table table;

    private H2Checks checks;

    private H2Columns columns;

    private H2Indexes indexes;

    private H2Triggers triggers;

    private H2ForeignKeys foreignKeys;

    public String tableName() {
        return this.table.getName();
    }

    public void setTableName(String tableName) {
        this.table.setName(tableName);
    }

    public boolean hasColumns() {
        return this.columns != null && !this.columns.isEmpty();
    }

    public boolean hasIndex() {
        return this.indexes != null && !this.indexes.isEmpty();
    }

    public boolean hasForeignKey() {
        return this.foreignKeys != null && !this.foreignKeys.isEmpty();
    }

    public boolean hasCheck() {
        return this.checks != null && !this.checks.isEmpty();
    }

    public boolean hasTrigger() {
        return this.triggers != null && !this.triggers.isEmpty();
    }

    public H2Table getTable() {
        return this.table;
    }

    public void setTable(H2Table table) {
        this.table = table;
    }

    public H2Checks getChecks() {
        return this.checks;
    }

    public void setChecks(H2Checks checks) {
        this.checks = checks;
    }

    public H2Columns getColumns() {
        return this.columns;
    }

    public void setColumns(H2Columns columns) {
        this.columns = columns;
    }

    public H2Indexes getIndexes() {
        return this.indexes;
    }

    public void setIndexes(H2Indexes indexes) {
        this.indexes = indexes;
    }

    public H2Triggers getTriggers() {
        return this.triggers;
    }

    public void setTriggers(H2Triggers triggers) {
        this.triggers = triggers;
    }

    public H2ForeignKeys getForeignKeys() {
        return this.foreignKeys;
    }

    public void setForeignKeys(H2ForeignKeys foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
