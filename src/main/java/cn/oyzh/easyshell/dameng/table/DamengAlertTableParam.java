package cn.oyzh.easyshell.dameng.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.check.DamengCheck;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKey;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.fx.db.DBObjects;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengAlertTableParam {

    private DamengTable table;

    private DBObjects<DamengCheck> checks;

    private DamengColumns columns;

    private DBObjects<DamengIndex> indexes;

    private DBObjects<DamengTrigger> triggers;

    private DBObjects<DamengForeignKey> foreignKeys;

    // private DamengPrimaryKeys primaryKeys;

    /**
     * 是否存在自动递增
     */
    private boolean existAutoIncrement;

    /**
     * 主键列表
     */
    private List<String> primaryKeys;

    public boolean hasColumns() {
        return CollectionUtil.isNotEmpty(this.columns);
    }

    public List<DamengColumn> primaryKeys() {
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

    public boolean primaryKeyChanged() {
        if (this.hasColumns()) {
            for (DamengColumn column : columns) {
                if (column.isPrimaryKeyChanged()) {
                    return true;
                }
                if (column.isCreated() && column.isPrimaryKey()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean columnChanged() {
        if (this.hasColumns()) {
            for (DamengColumn column : this.columns) {
                if (column.isDeleted()) {
                    return true;
                }
                if (column.isCreated()) {
                    return true;
                }
                if (column.isColumnChanged()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String tableName() {
        return this.table.getName();
    }

    public void setTableName(String tableName) {
        this.table.setName(tableName);
    }

    public DamengTable getTable() {
        return table;
    }

    public void setTable(DamengTable table) {
        this.table = table;
    }

    public DBObjects<DamengCheck> getChecks() {
        return checks;
    }

    public void setChecks(DBObjects<DamengCheck> checks) {
        this.checks = checks;
    }

    public DamengColumns getColumns() {
        return columns;
    }

    public void setColumns(DamengColumns columns) {
        this.columns = columns;
    }

    public DBObjects<DamengIndex> getIndexes() {
        return indexes;
    }

    public void setIndexes(DBObjects<DamengIndex> indexes) {
        this.indexes = indexes;
    }

    public DBObjects<DamengTrigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(DBObjects<DamengTrigger> triggers) {
        this.triggers = triggers;
    }

    public DBObjects<DamengForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(DBObjects<DamengForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public boolean isExistAutoIncrement() {
        return this.existAutoIncrement;
    }

    public void setExistAutoIncrement(boolean existAutoIncrement) {
        this.existAutoIncrement = existAutoIncrement;
    }

    public boolean isExistPrimaryKey() {
        return CollectionUtil.isNotEmpty(this.primaryKeys);
    }

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public String getSchema() {
        return this.table.getSchema();
    }
}
