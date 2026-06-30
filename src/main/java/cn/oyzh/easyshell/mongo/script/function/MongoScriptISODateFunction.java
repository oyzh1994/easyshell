package cn.oyzh.easyshell.mongo.script.function;

import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.internal.runtime.Undefined;

import java.util.Date;

/**
 *
 * @author oyzh
 * @since 2026-06-17
 */
public class MongoScriptISODateFunction extends AbstractJSObject {

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
            return new Date();
        }
        if (!args[0].toString().isEmpty()) {
            try {
                return ShellMongoUtil.DATE_FORMAT.parse(args[0].toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new Date();
    }
}