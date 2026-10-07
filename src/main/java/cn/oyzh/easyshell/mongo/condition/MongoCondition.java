package cn.oyzh.easyshell.mongo.condition;


import cn.oyzh.fx.db.condition.DBCondition;
import org.bson.conversions.Bson;

/**
 * 条件
 *
 * @author oyzh
 * @since 2025-11-06
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
    public Bson wrapCondition(String columnName, Object condition) {
        return null;
    }
}
