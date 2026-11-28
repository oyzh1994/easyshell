package cn.oyzh.easyshell.h2.table;

import cn.oyzh.easyshell.h2.check.H2Checks;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKeys;
import cn.oyzh.easyshell.h2.index.H2Indexes;
import cn.oyzh.easyshell.h2.trigger.H2Triggers;

import java.util.List;

/**
 * H2 修改表参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2AlertTableParam {

    public String dbName() {
        return this.table.getDbName();
    }

    private H2Table table;

    private String comment;

    private H2Checks checks;

    private H2Columns columns;

    private H2Indexes indexes;

    private H2Triggers triggers;

    private H2ForeignKeys foreignKeys;

    private boolean existPrimaryKey;

    public String tableName() {
        return this.table.getName();
    }

    public void setTableName(String tableName) {
        this.table.setName(tableName);
    }

    public boolean hasColumns() {
        return this.columns != null && !this.columns.isEmpty();
    }

    public List<H2Column> primaryKeys() {
        return this.columns.primaryKeys();
    }

    public boolean primaryKeyChanged() {
        return this.hasColumns() && this.columns.primaryKeyChanged();
    }

    public boolean columnChanged() {
        if (this.hasColumns()) {
            for (H2Column column : this.columns) {
                if (column.isDeleted() || column.isCreated() || column.isColumnChanged()) {
                    return true;
                }
            }
        }
        return false;
    }

    public H2Table getTable() {
        return this.table;
    }

    public void setTable(H2Table table) {
        this.table = table;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public boolean isExistPrimaryKey() {
        return this.existPrimaryKey;
    }

    public void setExistPrimaryKey(boolean existPrimaryKey) {
        this.existPrimaryKey = existPrimaryKey;
    }
}
