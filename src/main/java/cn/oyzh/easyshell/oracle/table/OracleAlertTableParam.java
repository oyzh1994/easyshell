package cn.oyzh.easyshell.oracle.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.oracle.check.OracleCheck;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.foreignKey.OracleForeignKey;
import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.easyshell.oracle.trigger.OracleTrigger;
import cn.oyzh.fx.db.DBObjects;

import java.util.List;

/**
 * Oracle修改表参数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class OracleAlertTableParam {

    /**
     * 表
     */
    private OracleTable table;

    /**
     * 检查约束集合
     */
    private DBObjects<OracleCheck> checks;

    /**
     * 字段集合
     */
    private OracleColumns columns;

    /**
     * 索引集合
     */
    private DBObjects<OracleIndex> indexes;

    /**
     * 触发器集合
     */
    private DBObjects<OracleTrigger> triggers;

    /**
     * 外键集合
     */
    private DBObjects<OracleForeignKey> foreignKeys;

    // private OraclePrimaryKeys primaryKeys;

    /**
     * 是否存在自动递增
     */
    private boolean existAutoIncrement;

    /**
     * 主键列表
     */
    private List<String> primaryKeys;

    /**
     * 是否包含字段
     *
     * @return 结果
     */
    public boolean hasColumns() {
        return CollectionUtil.isNotEmpty(this.columns);
    }

    /**
     * 获取主键字段列表
     *
     * @return 主键字段列表
     */
    public List<OracleColumn> primaryKeys() {
        return this.columns.primaryKeys();
    }

    /**
     * 是否包含索引
     *
     * @return 结果
     */
    public boolean hasIndex() {
        return CollectionUtil.isNotEmpty(this.indexes);
    }

    /**
     * 是否包含外键
     *
     * @return 结果
     */
    public boolean hasForeignKey() {
        return CollectionUtil.isNotEmpty(this.foreignKeys);
    }

    /**
     * 是否包含检查约束
     *
     * @return 结果
     */
    public boolean hasCheck() {
        return CollectionUtil.isNotEmpty(this.checks);
    }

    /**
     * 是否包含触发器
     *
     * @return 结果
     */
    public boolean hasTrigger() {
        return CollectionUtil.isNotEmpty(this.triggers);
    }

    /**
     * 主键是否变更
     *
     * @return 结果
     */
    public boolean primaryKeyChanged() {
        if (this.hasColumns()) {
            for (OracleColumn column : columns) {
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

    /**
     * 字段是否变更
     *
     * @return 结果
     */
    public boolean columnChanged() {
        if (this.hasColumns()) {
            for (OracleColumn column : this.columns) {
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

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.table.getName();
    }

    /**
     * 设置表名称
     *
     * @param tableName 表名称
     */
    public void setTableName(String tableName) {
        this.table.setName(tableName);
    }

    /**
     * 获取表
     *
     * @return 表
     */
    public OracleTable getTable() {
        return table;
    }

    /**
     * 设置表
     *
     * @param table 表
     */
    public void setTable(OracleTable table) {
        this.table = table;
    }

    /**
     * 获取检查约束集合
     *
     * @return 检查约束集合
     */
    public DBObjects<OracleCheck> getChecks() {
        return checks;
    }

    /**
     * 设置检查约束集合
     *
     * @param checks 检查约束集合
     */
    public void setChecks(DBObjects<OracleCheck> checks) {
        this.checks = checks;
    }

    /**
     * 获取字段集合
     *
     * @return 字段集合
     */
    public OracleColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段集合
     *
     * @param columns 字段集合
     */
    public void setColumns(OracleColumns columns) {
        this.columns = columns;
    }

    /**
     * 获取索引集合
     *
     * @return 索引集合
     */
    public DBObjects<OracleIndex> getIndexes() {
        return indexes;
    }

    /**
     * 设置索引集合
     *
     * @param indexes 索引集合
     */
    public void setIndexes(DBObjects<OracleIndex> indexes) {
        this.indexes = indexes;
    }

    /**
     * 获取触发器集合
     *
     * @return 触发器集合
     */
    public DBObjects<OracleTrigger> getTriggers() {
        return triggers;
    }

    /**
     * 设置触发器集合
     *
     * @param triggers 触发器集合
     */
    public void setTriggers(DBObjects<OracleTrigger> triggers) {
        this.triggers = triggers;
    }

    /**
     * 获取外键集合
     *
     * @return 外键集合
     */
    public DBObjects<OracleForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    /**
     * 设置外键集合
     *
     * @param foreignKeys 外键集合
     */
    public void setForeignKeys(DBObjects<OracleForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    /**
     * 是否存在自动递增
     *
     * @return 结果
     */
    public boolean isExistAutoIncrement() {
        return this.existAutoIncrement;
    }

    /**
     * 设置是否存在自动递增
     *
     * @param existAutoIncrement 是否存在自动递增
     */
    public void setExistAutoIncrement(boolean existAutoIncrement) {
        this.existAutoIncrement = existAutoIncrement;
    }

    /**
     * 是否存在主键
     *
     * @return 结果
     */
    public boolean isExistPrimaryKey() {
        return CollectionUtil.isNotEmpty(this.primaryKeys);
    }

    /**
     * 设置主键列表
     *
     * @param primaryKeys 主键列表
     */
    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    /**
     * 获取主键列表
     *
     * @return 主键列表
     */
    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String getSchema() {
        return this.table.getSchema();
    }
}
