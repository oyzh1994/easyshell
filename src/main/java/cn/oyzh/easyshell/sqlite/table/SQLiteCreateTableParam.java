package cn.oyzh.easyshell.sqlite.table;

import cn.oyzh.easyshell.sqlite.check.SQLiteChecks;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.foreignKey.SQLiteForeignKeys;
import cn.oyzh.easyshell.sqlite.index.SQLiteIndexes;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTriggers;

/**
 * SQLite 创建表参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteCreateTableParam {

    public String dbName() {
        return this.table.getDbName();
    }

    private SQLiteTable table;

    private SQLiteChecks checks;

    private SQLiteColumns columns;

    private SQLiteIndexes indexes;

    private SQLiteTriggers triggers;

    private SQLiteForeignKeys foreignKeys;

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

    public SQLiteTable getTable() {
        return this.table;
    }

    public void setTable(SQLiteTable table) {
        this.table = table;
    }

    public SQLiteChecks getChecks() {
        return this.checks;
    }

    public void setChecks(SQLiteChecks checks) {
        this.checks = checks;
    }

    public SQLiteColumns getColumns() {
        return this.columns;
    }

    public void setColumns(SQLiteColumns columns) {
        this.columns = columns;
    }

    public SQLiteIndexes getIndexes() {
        return this.indexes;
    }

    public void setIndexes(SQLiteIndexes indexes) {
        this.indexes = indexes;
    }

    public SQLiteTriggers getTriggers() {
        return this.triggers;
    }

    public void setTriggers(SQLiteTriggers triggers) {
        this.triggers = triggers;
    }

    public SQLiteForeignKeys getForeignKeys() {
        return this.foreignKeys;
    }

    public void setForeignKeys(SQLiteForeignKeys foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
