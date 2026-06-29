package cn.oyzh.easyshell.mongo.script.function;

import org.bson.types.ObjectId;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.internal.runtime.Undefined;

/**
 *
 * @author oyzh
 * @since 2026-06-17
 */
public class MongoScriptObjectIdFunction extends AbstractJSObject {

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
            return new ObjectId();
        }
        try {
            if (args[0] instanceof CharSequence sequence) {
                return new ObjectId(sequence.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ObjectId();
    }
}