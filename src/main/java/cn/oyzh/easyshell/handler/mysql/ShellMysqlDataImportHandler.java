package cn.oyzh.easyshell.handler.mysql;

import cn.oyzh.easyshell.db.handler.DBDataImportHandler;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;

/**
 *
 * @author oyzh
 * @since 2025-11-26
 */
public class ShellMysqlDataImportHandler extends DBDataImportHandler {

    public ShellMysqlDataImportHandler(ShellMysqlClient dbClient, String dbName) {
        super(dbClient, dbName);
    }
}
