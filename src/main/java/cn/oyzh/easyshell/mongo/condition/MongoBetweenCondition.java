package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

/**
 * 介于条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MongoBetweenCondition extends MongoCondition {

    public final static MongoBetweenCondition INSTANCE = new MongoBetweenCondition();

    public MongoBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    public MongoBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        Bson bson1;
        List<?> list = (List<?>) condition;
        Object f = list.getFirst();
        Object l = list.getLast();
        if (ShellMongoUtil.ID.equals(columnName)) {
            bson1 = Filters.expr(
                    new Document("$and", Arrays.asList(
                            new Document("$gte", Arrays.asList(new Document("$toString", "$_id"), f)),
                            new Document("$lte", Arrays.asList(new Document("$toString", "$_id"), l))
                    ))

            );
        } else {
            bson1 = Filters.and(Filters.exists(columnName), Filters.gte(columnName, f), Filters.lte(columnName, l));
        }
        return bson1;
    }

}
