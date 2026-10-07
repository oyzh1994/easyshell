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
 * 达梦数据库字段信息
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

    /**
     * 构造达梦数据库字段
     */
    public DamengColumn() {

    }

    /**
     * 构造达梦数据库字段
     *
     * @param name 字段名称
     */
    public DamengColumn(String name) {
        this.name = name;
    }

    /**
     * 名称是否变更
     *
     * @return 变更结果
     */
    public boolean isNameChanged() {
        return super.checkOriginalData("name", this.name);
    }

    /**
     * 获取原始名称
     *
     * @return 原始名称
     */
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

    /**
     * 类型是否变更
     *
     * @return 变更结果
     */
    public boolean isTypeChanged() {
        return super.checkOriginalData("type", this.getType());
    }

    /**
     * 长度是否变更
     *
     * @return 变更结果
     */
    public boolean isSizeChanged() {
        return super.checkOriginalData("size", this.size);
    }

    /**
     * 获取字段值列表
     *
     * @return 字段值列表
     */
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

    /**
     * 设置默认值
     *
     * @param defaultValue 默认值
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        super.putOriginalData("defaultValue", defaultValue);
    }

    /**
     * 默认值是否变更
     *
     * @return 变更结果
     */
    public boolean isDefaultValueChanged() {
        return super.checkOriginalData("defaultValue", this.defaultValue);
    }

    /**
     * 获取默认值的字符串形式
     *
     * @return 默认值字符串
     */
    public String getDefaultValueString() {
        Object defaultValue = this.defaultValue;
        return defaultValue == null ? null : defaultValue.toString();
    }

    /**
     * 获取按字段类型修正后的默认值
     *
     * @return 修正后的默认值
     */
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

    /**
     * 设置是否自动递增
     *
     * @param autoIncrement 是否自动递增
     */
    public void setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        super.putOriginalData("autoIncrement", autoIncrement);
        // // 如果是自动递增，则清除默认值
        // if (BooleanUtil.isTrue(autoIncrement)) {
        //     this.setDefaultValue(null);
        // }
    }

    /**
     * 是否自动递增
     *
     * @return 是否自动递增
     */
    public boolean isAutoIncrement() {
        return BooleanUtil.isTrue(this.autoIncrement);
    }

    /**
     * 自动递增是否变更
     *
     * @return 变更结果
     */
    public boolean isAutoIncrementChanged() {
        return super.checkOriginalData("autoIncrement", this.autoIncrement);
    }

    /**
     * 是否存在注释
     *
     * @return 是否存在注释
     */
    public boolean hasComment() {
        return this.getComment() != null;
    }

    /**
     * 设置字段值
     *
     * @param value 字段值
     */
    public void setValue(String value) {
        this.valueProperty().setValue(value);
        super.putOriginalData("value", value);
    }

    /**
     * 设置是否无符号
     *
     * @param unsigned 是否无符号
     */
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

    /**
     * 设置是否随当前时间戳更新
     *
     * @param updateOnCurrentTimestamp 是否随当前时间戳更新
     */
    public void setUpdateOnCurrentTimestamp(Boolean updateOnCurrentTimestamp) {
        this.updateOnCurrentTimestamp = updateOnCurrentTimestamp;
        super.putOriginalData("updateOnCurrentTimestamp", updateOnCurrentTimestamp);
    }

    /**
     * 是否随当前时间戳更新
     *
     * @return 是否随当前时间戳更新
     */
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

    /**
     * 设置注释
     *
     * @param comment 注释
     */
    public void setComment(String comment) {
        this.comment = comment;
        super.putOriginalData("comment", comment);
    }

    /**
     * 注释是否变更
     *
     * @return 变更结果
     */
    public boolean isCommentChanged() {
        return super.checkOriginalData("comment", this.getType());
    }

    @Override
    public void setSize(Integer size) {
        this.size = size;
        super.putOriginalData("size", size);
    }

    /**
     * 设置小数位
     *
     * @param digits 小数位
     */
    public void setDigits(Integer digits) {
        this.digits = digits;
        super.putOriginalData("digits", digits);
    }

    /**
     * 设置是否可为空
     *
     * @param nullable 是否可为空
     */
    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
        super.putOriginalData("nullable", nullable);
    }

    /**
     * 获取主键属性
     *
     * @return 主键属性
     */
    public SimpleBooleanProperty primaryKeyProperty() {
        if (this.primaryKeyProperty == null) {
            this.primaryKeyProperty = new SimpleBooleanProperty();
        }
        return this.primaryKeyProperty;
    }

    /**
     * 是否主键
     *
     * @return 是否主键
     */
    public boolean isPrimaryKey() {
        return this.primaryKeyProperty != null && this.primaryKeyProperty.get();
    }

    /**
     * 设置是否主键
     *
     * @param primaryKey 是否主键
     */
    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKeyProperty().set(primaryKey);
        super.putOriginalData("primaryKey", primaryKey);
    }

    /**
     * 字段是否变更
     *
     * @return 变更结果
     */
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

    /**
     * 设置是否填充零
     *
     * @param zeroFill 是否填充零
     */
    public void setZeroFill(Boolean zeroFill) {
        this.zeroFill = zeroFill;
        super.putOriginalData("zeroFill", zeroFill);
    }

    /**
     * 是否填充零
     *
     * @return 是否填充零
     */
    public boolean isZeroFill() {
        return BooleanUtil.isTrue(this.zeroFill);
    }

    /**
     * 设置主键长度
     *
     * @param primaryKeySize 主键长度
     */
    public void setPrimaryKeySize(Integer primaryKeySize) {
        this.primaryKeySize = primaryKeySize;
        super.putOriginalData("primaryKeySize", primaryKeySize);
    }

    /**
     * 是否可为空
     *
     * @return 是否可为空
     */
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

    /**
     * 解析主键标识
     *
     * @param key 主键标识
     */
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

    /**
     * 是否存在默认值
     *
     * @return 是否存在默认值
     */
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

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置模式名称
     *
     * @param schema 模式名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名称
     *
     * @param tableName 表名称
     */
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

    /**
     * 获取字段类型属性
     *
     * @return 字段类型属性
     */
    public StringProperty typeProperty() {
        if (this.typeProperty == null) {
            this.typeProperty = new SimpleStringProperty();
        }
        return typeProperty;
    }

    /**
     * 获取字段值
     *
     * @return 字段值
     */
    public String getValue() {
        return this.valueProperty == null ? null : this.valueProperty.getValue();
    }

    /**
     * 获取字段值属性
     *
     * @return 字段值属性
     */
    public StringProperty valueProperty() {
        if (this.valueProperty == null) {
            this.valueProperty = new SimpleStringProperty();
        }
        return valueProperty;
    }

    /**
     * 获取注释
     *
     * @return 注释
     */
    public String getComment() {
        return comment;
    }

    /**
     * 获取是否可为空
     *
     * @return 是否可为空
     */
    public Boolean getNullable() {
        return nullable;
    }

    /**
     * 获取是否无符号
     *
     * @return 是否无符号
     */
    public Boolean getUnsigned() {
        return unsigned;
    }

    /**
     * 获取是否填充零
     *
     * @return 是否填充零
     */
    public Boolean getZeroFill() {
        return zeroFill;
    }

    /**
     * 获取是否随当前时间戳更新
     *
     * @return 是否随当前时间戳更新
     */
    public Boolean getUpdateOnCurrentTimestamp() {
        return updateOnCurrentTimestamp;
    }

    /**
     * 获取字段位置
     *
     * @return 字段位置
     */
    public Integer getPosition() {
        return position == null ? 0 : position;
    }

    /**
     * 设置字段位置
     *
     * @param position 字段位置
     */
    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * 获取主键长度
     *
     * @return 主键长度
     */
    public Integer getPrimaryKeySize() {
        return primaryKeySize;
    }

    /**
     * 获取默认值
     *
     * @return 默认值
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * 获取小数位
     *
     * @return 小数位
     */
    public Integer getDigits() {
        return digits;
    }

    /**
     * 获取是否自动递增
     *
     * @return 是否自动递增
     */
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
