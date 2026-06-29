package cn.oyzh.easyshell.data.mongo.handler;


import cn.oyzh.easyshell.mongo.ShellMongoClient;

/**
 *
 * @author oyzh
 * @since 2025-11-26
 */
public class ShellMongoDataImportHandler extends DBDataImportHandler {

    public ShellMongoDataImportHandler(ShellMongoClient dbClient, String dbName) {
        super(dbClient, dbName);
    }
}
