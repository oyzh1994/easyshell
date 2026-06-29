package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.util.mongo.MongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

/**
 * 大于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MongoGtCondition extends MongoCondition {

    public final static MongoGtCondition INSTANCE = new MongoGtCondition();

    public MongoGtCondition() {
        super(I18nHelper.gt(), ">");
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        Bson bson1;
        if (MongoUtil.ID.equals(columnName)) {
            bson1 = Filters.expr(
                    new Document("$gt", Arrays.asList(
                            new Document("$toString", "$_id"),
                            condition
                    ))
            );
        } else {
            bson1 = Filters.and(Filters.exists(columnName), Filters.gt(columnName, condition));
        }
        return bson1;
    }

}
