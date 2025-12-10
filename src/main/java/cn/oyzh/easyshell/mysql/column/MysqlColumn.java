package cn.oyzh.easyshell.mysql.column;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBObjectStatus;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
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
public class MysqlColumn extends DBObjectStatus implements ObjectCopier<MysqlColumn>, DestroyAdapter {

    /**
     * 库名称
     */
    private String dbName;

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
    private StringProperty typeProperty = new SimpleStringProperty();

    /**
     * 字段值
     */
    private StringProperty valueProperty = new SimpleStringProperty();

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
     * 字段字符集
     */
    private String charset;

    /**
     * 字段排序规则
     */
    private String collation;

    public MysqlColumn() {

    }

    public MysqlColumn(String name) {
        this.name = name;
    }

    public boolean isNameChanged() {
        return super.checkOriginalData("name", this.name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public void setType(String type) {
        if (type != null) {
            type = type.toUpperCase();
        }
        this.typeProperty.set(type);
        super.putOriginalData("type", type);
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

    @Deprecated
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
        if (StringUtil.equalsIgnoreCase(valStr, "null")) {
            return null;
        }
        if (this.supportInteger()) {
            if (StringUtil.isBlank(valStr)) {
                return null;
            }
            if (RegexUtil.isNumber(valStr)) {
                return NumberUtil.toLong(valStr);
            }
        }
        if (this.supportDigits()) {
            if (StringUtil.isBlank(valStr)) {
                return null;
            }
            if (RegexUtil.isDecimal(valStr)) {
                return NumberUtil.toDouble(valStr);
            }
        }
        return defaultValue;
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

    public boolean hasComment() {
        return this.getComment() != null;
    }

    public void setCharset(String charset) {
        this.charset = charset;
        super.putOriginalData("charset", charset);
    }

    public void setCollation(String collation) {
        this.collation = collation;
        super.putOriginalData("collation", collation);
    }

    public void setValue(String value) {
        this.valueProperty.setValue(value);
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

    /**
     * 是否支持长度
     *
     * @return 结果
     */
    public boolean supportSize() {
        return ShellMysqlColumnUtil.supportSize(this.getType());
    }

    /**
     * 获取推荐长度
     *
     * @return 推荐长度
     */
    public Integer suggestSize() {
        return ShellMysqlColumnUtil.suggestSize(this.getType());
    }

    /**
     * 是否支持长度
     *
     * @return 结果
     */
    public boolean supportGeometry() {
        return ShellMysqlColumnUtil.supportGeometry(this.getType());
    }

    /**
     * 是否支持字符集及排序
     *
     * @return 结果
     */
    public boolean supportCharset() {
        return ShellMysqlColumnUtil.supportCharset(this.getType());
    }

    /**
     * 是否支持无符号
     *
     * @return 结果
     */
    public boolean supportUnsigned() {
        return ShellMysqlColumnUtil.supportUnsigned(this.getType());
    }

    /**
     * 是否支持小数
     *
     * @return 结果
     */
    public boolean supportDigits() {
        return ShellMysqlColumnUtil.supportDigits(this.getType());
    }

    /**
     * 是否支持整数
     *
     * @return 结果
     */
    public boolean supportInteger() {
        return ShellMysqlColumnUtil.supportInteger(this.getType());
    }

    /**
     * 是否支持自动递增
     *
     * @return 结果
     */
    public boolean supportAutoIncrement() {
        return ShellMysqlColumnUtil.supportAutoIncrement(this.getType());
    }

    /**
     * 是否支持默认值
     *
     * @return 结果
     */
    public boolean supportDefaultValue() {
        return ShellMysqlColumnUtil.supportDefaultValue(this.getType());
    }

    /**
     * 是否支持当前时间戳
     *
     * @return 结果
     */
    public boolean supportTimestamp() {
        return ShellMysqlColumnUtil.supportTimestamp(this.getType());
    }

    /**
     * 是否支持主键
     *
     * @return 结果
     */
    public boolean supportValue() {
        return ShellMysqlColumnUtil.supportValue(this.getType());
    }

    /**
     * 是否支持填充零
     *
     * @return 结果
     */
    public boolean supportZeroFill() {
        return ShellMysqlColumnUtil.supportZeroFill(this.getType());
    }

    /**
     * 是否支持填充零
     *
     * @return 结果
     */
    public boolean supportBit() {
        return ShellMysqlColumnUtil.supportBit(this.getType());
    }

    /**
     * 是否支持填充零
     *
     * @return 结果
     */
    public boolean supportJson() {
        return ShellMysqlColumnUtil.supportJson(this.getType());
    }

    /**
     * 是否支持键长度
     *
     * @return 结果
     */
    public boolean supportKeySize() {
        return ShellMysqlColumnUtil.supportKeySize(this.getType());
    }

    public boolean supportString() {
        return ShellMysqlColumnUtil.supportString(this.getType());
    }

    public Long minValue() {
        return ShellMysqlColumnUtil.minValue(this.getType());
    }

    public Long maxValue() {
        return ShellMysqlColumnUtil.maxValue(this.getType());
    }

    public Object exampleValue() {
        return ShellMysqlColumnUtil.exampleValue(this.getType());
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public void setComment(String comment) {
        this.comment = comment;
        super.putOriginalData("comment", comment);
    }

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
        // if (this.primaryKey != null) {
        //     this.primaryKey.setPrimaryKeySize(primaryKeySize);
        // }
    }

    public boolean isNullable() {
        return BooleanUtil.isTrue(this.nullable);
    }

    // @Override
    // public void setDeleted(boolean deleted) {
    //     super.setDeleted(deleted);
    //     // if (deleted && this.isPrimaryKey()) {
    //     //     this.setPrimaryKey(false);
    //     // }
    // }

    public boolean isYearType() {
        return ShellMysqlColumnUtil.isYearType(this.getType());
    }

    public boolean isDateType() {
        return ShellMysqlColumnUtil.isDateType(this.getType());
    }

    public boolean isGeometryType() {
        return ShellMysqlColumnUtil.isGeometryType(this.getType());
    }

    public boolean isTimeType() {
        return ShellMysqlColumnUtil.isTimeType(this.getType());
    }

    public boolean supportBinary() {
        return ShellMysqlColumnUtil.supportBinary(this.getType());
    }

    public boolean supportEnum() {
        return ShellMysqlColumnUtil.supportEnum(this.getType());
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
            // this.primaryKey = new MysqlPrimaryKey();
            // this.primaryKey.setPrimaryKey(true);
            this.setPrimaryKey(true);
        } else {
            this.setPrimaryKey(false);
        }
    }

    public void parseType(String type) {
        if (!type.contains("(") && !type.contains(" ")) {
            this.setType(type);
            return;
        }
        type = type.toLowerCase();
        if (type.contains("unsigned")) {
            this.setUnsigned(true);
            type = type.replace("unsigned", "").trim();
        }
        if (type.contains("zerofill")) {
            this.setZeroFill(true);
            type = type.replace("zerofill", "").trim();
        }
        if (!type.contains("(")) {
            this.setType(type);
            return;
        }

        String _type = type.substring(0, type.indexOf("("));
        this.setType(_type);
        String sub1 = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        // 枚举
        if (this.supportEnum()) {
            this.setValue(sub1);
        } else if (this.supportDigits() && sub1.contains(",")) {// 小数
            String[] arr = sub1.split(",");
            this.setSize(Integer.parseInt(arr[0]));
            this.setDigits(Integer.parseInt(arr[1]));
        } else {// 整数
            this.setSize(Integer.parseInt(sub1));
        }
    }

    public void parseExtra(String extra) {
        if (StringUtil.isEmpty(extra)) {
            return;
        }
        if (StringUtil.containsIgnoreCase(extra, "auto_increment")) {
            this.setAutoIncrement(true);
        }
        if (StringUtil.containsIgnoreCase(extra, "on update CURRENT_TIMESTAMP")) {
            this.setUpdateOnCurrentTimestamp(true);
        }
    }

    public void parseCollation(String collation) {
        if (StringUtil.isEmpty(collation)) {
            return;
        }
        this.setCollation(collation);
        this.setCharset(collation.substring(0, collation.indexOf("_")));
    }

    public void initColumn(String columnType, String columnExtra) {
        if (!columnType.contains("(") && !columnType.contains(" ")) {
            this.setType(columnType.toUpperCase());
        } else if (!columnType.contains("(")) {
            this.setType(columnType.toUpperCase());
        } else {
            String type = columnType.substring(0, columnType.indexOf("("));
            this.setType(type.toUpperCase());
            String sub1 = columnType.substring(columnType.indexOf("(") + 1, columnType.lastIndexOf(")"));
            if (this.supportEnum()) {
                this.setValue(sub1);
            } else if (this.supportDigits() && sub1.contains(",")) {
                String[] arr = sub1.split(",");
                this.setSize(Integer.parseInt(arr[0]));
                this.setDigits(Integer.parseInt(arr[1]));
            } else {
                this.setSize(Integer.parseInt(sub1));
            }
            if (StringUtil.containsIgnoreCase(columnType, "unsigned")) {
                this.setUnsigned(true);
            }
            if (StringUtil.containsIgnoreCase(columnType, "zerofill")) {
                this.setZeroFill(true);
            }
        }
        if (StringUtil.containsIgnoreCase(columnExtra, "auto_increment")) {
            this.setAutoIncrement(true);
        }
        if (StringUtil.containsIgnoreCase(columnExtra, "on update CURRENT_TIMESTAMP")) {
            this.setUpdateOnCurrentTimestamp(true);
        }
    }

    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    @Override
    public void copy(MysqlColumn column) {
        if (column != null) {
            this.setSize(column.size);
            this.setName(column.name);
            this.setType(column.getType());
            this.setValue(column.getValue());
            this.setDbName(column.dbName);
            this.setDigits(column.digits);
            this.setComment(column.comment);
            this.setCharset(column.charset);
            this.setNullable(column.nullable);
            this.setUnsigned(column.unsigned);
            this.setZeroFill(column.zeroFill);
            this.setTableName(column.tableName);
            this.setCollation(column.collation);
            this.setDefaultValue(column.defaultValue);
            this.setPrimaryKey(column.isPrimaryKey());
            this.setAutoIncrement(column.autoIncrement);
            this.setPrimaryKeySize(column.primaryKeySize);
            this.setUpdateOnCurrentTimestamp(column.updateOnCurrentTimestamp);
        }
    }

    public boolean isInvalid() {
        return StringUtil.isBlank(this.getName()) || StringUtil.isBlank(this.getType());
    }

    // public Integer getPrimaryKeySize() {
    //     return this.primaryKey == null ? null : this.primaryKey.getPrimaryKeySize();
    // }


    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
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

    public Integer getSize() {
        return size;
    }

    public String getType() {
        return typeProperty.get();
    }

    public StringProperty typeProperty() {
        return typeProperty;
    }

    public String getValue() {
        return this.valueProperty.getValue();
    }

    public StringProperty valueProperty() {
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

    public boolean isPrimaryKeyProperty() {
        return primaryKeyProperty.get();
    }

    public SimpleBooleanProperty primaryKeyPropertyProperty() {
        return primaryKeyProperty;
    }

    public void setPrimaryKeyProperty(boolean primaryKeyProperty) {
        this.primaryKeyProperty.set(primaryKeyProperty);
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

    public String getName() {
        return name;
    }

    public String getCharset() {
        return charset;
    }

    public String getCollation() {
        return collation;
    }

    @Override
    public void destroy() {
        this.typeProperty.unbind();
        this.valueProperty.unbind();
    }
}
