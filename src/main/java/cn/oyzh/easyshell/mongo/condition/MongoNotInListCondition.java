package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.mongo.util.MongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

/**
 * 不在列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MongoNotInListCondition extends MongoInListCondition {

    public final static MongoNotInListCondition INSTANCE = new MongoNotInListCondition();

    public MongoNotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        List<?> list = null;
        if (condition instanceof String str) {
            String[] arr = str.split(",");
            list = Arrays.asList(arr);
        } else if (condition instanceof List<?> l) {
            list = l;
        }
        if (list == null) {
            return null;
        }
        Bson bson1;
        if (MongoUtil.ID.equals(columnName)) {
            bson1 = Filters.expr(
                    new Document("$not",
                            new Document("$in", Arrays.asList(
                                    new Document("$toString", "$_id"),
                                    list
                            ))
                    )
            );
        } else {
            bson1 = Filters.and(Filters.exists(columnName), Filters.nin(columnName, list));
        }
        return bson1;
    }
}
