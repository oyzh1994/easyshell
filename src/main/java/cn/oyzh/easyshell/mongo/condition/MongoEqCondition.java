package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.mongo.util.MongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

/**
 * 等于条件
 * @author oyzh
 * @since 2024/6/27
 */
public class MongoEqCondition extends MongoCondition {

    public final static MongoEqCondition INSTANCE = new MongoEqCondition();

    public MongoEqCondition() {
        super(I18nHelper.eq(), "=");
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        Bson bson1;
        if (MongoUtil.ID.equals(columnName)) {
            bson1 = Filters.expr(
                    new Document("$eq", Arrays.asList(
                            new Document("$toString", "$_id"),
                            condition
                    ))
            );
        } else {
            bson1 = Filters.and(Filters.exists(columnName), Filters.eq(columnName, condition));
        }
        return bson1;
    }
}
