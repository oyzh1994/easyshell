package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

/**
 * 不是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MongoNotNullCondition extends MongoCondition {

    public final static MongoNotNullCondition INSTANCE = new MongoNotNullCondition();

    public MongoNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        Bson bson1;
        if (ShellMongoUtil.ID.equals(columnName)) {
            bson1 = Filters.expr(
                    new Document("$ne", Arrays.asList(
                            new Document("$toString", "$_id"),
                            null
                    ))
            );
        } else {
            bson1 = Filters.and(
                    Filters.exists(columnName),
                    Filters.ne(columnName, null)
            );
        }
        return bson1;
    }
}
