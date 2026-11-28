package cn.oyzh.easyshell.h2.column;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * H2 字段。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2Column extends DBObject implements DBColumn, ObjectCopier<H2Column> {

    public static final String KIND_NORMAL = "NORMAL";

    public static final String KIND_GENERATED = "GENERATED";

    private String dbName;

    private String tableName;

    private String name;

    private StringProperty typeProperty;

    private Integer size;

    /**
     * 数值精度的小数位数
     */
    private Integer digits;

    private Integer position;

    private boolean notNull;

    private BooleanProperty primaryKeyProperty;

    /**
     * 是否自增（H2 IDENTITY 列）
     */
    private boolean identity;

    private String defaultValue;

    private boolean unique;

    private String check;

    /**
     * 数据库中的检查约束名称
     */
    private String checkName;

    private boolean generated;

    private String generatedExpression;

    private boolean hidden;

    private String comment;

    public H2Column() {
    }

    public H2Column(String name) {
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
        String normalized = type == null ? null : type.trim().toUpperCase(java.util.Locale.ROOT);
        this.typeProperty().set(normalized);
        super.putOriginalData("type", normalized);
    }

    public boolean isTypeChanged() {
        return super.checkOriginalData("type", this.getType());
    }

    public boolean isSizeChanged() {
        return super.checkOriginalData("size", this.size);
    }

    public boolean isDigitsChanged() {
        return super.checkOriginalData("digits", this.digits);
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

    @Override
    public Integer getDigits() {
        return this.digits;
    }

    public void setDigits(Integer digits) {
        this.digits = digits;
        super.putOriginalData("digits", digits);
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

    /**
     * 是否自增（H2 IDENTITY 列）。
     *
     * @return 结果
     */
    @Override
    public boolean isAutoIncrement() {
        return this.identity;
    }

    /**
     * 设置是否自增（H2 IDENTITY 列）。
     *
     * @param autoIncrement 是否自增
     */
    public void setAutoIncrement(boolean autoIncrement) {
        this.setIdentity(autoIncrement);
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        super.putOriginalData("hidden", hidden);
    }

    /**
     * 非空设置是否变化。
     *
     * @return 结果
     */
    public boolean isNotNullChanged() {
        return super.checkOriginalData("notNull", this.notNull);
    }

    /**
     * 默认值是否变化。
     *
     * @return 结果
     */
    public boolean isDefaultValueChanged() {
        return super.checkOriginalData("defaultValue", this.defaultValue);
    }

    /**
     * 设置是否自增（H2 IDENTITY 列）。
     *
     * @param identity 是否自增
     */
    public void setIdentity(boolean identity) {
        this.identity = identity;
        super.putOriginalData("identity", identity);
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        super.putOriginalData("defaultValue", defaultValue);
    }

    public boolean isUnique() {
        return this.unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
        super.putOriginalData("unique", unique);
    }

    public String getCheck() {
        return this.check;
    }

    public void setCheck(String check) {
        this.check = check;
        super.putOriginalData("check", check);
    }

    public String getCheckName() {
        return this.checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
        super.putOriginalData("checkName", checkName);
    }

    public boolean isCheckChanged() {
        return super.checkOriginalData("check", this.check);
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        super.putOriginalData("comment", comment);
    }

    public boolean isCommentChanged() {
        return super.checkOriginalData("comment", this.comment);
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
            this.setIdentity(false);
            this.setDefaultValue(null);
        } else {
            this.setGeneratedExpression(null);
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
                || this.isSizeChanged()
                || this.isDigitsChanged()
                || super.checkOriginalData("notNull", this.notNull)
                || super.checkOriginalData("primaryKey", this.isPrimaryKey())
                || super.checkOriginalData("identity", this.identity)
                || super.checkOriginalData("defaultValue", this.defaultValue)
                || super.checkOriginalData("unique", this.unique)
                || super.checkOriginalData("check", this.check)
                || super.checkOriginalData("checkName", this.checkName)
                || super.checkOriginalData("generated", this.generated)
                || super.checkOriginalData("generatedExpression", this.generatedExpression)
                || super.checkOriginalData("comment", this.comment);
    }

    @Override
    public void copy(H2Column column) {
        if (column != null) {
            this.setDbName(column.getDbName());
            this.setTableName(column.getTableName());
            this.setName(column.getName());
            this.setType(ShellH2ColumnUtil.normalizeType(column.getType()));
            this.setSize(column.getSize() == null
                    ? ShellH2ColumnUtil.parseSize(column.getType()) : column.getSize());
            this.setDigits(column.getDigits() == null
                    ? ShellH2ColumnUtil.parseDigits(column.getType()) : column.getDigits());
            this.setPosition(column.getPosition());
            this.setPrimaryKey(column.isPrimaryKey());
            this.setNotNull(column.isNotNull());
            this.setIdentity(column.isAutoIncrement());
            this.setDefaultValue(column.getDefaultValue());
            this.setUnique(column.isUnique());
            this.setCheck(column.getCheck());
            this.setCheckName(column.getCheckName());
            this.setGenerated(column.isGenerated());
            this.setGeneratedExpression(column.getGeneratedExpression());
            this.setHidden(column.isHidden());
            this.setComment(column.getComment());
        }
    }

    @Override
    public DBDialect getDialect() {
        return DBDialect.H2;
    }

    @Override
    public boolean isDateType() {
        return ShellH2ColumnUtil.isDateType(this.getType());
    }

    @Override
    public boolean isDateTimeType() {
        return ShellH2ColumnUtil.isDateTimeType(this.getType());
    }

    @Override
    public boolean isTimeType() {
        return ShellH2ColumnUtil.isTimeType(this.getType());
    }
}
