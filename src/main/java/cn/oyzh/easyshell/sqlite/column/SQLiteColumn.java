package cn.oyzh.easyshell.sqlite.column;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * SQLite 字段。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteColumn extends DBObject implements DBColumn, ObjectCopier<SQLiteColumn> {

    public static final String KIND_NORMAL = "NORMAL";

    public static final String KIND_GENERATED = "GENERATED";

    private String dbName;

    private String tableName;

    private String name;

    private StringProperty typeProperty;

    private Integer size;

    private Integer position;

    private boolean notNull;

    private BooleanProperty primaryKeyProperty;

    private boolean autoIncrement;

    private String defaultValue;

    private boolean hidden;

    private boolean unique;

    private String collate;

    private String check;

    private boolean generated;

    private String generatedExpression;

    private String generatedStorage;

    public SQLiteColumn() {
    }

    public SQLiteColumn(String name) {
        this.setName(name);
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        super.putOriginalData("dbName", dbName);
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.putOriginalData("tableName", tableName);
    }

    public Integer getPosition() {
        return this.position;
    }

    public void setPosition(Integer position) {
        this.position = position;
        super.putOriginalData("position", position);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public boolean isNameChanged() {
        return super.checkOriginalData("name", this.name);
    }

    @Override
    public String getType() {
        return this.typeProperty().get();
    }

    @Override
    public void setType(String type) {
        this.typeProperty().set(type);
        super.putOriginalData("type", type);
    }

    public boolean isTypeChanged() {
        return super.checkOriginalData("type", this.getType());
    }

    public StringProperty typeProperty() {
        if (this.typeProperty == null) {
            this.typeProperty = new SimpleStringProperty();
        }
        return this.typeProperty;
    }

    @Override
    public Integer getSize() {
        return this.size;
    }

    @Override
    public void setSize(Integer size) {
        this.size = size;
        super.putOriginalData("size", size);
    }

    public boolean isNotNull() {
        return this.notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
        super.putOriginalData("notNull", notNull);
    }

    @Override
    public boolean isPrimaryKey() {
        return this.primaryKeyProperty().get();
    }

    @Override
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKeyProperty().set(primaryKey);
        super.putOriginalData("primaryKey", primaryKey);
    }

    public BooleanProperty primaryKeyProperty() {
        if (this.primaryKeyProperty == null) {
            this.primaryKeyProperty = new SimpleBooleanProperty();
        }
        return this.primaryKeyProperty;
    }

    @Override
    public boolean isAutoIncrement() {
        return this.autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        super.putOriginalData("autoIncrement", autoIncrement);
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        super.putOriginalData("defaultValue", defaultValue);
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        super.putOriginalData("hidden", hidden);
    }

    public boolean isUnique() {
        return this.unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
        super.putOriginalData("unique", unique);
    }

    public String getCollate() {
        return this.collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
        super.putOriginalData("collate", collate);
    }

    public String getCheck() {
        return this.check;
    }

    public void setCheck(String check) {
        this.check = check;
        super.putOriginalData("check", check);
    }

    public String getGeneratedExpression() {
        return this.generatedExpression;
    }

    public void setGeneratedExpression(String generatedExpression) {
        this.generatedExpression = generatedExpression;
        if (generatedExpression != null && !generatedExpression.isBlank()) {
            this.setGenerated(true);
        }
        super.putOriginalData("generatedExpression", generatedExpression);
    }

    public String getGeneratedStorage() {
        return this.generatedStorage;
    }

    public void setGeneratedStorage(String generatedStorage) {
        this.generatedStorage = generatedStorage;
        super.putOriginalData("generatedStorage", generatedStorage);
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
        super.putOriginalData("generated", generated);
    }

    @Override
    public boolean isGenerated() {
        return this.generated;
    }

    /**
     * 清理当前字段类型不适用的属性。
     */
    public void normalizeGeneratedAttributes() {
        if (this.isGenerated()) {
            this.setAutoIncrement(false);
            this.setDefaultValue(null);
        } else {
            this.setGeneratedExpression(null);
            this.setGeneratedStorage(null);
        }
    }

    public boolean isNullable() {
        return !this.notNull;
    }

    public void setNullable(boolean nullable) {
        this.setNotNull(!nullable);
    }

    public boolean isColumnChanged() {
        return this.isNameChanged() || this.isTypeChanged()
                || super.checkOriginalData("size", this.size)
                || super.checkOriginalData("notNull", this.notNull)
                || super.checkOriginalData("primaryKey", this.isPrimaryKey())
                || super.checkOriginalData("autoIncrement", this.autoIncrement)
                || super.checkOriginalData("defaultValue", this.defaultValue)
                || super.checkOriginalData("hidden", this.hidden)
                || super.checkOriginalData("unique", this.unique)
                || super.checkOriginalData("collate", this.collate)
                || super.checkOriginalData("check", this.check)
                || super.checkOriginalData("generated", this.generated)
                || super.checkOriginalData("generatedExpression", this.generatedExpression)
                || super.checkOriginalData("generatedStorage", this.generatedStorage);
    }

    @Override
    public void copy(SQLiteColumn column) {
        if (column != null) {
            this.setDbName(column.getDbName());
            this.setTableName(column.getTableName());
            this.setName(column.getName());
            this.setType(ShellSqliteColumnUtil.normalizeType(column.getType()));
            this.setSize(column.getSize() == null
                    ? ShellSqliteColumnUtil.parseSize(column.getType()) : column.getSize());
            this.setPosition(column.getPosition());
            this.setPrimaryKey(column.isPrimaryKey());
            this.setNotNull(column.isNotNull());
            this.setAutoIncrement(column.isAutoIncrement());
            this.setDefaultValue(column.getDefaultValue());
            this.setHidden(column.isHidden());
            this.setUnique(column.isUnique());
            this.setCollate(column.getCollate());
            this.setCheck(column.getCheck());
            this.setGenerated(column.isGenerated());
            this.setGeneratedExpression(column.getGeneratedExpression());
            this.setGeneratedStorage(column.getGeneratedStorage());
        }
    }

    @Override
    public DBDialect getDialect() {
        return DBDialect.SQLITE;
    }

    @Override
    public boolean isDateType() {
        return ShellSqliteColumnUtil.isDateType(this.getType());
    }

    @Override
    public boolean isDateTimeType() {
        return ShellSqliteColumnUtil.isDateTimeType(this.getType());
    }

    @Override
    public boolean isTimeType() {
        return ShellSqliteColumnUtil.isTimeType(this.getType());
    }
}
