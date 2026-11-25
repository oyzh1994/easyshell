package cn.oyzh.easyshell.mongo.script.function;

import org.bson.types.MaxKey;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;

/**
 * MongoDB MaxKey 构造函数
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoScriptMaxKeyFunction extends AbstractJSObject {

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public Object call(Object obj, Object... args) {
        return new MaxKey();
    }
}
