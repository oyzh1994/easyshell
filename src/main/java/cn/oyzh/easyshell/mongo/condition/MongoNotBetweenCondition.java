package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.util.mongo.MongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

/**
 * 不介于列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MongoNotBetweenCondition extends MongoBetweenCondition {

    public final static MongoNotBetweenCondition INSTANCE = new MongoNotBetweenCondition();

    public MongoNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        Bson bson1;
        List<?> list = (List<?>) condition;
        Object f = list.getFirst();
        Object l = list.getLast();
        if (MongoUtil.ID.equals(columnName)) {
            bson1 = Filters.expr(
                    new Document("$or", Arrays.asList(
                            new Document("$lt", Arrays.asList(new Document("$toString", "$_id"), f)),
                            new Document("$gt", Arrays.asList(new Document("$toString", "$_id"), l))
                    ))
            );
        } else {
            bson1 = Filters.and(Filters.exists(columnName), Filters.or(Filters.lt(columnName, f), Filters.gt(columnName, l)));
        }
        return bson1;
    }
}
