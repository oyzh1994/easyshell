package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.util.mongo.MongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.regex.Pattern;

/**
 * 不是结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MongoNotEndWithCondition extends MongoEndWithCondition {

    public final static MongoNotEndWithCondition INSTANCE = new MongoNotEndWithCondition();

    public MongoNotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        String quote = Pattern.quote(condition.toString());
        Pattern pattern = Pattern.compile(quote + "$", Pattern.CASE_INSENSITIVE);
        Bson bson1;
        if (MongoUtil.ID.equals(columnName)) {
            bson1 = MongoConditionUtil.idFilterRegexNot(pattern);
        } else {
            bson1 = Filters.and(Filters.exists(columnName), Filters.not(Filters.regex(columnName, pattern)));
        }
        return bson1;
    }
}
