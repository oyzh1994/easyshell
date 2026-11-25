package cn.oyzh.easyshell.mongo.script.function;

import org.bson.BsonTimestamp;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.internal.runtime.Undefined;

/**
 * MongoDB Timestamp 构造函数
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoScriptTimestampFunction extends AbstractJSObject {

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public Object call(Object obj, Object... args) {
        if (args == null || args.length == 0
                || args[0] == null || args[0] instanceof Undefined) {
            return new BsonTimestamp();
        }
        int seconds = args[0] instanceof Number number
                ? number.intValue() : Integer.parseInt(args[0].toString());
        int increment = args.length > 1 && args[1] instanceof Number number
                ? number.intValue() : 0;
        return new BsonTimestamp(seconds, increment);
    }
}
