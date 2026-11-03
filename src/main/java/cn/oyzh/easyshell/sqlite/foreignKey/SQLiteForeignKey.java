package cn.oyzh.easyshell.sqlite.foreignKey;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.fx.db.DBForeignKey;
import cn.oyzh.fx.db.DBObject;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SQLite外键。
 *
 * @since 2026-11-02
 */
public class SQLiteForeignKey extends DBObject implements DBForeignKey, ObjectCopier<SQLiteForeignKey> {

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 数据表名称
     */
    private String tableName;

    /**
     * 外键名称
     */
    private String name;

    /**
     * 外键字段列表
     */
    private Set<String> columns = new HashSet<>();

    /**
     * 引用表名称
     */
    private SimpleStringProperty primaryKeyTableProperty;

    /**
     * 引用字段列表
     */
    private Set<String> primaryKeyColumns = new HashSet<>();

    /**
     * 外键删除策略
     */
    private String deletePolicy = "NO ACTION";

    /**
     * 外键更新策略
     */
    private String updatePolicy = "NO ACTION";

    public SQLiteForeignKey() {
    }

    public SQLiteForeignKey(String name) {
        this.setName(name);
    }

    /**
     * 获取原始名称。
     *
     * @return 原始名称
     */
    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    /**
     * 获取引用表名称属性。
     *
     * @return 引用表名称属性
     */
    public SimpleStringProperty primaryKeyTableProperty() {
        if (this.primaryKeyTableProperty == null) {
            this.primaryKeyTableProperty = new SimpleStringProperty();
        }
        return this.primaryKeyTableProperty;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        super.putOriginalData("dbName", dbName);
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.putOriginalData("tableName", tableName);
    }

    /**
     * 设置删除策略。
     *
     * @param deletePolicy 删除策略
     */
    public void setDeletePolicy(String deletePolicy) {
        this.deletePolicy = deletePolicy;
        super.putOriginalData("deletePolicy", deletePolicy);
    }

    /**
     * 设置更新策略。
     *
     * @param updatePolicy 更新策略
     */
    public void setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
        super.putOriginalData("updatePolicy", updatePolicy);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    @Override
    public void setColumns(Set<String> columns) {
        this.columns = columns == null ? new HashSet<>() : columns;
        super.putOriginalData("columns", this.columns);
    }

    /**
     * 设置引用表名称。
     *
     * @param primaryKeyTable 引用表名称
     */
    public void setPrimaryKeyTable(String primaryKeyTable) {
        this.primaryKeyTableProperty().set(primaryKeyTable);
        super.putOriginalData("primaryKeyTable", primaryKeyTable);
    }

    /**
     * 获取引用表名称。
     *
     * @return 引用表名称
     */
    public String getPrimaryKeyTable() {
        return this.primaryKeyTableProperty == null ? null : this.primaryKeyTableProperty.get();
    }

    /**
     * 设置引用字段列表。
     *
     * @param primaryKeyColumns 引用字段列表
     */
    public void setPrimaryKeyColumns(Set<String> primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns == null ? new HashSet<>() : primaryKeyColumns;
        super.putOriginalData("primaryKeyColumns", this.primaryKeyColumns);
    }

    /**
     * 添加引用字段。
     *
     * @param columnName 字段名称
     */
    public void addPrimaryKeyColumn(String columnName) {
        if (this.primaryKeyColumns == null) {
            this.setPrimaryKeyColumns(new HashSet<>());
        }
        this.primaryKeyColumns.add(columnName);
    }

    @Override
    public void copy(SQLiteForeignKey foreignKey) {
        if (foreignKey != null) {
            this.setDbName(foreignKey.getDbName());
            this.setTableName(foreignKey.getTableName());
            this.setName(foreignKey.getName());
            this.setDeletePolicy(foreignKey.getDeletePolicy());
            this.setUpdatePolicy(foreignKey.getUpdatePolicy());
            this.setColumns(new HashSet<>(foreignKey.getColumns()));
            this.setPrimaryKeyTable(foreignKey.getPrimaryKeyTable());
            this.setPrimaryKeyColumns(new HashSet<>(foreignKey.getPrimaryKeyColumns()));
        }
    }

    @Override
    public boolean isInvalid() {
        return DBForeignKey.super.isInvalid() || CollectionUtil.isEmpty(this.columns)
                || CollectionUtil.isEmpty(this.primaryKeyColumns)
                || StringUtil.isBlank(this.getPrimaryKeyTable());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getColumns() {
        return this.columns;
    }

    /**
     * 获取删除策略。
     *
     * @return 删除策略
     */
    public String getDeletePolicy() {
        return this.deletePolicy;
    }

    /**
     * 获取更新策略。
     *
     * @return 更新策略
     */
    public String getUpdatePolicy() {
        return this.updatePolicy;
    }

    /**
     * 获取引用字段列表。
     *
     * @return 引用字段列表
     */
    public Set<String> getPrimaryKeyColumns() {
        return this.primaryKeyColumns;
    }

    public String toConstraintSql() {
        StringBuilder sql = new StringBuilder();
        if (StringUtil.isNotBlank(this.name)) {
            sql.append("CONSTRAINT ").append(SQLiteGeneratorUtil.quote(this.name)).append(' ');
        }
        sql.append("FOREIGN KEY (").append(this.columns.stream().map(SQLiteGeneratorUtil::quote)
                        .collect(java.util.stream.Collectors.joining(", "))).append(") REFERENCES ")
                .append(SQLiteGeneratorUtil.quote(this.getPrimaryKeyTable()));
        if (!this.primaryKeyColumns.isEmpty()) {
            sql.append(" (").append(this.primaryKeyColumns.stream().map(SQLiteGeneratorUtil::quote)
                    .collect(java.util.stream.Collectors.joining(", "))).append(')');
        }
        if (StringUtil.isNotBlank(this.deletePolicy)) {
            sql.append(" ON DELETE ").append(this.deletePolicy);
        }
        if (StringUtil.isNotBlank(this.updatePolicy)) {
            sql.append(" ON UPDATE ").append(this.updatePolicy);
        }
        return sql.toString();
    }

    @Override
    public void destroy() {
        if (this.primaryKeyTableProperty != null) {
            this.primaryKeyTableProperty.unbind();
        }
        if (this.columns != null) {
            this.columns.clear();
        }
        if (this.primaryKeyColumns != null) {
            this.primaryKeyColumns.clear();
        }
        super.destroy();
    }
}
