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
    private SimpleStringProperty referencedTableProperty;

    /**
     * 引用字段列表
     */
    private Set<String> referencedColumns = new HashSet<>();

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
    public SimpleStringProperty referencedTableProperty() {
        if (this.referencedTableProperty == null) {
            this.referencedTableProperty = new SimpleStringProperty();
        }
        return this.referencedTableProperty;
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

    @Override
    public String getReferencedDatabase() {
        return null;
    }

    @Override
    public void setReferencedDatabase(String referencedDatabase) {

    }

    @Override
    public void setReferencedTable(String referencedTable) {
        this.referencedTableProperty().set(referencedTable);
        super.putOriginalData("referencedTable", referencedTable);
    }

    @Override
    public String getReferencedTable() {
        return this.referencedTableProperty == null ? null : this.referencedTableProperty.get();
    }

    @Override
    public void setReferencedColumns(Set<String> referencedColumns) {
        this.referencedColumns = referencedColumns == null ? new HashSet<>() : referencedColumns;
        super.putOriginalData("referencedColumns", this.referencedColumns);
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
            this.setReferencedTable(foreignKey.getReferencedTable());
            this.setReferencedColumns(new HashSet<>(foreignKey.getReferencedColumns()));
        }
    }

    @Override
    public boolean isInvalid() {
        return DBForeignKey.super.isInvalid() || CollectionUtil.isEmpty(this.columns)
                || CollectionUtil.isEmpty(this.referencedColumns)
                || StringUtil.isBlank(this.getReferencedTable());
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

    @Override
    public Set<String> getReferencedColumns() {
        return this.referencedColumns;
    }

    public String toConstraintSql() {
        StringBuilder sql = new StringBuilder();
        if (StringUtil.isNotBlank(this.name)) {
            sql.append("CONSTRAINT ").append(SQLiteGeneratorUtil.quote(this.name)).append(' ');
        }
        sql.append("FOREIGN KEY (").append(this.columns.stream().map(SQLiteGeneratorUtil::quote)
                        .collect(java.util.stream.Collectors.joining(", "))).append(") REFERENCES ")
                .append(SQLiteGeneratorUtil.quote(this.getReferencedTable()));
        if (!this.referencedColumns.isEmpty()) {
            sql.append(" (").append(this.referencedColumns.stream().map(SQLiteGeneratorUtil::quote)
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
        if (this.referencedTableProperty != null) {
            this.referencedTableProperty.unbind();
        }
        if (this.columns != null) {
            this.columns.clear();
        }
        if (this.referencedColumns != null) {
            this.referencedColumns.clear();
        }
        super.destroy();
    }
}
