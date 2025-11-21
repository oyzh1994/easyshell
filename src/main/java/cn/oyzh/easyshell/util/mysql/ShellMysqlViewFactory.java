package cn.oyzh.easyshell.util.mysql;

import cn.oyzh.easyshell.controller.mysql.data.ShellMysqlDataDumpController;
import cn.oyzh.easyshell.controller.mysql.data.ShellMysqlDataExportController;
import cn.oyzh.easyshell.controller.mysql.data.ShellMysqlDataImportController;
import cn.oyzh.easyshell.controller.mysql.data.ShellMysqlDataTransportController;
import cn.oyzh.easyshell.controller.mysql.data.ShellMysqlRunSqlFileController;
import cn.oyzh.easyshell.controller.mysql.database.ShellMysqlDatabaseAddController;
import cn.oyzh.easyshell.controller.mysql.database.ShellMysqlDatabaseUpdateController;
import cn.oyzh.easyshell.controller.mysql.event.ShellMysqlEventInfoController;
import cn.oyzh.easyshell.controller.mysql.function.ShellMysqlFunctionInfoController;
import cn.oyzh.easyshell.controller.mysql.procedure.ShellMysqlProcedureInfoController;
import cn.oyzh.easyshell.controller.mysql.table.ShellMysqlTableInfoController;
import cn.oyzh.easyshell.controller.mysql.view.ShellMysqlViewInfoController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataExportTable;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.trees.mysql.event.ShellMysqlEventTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.ShellMysqlFunctionTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.ShellMysqlProcedureTreeItem;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTableTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
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
    public static void exportData(ShellMysqlClient client, String dbName, String tableName) {
        exportData(client, dbName, tableName, 0, null);
    }

    /**
     * 导出数据
     *
     * @param client      客户端
     * @param dbName      数据库名称
     * @param tableName   表名称
     * @param exportMode  导出模式
     * @param exportTable 导出表
     */
    public static void exportData(ShellMysqlClient client, String dbName, String tableName, int exportMode, ShellMysqlDataExportTable exportTable) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMysqlDataExportController.class, StageManager.getFrontWindow());
            adapter.setProp("dbName", dbName);
            adapter.setProp("dbClient", client);
            adapter.setProp("tableName", tableName);
            adapter.setProp("exportMode", exportMode);
            adapter.setProp("exportTable", exportTable);
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
    public static void importData(ShellMysqlClient client, String dbName) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMysqlDataImportController.class, StageManager.getFrontWindow());
            adapter.setProp("dbName", dbName);
            adapter.setProp("dbClient", client);
            adapter.display();
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
    public static void dumpData(ShellMysqlClient client, String dbName, String tableName, int dumpType) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMysqlDataDumpController.class, StageManager.getFrontWindow());
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
    public static void runSqlFile(ShellMysqlClient client, String dbName) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMysqlRunSqlFileController.class, StageManager.getFrontWindow());
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
    public static void databaseUpdate(MysqlDatabase database, ShellMysqlRootTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMysqlDatabaseUpdateController.class, StageManager.getFrontWindow());
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
            StageAdapter adapter = StageManager.parseStage(ShellMysqlDataTransportController.class, StageManager.getFrontWindow());
            adapter.setProp("connect", connect);
            adapter.setProp("dbName", dbName);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加数据库
     *
     * @param connectItem 根节点
     * @return 窗口适配器
     */
    public static StageAdapter addDatabase(ShellMysqlRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMysqlDatabaseAddController.class, StageManager.getFrontWindow());
            adapter.setProp("connectItem", connectItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 表信息
     *
     * @param treeItem 表节点
     */
    public static void tableInfo(ShellMysqlTableTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMysqlTableInfoController.class, StageManager.getFrontWindow());
            fxView.setProp("item", treeItem);
            fxView.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 视图信息
     *
     * @param treeItem 视图节点
     */
    public static void viewInfo(ShellMysqlViewTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMysqlViewInfoController.class, StageManager.getFrontWindow());
            fxView.setProp("item", treeItem);
            fxView.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 函数信息
     *
     * @param treeItem 函数节点
     */
    public static void functionInfo(ShellMysqlFunctionTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMysqlFunctionInfoController.class, StageManager.getFrontWindow());
            fxView.setProp("item", treeItem);
            fxView.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 过程信息
     *
     * @param treeItem 过程节点
     */
    public static void procedureInfo(ShellMysqlProcedureTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMysqlProcedureInfoController.class, StageManager.getFrontWindow());
            fxView.setProp("item", treeItem);
            fxView.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 事件信息
     *
     * @param treeItem 事件节点
     */
    public static void eventInfo(ShellMysqlEventTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMysqlEventInfoController.class, StageManager.getFrontWindow());
            fxView.setProp("item", treeItem);
            fxView.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

}
