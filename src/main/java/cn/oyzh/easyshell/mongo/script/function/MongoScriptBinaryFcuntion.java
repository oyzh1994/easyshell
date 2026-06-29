package cn.oyzh.easyshell.mongo.script.function;

import cn.oyzh.common.util.Base64Util;
import org.bson.types.Binary;

/**
 *
 * @author oyzh
 * @since 2026-06-08
 */
public class MongoScriptBinaryFcuntion {

    public Binary createFromBase64() {
        return this.createFromBase64("", 0);
    }

    public Binary createFromBase64(String base64, int type) {
        byte[] data = Base64Util.decode(base64);
        return new Binary((byte) type, data);
    }
}
