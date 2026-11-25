package cn.oyzh.easyshell.mongo.script.function;

import org.bson.types.MinKey;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;

/**
 * MongoDB MinKey 构造函数
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoScriptMinKeyFunction extends AbstractJSObject {

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public Object call(Object obj, Object... args) {
        return new MinKey();
    }
}
