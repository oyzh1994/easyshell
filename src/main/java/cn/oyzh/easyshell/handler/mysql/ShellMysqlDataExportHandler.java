package cn.oyzh.easyshell.handler.mysql;

import cn.oyzh.easyshell.db.handler.DBDataExportHandler;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;

/**
 *
 * @author oyzh
 * @since 2025-11-26
 */
public class ShellMysqlDataExportHandler extends DBDataExportHandler {

    public ShellMysqlDataExportHandler(ShellMysqlClient dbClient, String dbName) {
        super(dbClient, dbName);
    }
}
