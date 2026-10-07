package cn.oyzh.easyshell.mysql.column;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MySQL字段
 *
 * @author oyzh
 * @since 2023/12/20
 */
public class MysqlColumn extends DBObject implements DBColumn, ObjectCopier<MysqlColumn> {

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
     * 字段字符集
     */
    private String charset;

    /**
     * 字段排序规则
     */
    private String collation;

    /**
     * 构造字段
     */
    public MysqlColumn() {

    }

    /**
     * 构造字段
     *
     * @param name 名称
     */
    public MysqlColumn(String name) {
        this.name = name;
    }

    /**
     * 名称是否变更
     *
     * @return 结果
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

    /**
     * 获取值列表
     *
     * @return 值列表
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
     * 获取默认值字符串
     *
     * @return 默认值字符串
     */
    public String getDefaultValueString() {
        Object defaultValue = this.defaultValue;
        return defaultValue == null ? null : defaultValue.toString();
    }

    /**
     * 获取修正后的默认值
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
        if (this.supportEnum()) {
            if (StringUtil.isBlank(valStr) || StringUtil.equalsIgnoreCase(valStr, "null")) {
                return null;
            }
        }
        if (this.supportTimestamp()) {
            if (StringUtil.equalsIgnoreCase(valStr, "null")) {
                return null;
            }
            if (StringUtil.equalsIgnoreCase(valStr, "CURRENT_TIMESTAMP")) {
                return valStr;
            }
        }
        return defaultValue;
    }

    /**
     * 设置自动递增
     *
     * @param autoIncrement 自动递增
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
     * @return 结果
     */
    public boolean isAutoIncrement() {
        return BooleanUtil.isTrue(this.autoIncrement);
    }

    /**
     * 是否包含注释
     *
     * @return 结果
     */
    public boolean hasComment() {
        return this.getComment() != null;
    }

    /**
     * 设置字符集
     *
     * @param charset 字符集
     */
    public void setCharset(String charset) {
        this.charset = charset;
        super.putOriginalData("charset", charset);
    }

    /**
     * 设置排序规则
     *
     * @param collation 排序规则
     */
    public void setCollation(String collation) {
        this.collation = collation;
        super.putOriginalData("collation", collation);
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(String value) {
        this.valueProperty().setValue(value);
        super.putOriginalData("value", value);
    }

    /**
     * 设置无符号
     *
     * @param unsigned 无符号
     */
    public void setUnsigned(Boolean unsigned) {
        this.unsigned = unsigned;
        super.putOriginalData("unsigned", unsigned);
    }

    /**
     * 是否无符号模式
     *
     * @return 结果
     */
    public boolean isUnsigned() {
        return BooleanUtil.isTrue(this.unsigned);
    }

    /**
     * 设置根据当前时间戳更新
     *
     * @param updateOnCurrentTimestamp 根据当前时间戳更新
     */
    public void setUpdateOnCurrentTimestamp(Boolean updateOnCurrentTimestamp) {
        this.updateOnCurrentTimestamp = updateOnCurrentTimestamp;
        super.putOriginalData("updateOnCurrentTimestamp", updateOnCurrentTimestamp);
    }

    /**
     * 是否根据当前时间戳更新
     *
     * @return 结果
     */
    public boolean isUpdateOnCurrentTimestamp() {
        return BooleanUtil.isTrue(this.updateOnCurrentTimestamp);
    }

    @Override
    public boolean supportSize() {
        return DBColumnFieldManager.supportSize(DBDialect.MYSQL, this.getType());
    }

    /**
     * 获取推荐长度
     *
     * @return 推荐长度
     */
    public Integer suggestSize() {
        return DBColumnFieldManager.suggestSize(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportGeometry() {
        return DBColumnFieldManager.supportGeometry(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportCharset() {
        return DBColumnFieldManager.supportCharset(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportUnsigned() {
        return DBColumnFieldManager.supportUnsigned(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportDigits() {
        return DBColumnFieldManager.supportDigits(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportInteger() {
        return DBColumnFieldManager.supportInteger(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportAutoIncrement() {
        return DBColumnFieldManager.supportAutoIncrement(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportDefaultValue() {
        return DBColumnFieldManager.supportDefaultValue(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportTimestamp() {
        return DBColumnFieldManager.supportTimestamp(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportValue() {
        return DBColumnFieldManager.supportValue(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportText() {
        return DBColumnFieldManager.supportText(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportZeroFill() {
        return DBColumnFieldManager.supportZeroFill(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportBit() {
        return DBColumnFieldManager.supportBit(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportJson() {
        return DBColumnFieldManager.supportJson(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportKeySize() {
        return DBColumnFieldManager.supportKeySize(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportString() {
        return DBColumnFieldManager.supportString(DBDialect.MYSQL, this.getType());
    }

    @Override
    public Long minValue() {
        return DBColumnFieldManager.minValue(DBDialect.MYSQL, this.getType());
    }

    @Override
    public Long maxValue() {
        return DBColumnFieldManager.maxValue(DBDialect.MYSQL, this.getType());
    }

    @Override
    public Object exampleValue() {
        return DBColumnFieldManager.exampleValue(DBDialect.MYSQL, this.getType());
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
     * 设置字段大小
     *
     * @param size 字段大小
     */
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
     * 设置可为null
     *
     * @param nullable 可为null
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
     * @return 结果
     */
    public boolean isPrimaryKey() {
        return this.primaryKeyProperty != null && this.primaryKeyProperty.get();
    }

    /**
     * 设置主键
     *
     * @param primaryKey 主键
     */
    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKeyProperty().set(primaryKey);
        super.putOriginalData("primaryKey", primaryKey);
    }

    /**
     * 字段是否变更
     *
     * @return 结果
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
     * 设置填充零
     *
     * @param zeroFill 填充零
     */
    public void setZeroFill(Boolean zeroFill) {
        this.zeroFill = zeroFill;
        super.putOriginalData("zeroFill", zeroFill);
    }

    /**
     * 是否填充零
     *
     * @return 结果
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
        // if (this.primaryKey != null) {
        //     this.primaryKey.setPrimaryKeySize(primaryKeySize);
        // }
    }

    /**
     * 是否可为null
     *
     * @return 结果
     */
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

    /**
     * 是否为year类型
     *
     * @return 结果
     */
    public boolean isYearType() {
        return ShellMysqlColumnUtil.isYearType(this.getType());
    }

    /**
     * 是否为date类型
     *
     * @return 结果
     */
    public boolean isDateType() {
        return ShellMysqlColumnUtil.isDateType(this.getType());
    }

    /**
     * 是否为datetime类型
     *
     * @return 结果
     */
    public boolean isDateTimeType() {
        return ShellMysqlColumnUtil.isDateTimeType(this.getType());
    }

    /**
     * 是否为geometry类型
     *
     * @return 结果
     */
    public boolean isGeometryType() {
        return ShellMysqlColumnUtil.isGeometryType(this.getType());
    }

    /**
     * 是否为time类型
     *
     * @return 结果
     */
    public boolean isTimeType() {
        return ShellMysqlColumnUtil.isTimeType(this.getType());
    }

    @Override
    public boolean supportBinary() {
        return DBColumnFieldManager.supportBinary(DBDialect.MYSQL, this.getType());
    }

    @Override
    public boolean supportEnum() {
        return DBColumnFieldManager.supportEnum(DBDialect.MYSQL, this.getType());
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
            // this.primaryKey = new MysqlPrimaryKey();
            // this.primaryKey.setPrimaryKey(true);
            this.setPrimaryKey(true);
        } else {
            this.setPrimaryKey(false);
        }
    }

    /**
     * 解析字段类型
     *
     * @param type 字段类型
     * @return 字段类型
     */
    private String parseType(String type) {
        if (!type.contains("(") && !type.contains(" ")) {
            return type;
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
            return type;
        }

        String _type = type.substring(0, type.indexOf("("));
        String sub1 = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        this.typeProperty().setValue(_type);
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
        return _type;
    }

    /**
     * 解析额外信息
     *
     * @param extra 额外信息
     */
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

    /**
     * 解析排序规则
     *
     * @param collation 排序规则
     */
    public void parseCollation(String collation) {
        if (StringUtil.isEmpty(collation)) {
            return;
        }
        this.setCollation(collation);
        this.setCharset(collation.substring(0, collation.indexOf("_")));
    }

    /**
     * 初始化字段
     *
     * @param columnType  字段类型
     * @param columnExtra 字段额外信息
     */
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

    /**
     * 是否包含默认值
     *
     * @return 结果
     */
    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    @Override
    public void copy(MysqlColumn column) {
        if (column != null) {
            this.setSize(column.getSize());
            this.setName(column.getName());
            this.setType(column.getType());
            this.setValue(column.getValue());
            this.setDbName(column.getDbName());
            this.setDigits(column.getDigits());
            this.setComment(column.getComment());
            this.setCharset(column.getCharset());
            this.setNullable(column.isNullable());
            this.setUnsigned(column.isUnsigned());
            this.setZeroFill(column.isZeroFill());
            this.setTableName(column.getTableName());
            this.setCollation(column.getCollation());
            this.setPrimaryKey(column.isPrimaryKey());
            this.setDefaultValue(column.getDefaultValue());
            this.setAutoIncrement(column.isAutoIncrement());
            this.setPrimaryKeySize(column.getPrimaryKeySize());
            this.setUpdateOnCurrentTimestamp(column.isUpdateOnCurrentTimestamp());
        }
    }

    /**
     * 获取库名称
     *
     * @return 库名称
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置库名称
     *
     * @param dbName 库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
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

    /**
     * 获取字段大小
     *
     * @return 字段大小
     */
    public Integer getSize() {
        return size;
    }

    @Override
    public void setType(String type) {
        if (type != null) {
            type = this.parseType(type);
            type = type.toUpperCase();
        }
        this.typeProperty().set(type);
        super.putOriginalData("type", type);
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
     * 获取可为null
     *
     * @return 可为null
     */
    public Boolean getNullable() {
        return nullable;
    }

    /**
     * 获取无符号
     *
     * @return 无符号
     */
    public Boolean getUnsigned() {
        return unsigned;
    }

    /**
     * 获取填充零
     *
     * @return 填充零
     */
    public Boolean getZeroFill() {
        return zeroFill;
    }

    /**
     * 获取根据当前时间戳更新
     *
     * @return 根据当前时间戳更新
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

    //    public boolean isPrimaryKeyProperty() {
    //        return primaryKeyProperty.get();
    //    }
    //
    //    public SimpleBooleanProperty primaryKeyPropertyProperty() {
    //        return primaryKeyProperty;
    //    }
    //
    //    public void setPrimaryKeyProperty(boolean primaryKeyProperty) {
    //        this.primaryKeyProperty.set(primaryKeyProperty);
    //    }

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
     * 获取自动递增
     *
     * @return 自动递增
     */
    public Boolean getAutoIncrement() {
        return autoIncrement;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取字符集
     *
     * @return 字符集
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 获取排序规则
     *
     * @return 排序规则
     */
    public String getCollation() {
        return collation;
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
