package cn.oyzh.easyshell.mongo.script.function;

import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.internal.runtime.Undefined;

/**
 *
 * @author oyzh
 * @since 2026-06-17
 */
public class MongoScriptInit32Function extends AbstractJSObject {

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public Object call(Object obj, Object... args) {
        if (args == null
                || args.length == 0
                || args[0] == null
                || args[0] instanceof Undefined) {
            return 0;
        }
        if (args[0] instanceof Number) {
            return ((Number) args[0]).intValue();
        }
        return Integer.parseInt(args[0].toString());
    }
}