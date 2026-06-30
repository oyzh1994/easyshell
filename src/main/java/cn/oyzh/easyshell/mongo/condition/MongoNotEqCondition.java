package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

/**
 * 不等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MongoNotEqCondition extends MongoCondition {

    public final static MongoNotEqCondition INSTANCE = new MongoNotEqCondition();

    public MongoNotEqCondition() {
        super(I18nHelper.notEq(), "!=");
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        Bson bson1;
        if (ShellMongoUtil.ID.equals(columnName)) {
            bson1 = Filters.expr(
                    new Document("$ne", Arrays.asList(
                            new Document("$toString", "$_id"),
                            condition
                    ))
            );
        } else {
            bson1 = Filters.and(Filters.exists(columnName), Filters.ne(columnName, condition));
        }
        return bson1;
    }
}
