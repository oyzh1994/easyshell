package cn.oyzh.easyshell.mongo.condition;


import org.bson.conversions.Bson;

/**
 * 条件
 *
 * @author oyzh
 * @since 2024/06/26
 */
public abstract class MongoCondition {

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private String value;

    /**
     * 需要条件标志位
     */
    private boolean requireCondition = true;

    public MongoCondition() {

    }

    public MongoCondition(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public MongoCondition(String name, String value, boolean requireCondition) {
        this.name = name;
        this.value = value;
        this.requireCondition = requireCondition;
    }

    public Bson wrapCondition(String columnName) {
        return this.wrapCondition(columnName, null);
    }

    public Bson wrapCondition(String columnName, Object condition) {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isRequireCondition() {
        return requireCondition;
    }

    public void setRequireCondition(boolean requireCondition) {
        this.requireCondition = requireCondition;
    }
}
