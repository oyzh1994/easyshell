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
 * MySQL创建表参数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlCreateTableParam {

    /**
     * 表
     */
    private MysqlTable table;

    /**
     * 检查约束集合
     */
    private DBObjects<MysqlCheck> checks;

    /**
     * 字段集合
     */
    private MysqlColumns columns;

    /**
     * 索引集合
     */
    private DBObjects<MysqlIndex> indexes;

    /**
     * 触发器集合
     */
    private DBObjects<MysqlTrigger> triggers;

    /**
     * 外键集合
     */
    private DBObjects<MysqlForeignKey> foreignKeys;

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
    public List<MysqlColumn> primaryKeys() {
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
    public MysqlTable getTable() {
        return table;
    }

    /**
     * 设置表
     *
     * @param table 表
     */
    public void setTable(MysqlTable table) {
        this.table = table;
    }

    /**
     * 获取检查约束集合
     *
     * @return 检查约束集合
     */
    public DBObjects<MysqlCheck> getChecks() {
        return checks;
    }

    /**
     * 设置检查约束集合
     *
     * @param checks 检查约束集合
     */
    public void setChecks(DBObjects<MysqlCheck> checks) {
        this.checks = checks;
    }

    /**
     * 获取字段集合
     *
     * @return 字段集合
     */
    public MysqlColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段集合
     *
     * @param columns 字段集合
     */
    public void setColumns(MysqlColumns columns) {
        this.columns = columns;
    }

    /**
     * 获取索引集合
     *
     * @return 索引集合
     */
    public DBObjects<MysqlIndex> getIndexes() {
        return indexes;
    }

    /**
     * 设置索引集合
     *
     * @param indexes 索引集合
     */
    public void setIndexes(DBObjects<MysqlIndex> indexes) {
        this.indexes = indexes;
    }

    /**
     * 获取触发器集合
     *
     * @return 触发器集合
     */
    public DBObjects<MysqlTrigger> getTriggers() {
        return triggers;
    }

    /**
     * 设置触发器集合
     *
     * @param triggers 触发器集合
     */
    public void setTriggers(DBObjects<MysqlTrigger> triggers) {
        this.triggers = triggers;
    }

    /**
     * 获取外键集合
     *
     * @return 外键集合
     */
    public DBObjects<MysqlForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    /**
     * 设置外键集合
     *
     * @param foreignKeys 外键集合
     */
    public void setForeignKeys(DBObjects<MysqlForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
