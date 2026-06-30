package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.regex.Pattern;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MongoContainsCondition extends MongoCondition {

    public final static MongoContainsCondition INSTANCE = new MongoContainsCondition();

    public MongoContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    public MongoContainsCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        String quote = Pattern.quote(condition.toString());
        Pattern pattern = Pattern.compile(quote, Pattern.CASE_INSENSITIVE);
        Bson bson1;
        if (ShellMongoUtil.ID.equals(columnName)) {
            bson1 = MongoConditionUtil.idFilterRegex(pattern);
        } else {
            bson1 = Filters.and(Filters.exists(columnName), Filters.regex(columnName, pattern));
        }
        return bson1;
    }
}
