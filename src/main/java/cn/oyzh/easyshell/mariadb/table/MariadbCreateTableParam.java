package cn.oyzh.easyshell.mariadb.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mariadb.check.MariadbCheck;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.foreignKey.MariadbForeignKey;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
import cn.oyzh.easyshell.mariadb.trigger.MariadbTrigger;
import cn.oyzh.fx.db.DBObjects;

import java.util.List;

/**
 * MariaDB创建表参数
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbCreateTableParam {

    /**
     * 表
     */
    private MariadbTable table;

    /**
     * 检查约束集合
     */
    private DBObjects<MariadbCheck> checks;

    /**
     * 字段集合
     */
    private MariadbColumns columns;

    /**
     * 索引集合
     */
    private DBObjects<MariadbIndex> indexes;

    /**
     * 触发器集合
     */
    private DBObjects<MariadbTrigger> triggers;

    /**
     * 外键集合
     */
    private DBObjects<MariadbForeignKey> foreignKeys;

    /**
     * 获取库名称
     *
     * @return 库名称
     */
    public String dbName() {
        return this.table.getDbName();
    }

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
    public List<MariadbColumn> primaryKeys() {
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
    public MariadbTable getTable() {
        return table;
    }

    /**
     * 设置表
     *
     * @param table 表
     */
    public void setTable(MariadbTable table) {
        this.table = table;
    }

    /**
     * 获取检查约束集合
     *
     * @return 检查约束集合
     */
    public DBObjects<MariadbCheck> getChecks() {
        return checks;
    }

    /**
     * 设置检查约束集合
     *
     * @param checks 检查约束集合
     */
    public void setChecks(DBObjects<MariadbCheck> checks) {
        this.checks = checks;
    }

    /**
     * 获取字段集合
     *
     * @return 字段集合
     */
    public MariadbColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段集合
     *
     * @param columns 字段集合
     */
    public void setColumns(MariadbColumns columns) {
        this.columns = columns;
    }

    /**
     * 获取索引集合
     *
     * @return 索引集合
     */
    public DBObjects<MariadbIndex> getIndexes() {
        return indexes;
    }

    /**
     * 设置索引集合
     *
     * @param indexes 索引集合
     */
    public void setIndexes(DBObjects<MariadbIndex> indexes) {
        this.indexes = indexes;
    }

    /**
     * 获取触发器集合
     *
     * @return 触发器集合
     */
    public DBObjects<MariadbTrigger> getTriggers() {
        return triggers;
    }

    /**
     * 设置触发器集合
     *
     * @param triggers 触发器集合
     */
    public void setTriggers(DBObjects<MariadbTrigger> triggers) {
        this.triggers = triggers;
    }

    /**
     * 获取外键集合
     *
     * @return 外键集合
     */
    public DBObjects<MariadbForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    /**
     * 设置外键集合
     *
     * @param foreignKeys 外键集合
     */
    public void setForeignKeys(DBObjects<MariadbForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
