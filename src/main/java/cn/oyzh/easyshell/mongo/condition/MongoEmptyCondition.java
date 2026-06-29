package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MongoEmptyCondition extends MongoCondition {

    public final static MongoEmptyCondition INSTANCE = new MongoEmptyCondition();

    public MongoEmptyCondition() {
        super(I18nHelper.isEmpty(), "=''", false);
    }

    @Override
    public Bson wrapCondition(String columnName, Object condition) {
        return Filters.eq(columnName, "");
    }
}
