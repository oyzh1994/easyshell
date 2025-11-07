package cn.oyzh.easyshell.util.mysql;

import cn.oyzh.easyshell.controller.mysql.data.MysqlDataDumpController;
import cn.oyzh.easyshell.controller.mysql.data.MysqlDataExportController;
import cn.oyzh.easyshell.controller.mysql.data.MysqlDataImportController;
import cn.oyzh.easyshell.controller.mysql.data.MysqlDataTransportController;
import cn.oyzh.easyshell.controller.mysql.data.MysqlRunSqlFileController;
import cn.oyzh.easyshell.controller.mysql.database.MysqlDatabaseUpdateController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.easyshell.trees.mysql.root.MysqlRootTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * msyql页面工厂
 *
 * @author oyzh
 * @since 2025-11-07
 */
public class ShellMysqlViewFactory {

    /**
     * 导出数据
     *
     * @param client    客户端
     * @param dbName    数据库名称
     * @param tableName 表名称
     */
    public static void exportData(MysqlClient client, String dbName, String tableName) {
        try {
            StageAdapter adapter = StageManager.parseStage(MysqlDataExportController.class, StageManager.getFrontWindow());
            adapter.setProp("dbName", dbName);
            adapter.setProp("dbClient", client);
            adapter.setProp("tableName", tableName);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 导入数据
     *
     * @param client 客户端
     * @param dbName 数据库名称
     */
    public static void importData(MysqlClient client, String dbName) {
        try {
            StageAdapter adapter = StageManager.parseStage(MysqlDataImportController.class, StageManager.getFrontWindow());
            adapter.setProp("dbName", dbName);
            adapter.setProp("dbClient", client);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 转储数据
     *
     * @param client    客户端
     * @param dbName    数据库名称
     * @param tableName 表名称
     */
    public static void dumpData(MysqlClient client, String dbName, String tableName, int dumpType) {
        try {
            StageAdapter adapter = StageManager.parseStage(MysqlDataDumpController.class, StageManager.getFrontWindow());
            adapter.setProp("dumpType", dumpType);
            adapter.setProp("dbName", dbName);
            adapter.setProp("dbClient", client);
            adapter.setProp("tableName", tableName);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 运行sql文件
     *
     * @param client 客户端
     * @param dbName 数据库名称
     */
    public static void runSqlFile(MysqlClient client, String dbName) {
        try {
            StageAdapter adapter = StageManager.parseStage(MysqlRunSqlFileController.class, StageManager.getFrontWindow());
            adapter.setProp("dbName", dbName);
            adapter.setProp("dbClient", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 编辑数据库
     *
     * @param database 数据库
     * @param treeItem 树节点
     */
    public static void databaseUpdate(MysqlDatabase database, MysqlRootTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(MysqlDatabaseUpdateController.class, StageManager.getFrontWindow());
            adapter.setProp("database", database);
            adapter.setProp("connectItem", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 传输数据
     *
     * @param connect 连接
     * @param dbName  数据库
     */
    public static void transportData(ShellConnect connect, String dbName) {
        try {
            StageAdapter adapter = StageManager.parseStage(MysqlDataTransportController.class, StageManager.getFrontWindow());
            adapter.setProp("connect", connect);
            adapter.setProp("dbName", dbName);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
