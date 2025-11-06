package cn.oyzh.easyshell.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.check.MysqlChecks;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKeys;
import cn.oyzh.easyshell.mysql.index.MysqlIndexes;
import cn.oyzh.easyshell.mysql.trigger.MysqlTriggers;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlCreateTableParam {

    private MysqlTable table;

    private MysqlChecks checks;

    private MysqlColumns columns;

    private MysqlIndexes indexes;

    private MysqlTriggers triggers;

    private MysqlForeignKeys foreignKeys;

    public String dbName() {
        return this.table.getDbName();
    }

    public boolean hasColumns() {
        return CollectionUtil.isNotEmpty(this.columns);
    }

    public List<MysqlColumn> primaryKeys() {
        return this.columns.primaryKeys();
    }

    public boolean hasIndex() {
        return CollectionUtil.isNotEmpty(this.indexes);
    }

    public boolean hasForeignKey() {
        return CollectionUtil.isNotEmpty(this.foreignKeys);
    }

    public boolean hasCheck() {
        return CollectionUtil.isNotEmpty(this.checks);
    }

    public boolean hasTrigger() {
        return CollectionUtil.isNotEmpty(this.triggers);
    }

    public String tableName() {
        return this.table.getName();
    }

    public void setTableName(String tableName) {
        this.table.setName(tableName);
    }

    public MysqlTable getTable() {
        return table;
    }

    public void setTable(MysqlTable table) {
        this.table = table;
    }

    public MysqlChecks getChecks() {
        return checks;
    }

    public void setChecks(MysqlChecks checks) {
        this.checks = checks;
    }

    public MysqlColumns getColumns() {
        return columns;
    }

    public void setColumns(MysqlColumns columns) {
        this.columns = columns;
    }

    public MysqlIndexes getIndexes() {
        return indexes;
    }

    public void setIndexes(MysqlIndexes indexes) {
        this.indexes = indexes;
    }

    public MysqlTriggers getTriggers() {
        return triggers;
    }

    public void setTriggers(MysqlTriggers triggers) {
        this.triggers = triggers;
    }

    public MysqlForeignKeys getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(MysqlForeignKeys foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
