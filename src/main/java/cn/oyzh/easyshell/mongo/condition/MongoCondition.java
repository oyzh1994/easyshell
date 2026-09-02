package cn.oyzh.easyshell.mongo.condition;


import cn.oyzh.fx.db.condition.DBCondition;
import org.bson.conversions.Bson;

/**
 * 条件
 *
 * @author oyzh
 * @since 2024/06/26
 */
public abstract class MongoCondition extends DBCondition {


    public MongoCondition() {
        super();
    }

    public MongoCondition(String name, String value) {
        super(name, value);
    }

    public MongoCondition(String name, String value, boolean requireCondition) {
        super(name, value, requireCondition);
    }

    @Override
    public Bson wrapCondition(String columnName) {
        return (Bson) super.wrapCondition(columnName);
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        return null;
    }
}
