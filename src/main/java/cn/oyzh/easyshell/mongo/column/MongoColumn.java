package cn.oyzh.easyshell.mongo.column;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.DBObjectStatus;
import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

/**
 * mongodb字段
 *
 * @author oyzh
 * @since 2023/12/20
 */
public class MongoColumn extends DBObjectStatus implements ObjectCopier<MongoColumn> {

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 集合名称
     */
    private String collectionName;

    /**
     * 字段类型
     */
    private final StringProperty typeProperty = new SimpleStringProperty();

    /**
     * 字段值
     */
    private String value;

    /**
     * 名称
     */
    private String name;

    /**
     * 别名，优先name显示
     */
    private String aliasName;

    public MongoColumn() {

    }

    public MongoColumn(String name) {
        this.name = name;
    }

    public MongoColumn(String name, String aliasName) {
        this.name = name;
        this.aliasName = aliasName;
    }

    public boolean isNameChanged() {
        return super.checkOriginalData("name", this.name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public void setType(String type) {
        type = StringUtil.toUpperCase(type);
        this.typeProperty.set(type);
        super.putOriginalData("type", type);
    }

    public void setValue(String value) {
        this.value = value;
        super.putOriginalData("value", value);
    }

    /**
     * 是否支持小数
     *
     * @return 结果
     */
    public boolean supportDigits() {
        return StringUtil.equalsIgnoreCase(this.getType(), "double");
    }

    /**
     * 是否支持32位整数
     *
     * @return 结果
     */
    public boolean supportInt32() {
        return StringUtil.equalsIgnoreCase(this.getType(), "int");
    }

    /**
     * 是否支持64位整数
     *
     * @return 结果
     */
    public boolean supportInt64() {
        return StringUtil.equalsIgnoreCase(this.getType(), "long");
    }

    /**
     * 是否支持字符
     *
     * @return 结果
     */
    public boolean supportString() {
        return StringUtil.equalsIgnoreCase(this.getType(), "string");
    }

    /**
     * 是否支持日期
     *
     * @return 结果
     */
    public boolean supportDate() {
        return StringUtil.equalsIgnoreCase(this.getType(), "date");
    }

    /**
     * 是否支持布尔
     *
     * @return 结果
     */
    public boolean supportBoolean() {
        return StringUtil.equalsIgnoreCase(this.getType(), "boolean");
    }

    /**
     * 是否支持集合
     *
     * @return 结果
     */
    public boolean supportList() {
        return StringUtil.equalsIgnoreCase(this.getType(), "list");
    }

    /**
     * 是否支持对象
     *
     * @return 结果
     */
    public boolean supportObject() {
        return StringUtil.equalsIgnoreCase(this.getType(), "object");
    }

    /**
     * 是否支持二进制
     *
     * @return 结果
     */
    public boolean supportBinary() {
        return StringUtil.equalsIgnoreCase(this.getType(), "binary");
    }

    /**
     * 是否支持对象id
     *
     * @return 结果
     */
    public boolean supportObjectId() {
        return StringUtil.equalsIgnoreCase(this.getType(), "obejectid");
    }

    /**
     * 是否支持代码
     *
     * @return 结果
     */
    public boolean supportCode() {
        return StringUtil.equalsIgnoreCase(this.getType(), "code");
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    @Override
    public void initStatus() {
        if (this.value == null) {
            this.setValue(null);
        }
    }

    @Override
    public void copy(MongoColumn column) {
        if (column != null) {
            this.setName(column.name);
            this.setType(column.getType());
            this.setValue(column.value);
            this.setDbName(column.dbName);
            this.setCollectionName(column.collectionName);
        }
    }

    public boolean isInvalid() {
        return StringUtil.isBlank(this.getName()) || StringUtil.isBlank(this.getType());
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getType() {
        return typeProperty.get();
    }

    public StringProperty typeProperty() {
        return typeProperty;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public boolean is_id() {
        return ShellMongoUtil.ID.equalsIgnoreCase(this.name);
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String displayName() {
        return this.aliasName == null ? this.name : this.aliasName;
    }

    public boolean supportInteger() {
        return this.supportInt32() || this.supportInt64();
    }

    public Object defaultValue() {
        if (this.is_id() || this.supportObjectId()) {
            return null;
        }
        if (this.supportInt32()) {
            return 0;
        }
        if (this.supportInt64()) {
            return 0L;
        }
        if (this.supportDigits()) {
            return 0d;
        }
        if (this.supportObject()) {
            return "{}";
        }
        if (this.supportList()) {
            return "[]";
        }
        if (this.supportDate()) {
            return ShellMongoUtil.DATE_FORMAT.format(new Date());
        }
        if (this.supportBinary()) {
            return new byte[]{};
        }
        if (this.supportBoolean()) {
            return false;
        }
        if (this.supportCode()) {
            return """
                    function func(){}
                    """;
        }
        return "";
    }
}
