package cn.oyzh.easyshell.mongo;

import cn.oyzh.easyshell.domain.ShellConnect;

/**
 *
 * @author oyzh
 * @since 2026-06-08
 */
public class MongoClientUtil {

    public static ShellMongoClient newClient(ShellConnect connect) {
        return new ShellMongoClient(connect);
    }
}
