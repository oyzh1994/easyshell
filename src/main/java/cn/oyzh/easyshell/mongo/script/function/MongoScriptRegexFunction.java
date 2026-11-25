package cn.oyzh.easyshell.mongo.script.function;

import org.bson.BsonRegularExpression;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;

/**
 * MongoDB 正则构造函数，避免 Nashorn 内置 RegExp 无法直接编码为 BSON。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoScriptRegexFunction extends AbstractJSObject {

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public Object call(Object obj, Object... args) {
        String pattern = args != null && args.length > 0 && args[0] != null
                ? args[0].toString() : "";
        String options = args != null && args.length > 1 && args[1] != null
                ? args[1].toString() : "";
        return new BsonRegularExpression(pattern, options);
    }
}
