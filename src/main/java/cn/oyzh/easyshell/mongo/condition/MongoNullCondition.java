package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

/**
 * 是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MongoNullCondition extends MongoCondition {

    public final static MongoNullCondition INSTANCE = new MongoNullCondition();

    public MongoNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        return Filters.or(
                Filters.exists(columnName, false),
                Filters.eq(columnName, null)
        );
    }
}
