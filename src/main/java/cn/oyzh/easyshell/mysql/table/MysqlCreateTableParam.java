package cn.oyzh.easyshell.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.check.MysqlCheck;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKey;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.fx.db.DBObjects;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlCreateTableParam {

    private MysqlTable table;

    private DBObjects<MysqlCheck> checks;

    private MysqlColumns columns;

    private DBObjects<MysqlIndex> indexes;

    private DBObjects<MysqlTrigger> triggers;

    private DBObjects<MysqlForeignKey> foreignKeys;

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

    public DBObjects<MysqlCheck> getChecks() {
        return checks;
    }

    public void setChecks(DBObjects<MysqlCheck> checks) {
        this.checks = checks;
    }

    public MysqlColumns getColumns() {
        return columns;
    }

    public void setColumns(MysqlColumns columns) {
        this.columns = columns;
    }

    public DBObjects<MysqlIndex> getIndexes() {
        return indexes;
    }

    public void setIndexes(DBObjects<MysqlIndex> indexes) {
        this.indexes = indexes;
    }

    public DBObjects<MysqlTrigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(DBObjects<MysqlTrigger> triggers) {
        this.triggers = triggers;
    }

    public DBObjects<MysqlForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(DBObjects<MysqlForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
