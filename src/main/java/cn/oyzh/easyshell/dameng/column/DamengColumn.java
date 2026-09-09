package cn.oyzh.easyshell.dameng.column;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.dameng.ShellDamengColumnUtil;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.util.DBUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * db字段
 *
 * @author oyzh
 * @since 2023/12/20
 */
public class DamengColumn extends DBObject implements DBColumn, ObjectCopier<DamengColumn>, Destroyable {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 字段大小
     */
    private Integer size;

    /**
     * 字段类型
     */
    private StringProperty typeProperty;

    /**
     * 字段值
     */
    private StringProperty valueProperty;

    /**
     * 注释
     */
    private String comment;

    /**
     * 可为null
     */
    private Boolean nullable;

    /**
     * 无符号
     */
    private Boolean unsigned;

    /**
     * 填充零
     */
    private Boolean zeroFill;

    /**
     * 根据当前时间戳更新
     */
    private Boolean updateOnCurrentTimestamp;

    /**
     * 字段位置
     */
    private Integer position;

    /**
     * 主键属性
     */
    private SimpleBooleanProperty primaryKeyProperty;

    /**
     * 键长度
     */
    private Integer primaryKeySize;

    /**
     * 默认值
     */
    private Object defaultValue;

    /**
     * 小数位
     */
    private Integer digits;

    /**
     * 自动递增
     */
    private Boolean autoIncrement;

    /**
     * 名称
     */
    private String name;

    public DamengColumn() {

    }

    public DamengColumn(String name) {
        this.name = name;
    }

    public boolean isNameChanged() {
        return super.checkOriginalData("name", this.name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    @Override
    public void setType(String type) {
        if (type != null) {
            type = type.toUpperCase();
        }
        this.typeProperty().set(type);
        super.putOriginalData("type", type);
    }

    public boolean isTypeChanged() {
        return super.checkOriginalData("type", this.getType());
    }

    public boolean isSizeChanged() {
        return super.checkOriginalData("size", this.size);
    }

    public List<String> getValueList() {
        List<String> valueList = new ArrayList<>();
        if (this.getValue() != null) {
            List<String> list = StringUtil.split(this.getValue(), ",");
            for (String s : list) {
                if (s.startsWith("'") && s.endsWith("'")) {
                    valueList.add(s.substring(1, s.length() - 1));
                } else {
                    valueList.add(s);
                }
            }
        }
        return valueList;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        super.putOriginalData("defaultValue", defaultValue);
    }

    public boolean isDefaultValueChanged() {
        return super.checkOriginalData("defaultValue", this.defaultValue);
    }

    public String getDefaultValueString() {
        Object defaultValue = this.defaultValue;
        return defaultValue == null ? null : defaultValue.toString();
    }

    public Object getDefaultValueFix() {
        Object defaultValue = this.defaultValue;
        if (defaultValue == null) {
            return null;
        }
        String valStr = defaultValue.toString();
        if (this.supportInteger()) {
            if (StringUtil.isBlank(valStr) || StringUtil.equalsIgnoreCase(valStr, "null")) {
                return null;
            }
            if (RegexUtil.isNumber(valStr)) {
                return NumberUtil.toLong(valStr);
            }
        }
        if (this.supportDigits()) {
            if (StringUtil.isBlank(valStr) || StringUtil.equalsIgnoreCase(valStr, "null")) {
                return null;
            }
            if (RegexUtil.isDecimal(valStr)) {
                return NumberUtil.toDouble(valStr);
            }
        }
        if (this.supportTimestamp()) {
            if (StringUtil.equalsIgnoreCase(valStr, "null")) {
                return null;
            }
            if (StringUtil.equalsIgnoreCase(valStr, "CURRENT_TIMESTAMP()")) {
                return valStr;
            }
        }
        return DBUtil.wrapData(defaultValue, DBDialect.DAMENG);
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        super.putOriginalData("autoIncrement", autoIncrement);
        // // 如果是自动递增，则清除默认值
        // if (BooleanUtil.isTrue(autoIncrement)) {
        //     this.setDefaultValue(null);
        // }
    }

    public boolean isAutoIncrement() {
        return BooleanUtil.isTrue(this.autoIncrement);
    }

    public boolean isAutoIncrementChanged() {
        return super.checkOriginalData("autoIncrement", this.autoIncrement);
    }

    public boolean hasComment() {
        return this.getComment() != null;
    }

    public void setValue(String value) {
        this.valueProperty().setValue(value);
        super.putOriginalData("value", value);
    }

    public void setUnsigned(Boolean unsigned) {
        this.unsigned = unsigned;
        super.putOriginalData("unsigned", unsigned);
    }

    /**
     * 是否无符号模式
     *
     * @return 无符号模式
     */
    public boolean isUnsigned() {
        return BooleanUtil.isTrue(this.unsigned);
    }

    public void setUpdateOnCurrentTimestamp(Boolean updateOnCurrentTimestamp) {
        this.updateOnCurrentTimestamp = updateOnCurrentTimestamp;
        super.putOriginalData("updateOnCurrentTimestamp", updateOnCurrentTimestamp);
    }

    public boolean isUpdateOnCurrentTimestamp() {
        return BooleanUtil.isTrue(this.updateOnCurrentTimestamp);
    }

    @Override
    public boolean supportSize() {
        return DBColumnFieldManager.supportSize(DBDialect.DAMENG, this.getType());
    }

    /**
     * 获取推荐长度
     *
     * @return 推荐长度
     */
    public Integer suggestSize() {
        return DBColumnFieldManager.suggestSize(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportUnsigned() {
        return DBColumnFieldManager.supportUnsigned(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportDigits() {
        return DBColumnFieldManager.supportDigits(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportInteger() {
        return DBColumnFieldManager.supportInteger(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportAutoIncrement() {
        return DBColumnFieldManager.supportAutoIncrement(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportDefaultValue() {
        return DBColumnFieldManager.supportDefaultValue(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportTimestamp() {
        return DBColumnFieldManager.supportTimestamp(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportValue() {
        return DBColumnFieldManager.supportValue(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportBit() {
        return DBColumnFieldManager.supportBit(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportBoolean() {
        return DBColumnFieldManager.supportBoolean(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportJson() {
        return DBColumnFieldManager.supportJson(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportJsonArray() {
        return DBColumnFieldManager.supportJsonArray(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportText() {
        return DBColumnFieldManager.supportText(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportKeySize() {
        return DBColumnFieldManager.supportKeySize(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean supportString() {
        return DBColumnFieldManager.supportString(DBDialect.DAMENG, this.getType());
    }

    @Override
    public Long minValue() {
        return DBColumnFieldManager.minValue(DBDialect.DAMENG, this.getType());
    }

    @Override
    public Long maxValue() {
        return DBColumnFieldManager.maxValue(DBDialect.DAMENG, this.getType());
    }

    @Override
    public Object exampleValue() {
        return DBColumnFieldManager.exampleValue(DBDialect.DAMENG, this.getType());
    }

    @Override
    public boolean isYearType() {
        return ShellDamengColumnUtil.isYearType(this.getType());
    }

    @Override
    public boolean isDateType() {
        return ShellDamengColumnUtil.isDateType(this.getType());
    }

    @Override
    public boolean isDateTimeType() {
        return ShellDamengColumnUtil.isDateTimeType(this.getType());
    }

    @Override
    public boolean isTimeType() {
        return ShellDamengColumnUtil.isTimeType(this.getType());
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public void setComment(String comment) {
        this.comment = comment;
        super.putOriginalData("comment", comment);
    }

    public boolean isCommentChanged() {
        return super.checkOriginalData("comment", this.getType());
    }

    @Override
    public void setSize(Integer size) {
        this.size = size;
        super.putOriginalData("size", size);
    }

    public void setDigits(Integer digits) {
        this.digits = digits;
        super.putOriginalData("digits", digits);
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
        super.putOriginalData("nullable", nullable);
    }

    public SimpleBooleanProperty primaryKeyProperty() {
        if (this.primaryKeyProperty == null) {
            this.primaryKeyProperty = new SimpleBooleanProperty();
        }
        return this.primaryKeyProperty;
    }

    public boolean isPrimaryKey() {
        return this.primaryKeyProperty != null && this.primaryKeyProperty.get();
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKeyProperty().set(primaryKey);
        super.putOriginalData("primaryKey", primaryKey);
    }

    public boolean isColumnChanged() {
        for (Map.Entry<String, Object> entry : super.originalData().entrySet()) {
            if (!StringUtil.equalsAny(entry.getKey(), "primaryKey", "primaryKeySize")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 主键是否变更
     *
     * @return 结果
     */
    public boolean isPrimaryKeyChanged() {
        // 判断1
        boolean checked1 = super.checkOriginalData("primaryKey", this.isPrimaryKey());
        if (checked1) {
            return true;
        }
        // 判断2
        boolean checked2 = super.checkOriginalData("primaryKeySize", this.getPrimaryKeySize());
        if (checked2) {
            return true;
        }
        // 判断3
        if (this.isCreated() && this.isPrimaryKey()) {
            return true;
        }
        // 判断4
        if (this.isNameChanged() && this.isPrimaryKey()) {
            return true;
        }
        // 判断5
        if (this.isDeleted() && this.isPrimaryKey()) {
            return true;
        }
        // return this.isDeleted();
        return false;
    }

    public void setZeroFill(Boolean zeroFill) {
        this.zeroFill = zeroFill;
        super.putOriginalData("zeroFill", zeroFill);
    }

    public boolean isZeroFill() {
        return BooleanUtil.isTrue(this.zeroFill);
    }

    public void setPrimaryKeySize(Integer primaryKeySize) {
        this.primaryKeySize = primaryKeySize;
        super.putOriginalData("primaryKeySize", primaryKeySize);
    }

    public boolean isNullable() {
        return BooleanUtil.isTrue(this.nullable);
    }

    @Override
    public boolean supportBinary() {
        return DBColumnFieldManager.supportBinary(DBDialect.DAMENG, this.getType());
    }

    @Override
    public void initStatus() {
        if (this.size == null) {
            this.setSize(null);
        }
        if (this.getValue() == null) {
            this.setValue(null);
        }
        if (this.digits == null) {
            this.setDigits(null);
        }
        if (this.unsigned == null) {
            this.setUnsigned(null);
        }
        if (this.zeroFill == null) {
            this.setZeroFill(null);
        }
        if (this.autoIncrement == null) {
            this.setAutoIncrement(null);
        }
        if (this.updateOnCurrentTimestamp == null) {
            this.setUpdateOnCurrentTimestamp(null);
        }
    }

    public void parseKey(String key) {
        if (StringUtil.isEmpty(key)) {
            return;
        }
        if ("pri".equalsIgnoreCase(key)) {
            this.setPrimaryKey(true);
        } else {
            this.setPrimaryKey(false);
        }
    }

    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    @Override
    public void copy(DamengColumn column) {
        if (column != null) {
            this.setSize(column.size);
            this.setName(column.name);
            this.setType(column.getType());
            this.setValue(column.getValue());
            this.setDigits(column.digits);
            this.setComment(column.comment);
            this.setNullable(column.nullable);
            this.setUnsigned(column.unsigned);
            this.setZeroFill(column.zeroFill);
            this.setTableName(column.tableName);
            this.setDefaultValue(column.defaultValue);
            this.setPrimaryKey(column.isPrimaryKey());
            this.setAutoIncrement(column.autoIncrement);
            this.setPrimaryKeySize(column.primaryKeySize);
            this.setUpdateOnCurrentTimestamp(column.updateOnCurrentTimestamp);
        }
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Integer getSize() {
        return size;
    }

    @Override
    public String getType() {
        return this.typeProperty == null ? null : this.typeProperty.get();
    }

    public StringProperty typeProperty() {
        if (this.typeProperty == null) {
            this.typeProperty = new SimpleStringProperty();
        }
        return typeProperty;
    }

    public String getValue() {
        return this.valueProperty == null ? null : this.valueProperty.getValue();
    }

    public StringProperty valueProperty() {
        if (this.valueProperty == null) {
            this.valueProperty = new SimpleStringProperty();
        }
        return valueProperty;
    }

    public String getComment() {
        return comment;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public Boolean getUnsigned() {
        return unsigned;
    }

    public Boolean getZeroFill() {
        return zeroFill;
    }

    public Boolean getUpdateOnCurrentTimestamp() {
        return updateOnCurrentTimestamp;
    }

    public Integer getPosition() {
        return position == null ? 0 : position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getPrimaryKeySize() {
        return primaryKeySize;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Integer getDigits() {
        return digits;
    }

    public Boolean getAutoIncrement() {
        return autoIncrement;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void destroy() {
        if (this.typeProperty != null) {
            this.typeProperty.unbind();
        }
        if (this.valueProperty != null) {
            this.valueProperty.unbind();
        }
        if (this.primaryKeyProperty != null) {
            this.primaryKeyProperty.unbind();
        }
        super.destroy();
    }
}
