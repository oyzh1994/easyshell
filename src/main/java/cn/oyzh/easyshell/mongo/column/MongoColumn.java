package cn.oyzh.easyshell.mongo.column;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.data.db.DBObjectStatus;
import cn.oyzh.easyshell.db.DBColumn;
import cn.oyzh.easyshell.db.DBColumnFieldManager;
import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.bson.Document;
import org.bson.types.Code;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * mongodb字段
 *
 * @author oyzh
 * @since 2023/12/20
 */
public class MongoColumn extends DBObjectStatus implements DBColumn, ObjectCopier<MongoColumn> {

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

    @Override
    public void setType(String type) {
        type = StringUtil.toUpperCase(type);
        this.typeProperty.set(type);
        super.putOriginalData("type", type);
    }

    public void setValue(String value) {
        this.value = value;
        super.putOriginalData("value", value);
    }

    @Override
    public boolean supportDigits() {
        return DBColumnFieldManager.supportDigits(DBDialect.MONGODB, this.getType());
    }

    @Override
    public boolean supportInteger() {
        return DBColumnFieldManager.supportInteger(DBDialect.MONGODB, this.getType());
    }

    @Override
    public boolean supportBigInteger() {
        return DBColumnFieldManager.supportBigInteger(DBDialect.MONGODB, this.getType());
    }

    @Override
    public boolean supportString() {
        return DBColumnFieldManager.supportString(DBDialect.MONGODB, this.getType());
    }

    @Override
    public boolean supportBoolean() {
        return DBColumnFieldManager.supportBoolean(DBDialect.MONGODB, this.getType());
    }

    @Override
    public boolean supportJson() {
        return DBColumnFieldManager.supportJson(DBDialect.MONGODB, this.getType());
    }

    @Override
    public boolean supportJsonArray() {
        return DBColumnFieldManager.supportJsonArray(DBDialect.MONGODB, this.getType());
    }

    @Override
    public boolean supportBinary() {
        return DBColumnFieldManager.supportBinary(DBDialect.MONGODB, this.getType());
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

    @Override
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
            this.setName(column.getName());
            this.setType(column.getType());
            this.setValue(column.getValue());
            this.setDbName(column.getDbName());
            this.setAliasName(column.getAliasName());
            this.setCollectionName(column.getCollectionName());
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

    @Override
    public String getType() {
        return typeProperty.get();
    }

    public StringProperty typeProperty() {
        return typeProperty;
    }

    public String getValue() {
        return value;
    }

    @Override
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

    @Override
    public boolean supportTimestamp() {
        return StringUtil.equalsIgnoreCase(this.getType(), "date");
    }

    public Object defaultValue() {
        // 对象id
        if (this.supportObjectId()) {
            return null;
        }
        // id字段
        if (this.is_id()) {
            if (this.supportString()) {
                return new ObjectId().toHexString();
            }
            if (this.supportBinary()) {
                return new ObjectId().toByteArray();
            }
            return null;
        }
        if (this.supportInteger()) {
            return 0;
        }
        if (this.supportBigInteger()) {
            return 0L;
        }
        if (this.supportDigits()) {
            return 0d;
        }
        if (this.supportJson() || this.supportJsonArray()) {
            return new Document();
        }
        if (this.supportTimestamp()) {
            return new Date();
        }
        if (this.supportBinary()) {
            return new byte[]{};
        }
        if (this.supportBoolean()) {
            return false;
        }
        if (this.supportCode()) {
            return new Code("""
                    function func(){}
                    """);
        }
        return "";
    }
}
