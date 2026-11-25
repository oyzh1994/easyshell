package cn.oyzh.easyshell.mongo.script.function;

import org.bson.types.Decimal128;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.internal.runtime.Undefined;

/**
 * MongoDB NumberDecimal 构造函数
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoScriptNumberDecimalFunction extends AbstractJSObject {

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public Object call(Object obj, Object... args) {
        if (args == null || args.length == 0
                || args[0] == null || args[0] instanceof Undefined) {
            return Decimal128.parse("0");
        }
        return Decimal128.parse(args[0].toString());
    }
}
