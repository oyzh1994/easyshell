package cn.oyzh.easyshell.sqlite.foreignKey;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.fx.db.DBForeignKey;
import cn.oyzh.fx.db.DBObject;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 外键约束。
 *
 * @since 2026-11-02
 */
public class SQLiteForeignKey extends DBObject implements DBForeignKey, ObjectCopier<SQLiteForeignKey> {

    private String dbName;

    private String tableName;

    private String name;

    private List<String> columns = new ArrayList<>();

    private String referencedTable;

    private List<String> referencedColumns = new ArrayList<>();

    private String deletePolicy = "NO ACTION";

    private String updatePolicy = "NO ACTION";

    public SQLiteForeignKey() {
    }

    public SQLiteForeignKey(String name) {
        this.setName(name);
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public List<String> getColumns() {
        return this.columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns == null ? new ArrayList<>() : columns;
        super.putOriginalData("columns", this.columns);
    }

    public String getReferencedTable() {
        return this.referencedTable;
    }

    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable;
        super.putOriginalData("referencedTable", referencedTable);
    }

    public List<String> getReferencedColumns() {
        return this.referencedColumns;
    }

    public void setReferencedColumns(List<String> referencedColumns) {
        this.referencedColumns = referencedColumns == null ? new ArrayList<>() : referencedColumns;
        super.putOriginalData("referencedColumns", this.referencedColumns);
    }

    public String getDeletePolicy() {
        return this.deletePolicy;
    }

    public void setDeletePolicy(String deletePolicy) {
        this.deletePolicy = deletePolicy;
        super.putOriginalData("deletePolicy", deletePolicy);
    }

    public String getUpdatePolicy() {
        return this.updatePolicy;
    }

    public void setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
        super.putOriginalData("updatePolicy", updatePolicy);
    }

    public String columnsText() {
        return String.join(", ", this.columns);
    }

    public String referencedColumnsText() {
        return String.join(", ", this.referencedColumns);
    }

    @Override
    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || this.columns.isEmpty()
                || StringUtil.isBlank(this.referencedTable);
    }

    @Override
    public void copy(SQLiteForeignKey foreignKey) {
        if (foreignKey != null) {
            this.setDbName(foreignKey.getDbName());
            this.setTableName(foreignKey.getTableName());
            this.setName(foreignKey.getName());
            this.setColumns(new ArrayList<>(foreignKey.getColumns()));
            this.setReferencedTable(foreignKey.getReferencedTable());
            this.setReferencedColumns(new ArrayList<>(foreignKey.getReferencedColumns()));
            this.setDeletePolicy(foreignKey.getDeletePolicy());
            this.setUpdatePolicy(foreignKey.getUpdatePolicy());
        }
    }

    public String toConstraintSql() {
        StringBuilder sql = new StringBuilder();
        if (StringUtil.isNotBlank(this.name)) {
            sql.append("CONSTRAINT ").append(SQLiteGeneratorUtil.quote(this.name)).append(' ');
        }
        sql.append("FOREIGN KEY (").append(this.columns.stream().map(SQLiteGeneratorUtil::quote)
                .collect(java.util.stream.Collectors.joining(", "))).append(") REFERENCES ")
                .append(SQLiteGeneratorUtil.quote(this.referencedTable));
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
}
