package cn.oyzh.easyshell.util.mariadb;

import cn.oyzh.easyshell.controller.mariadb.data.ShellMariadbDataDumpController;
import cn.oyzh.easyshell.controller.mariadb.data.ShellMariadbDataExportController;
import cn.oyzh.easyshell.controller.mariadb.data.ShellMariadbDataImportController;
import cn.oyzh.easyshell.controller.mariadb.data.ShellMariadbDataRunSqlFileController;
import cn.oyzh.easyshell.controller.mariadb.data.ShellMariadbDataTransportController;
import cn.oyzh.easyshell.controller.mariadb.database.ShellMariadbDatabaseAddController;
import cn.oyzh.easyshell.controller.mariadb.database.ShellMariadbDatabaseUpdateController;
import cn.oyzh.easyshell.controller.mariadb.event.ShellMariadbEventInfoController;
import cn.oyzh.easyshell.controller.mariadb.function.ShellMariadbFunctionInfoController;
import cn.oyzh.easyshell.controller.mariadb.procedure.ShellMariadbProcedureInfoController;
import cn.oyzh.easyshell.controller.mariadb.table.ShellMariadbTableInfoController;
import cn.oyzh.easyshell.controller.mariadb.view.ShellMariadbViewInfoController;
import cn.oyzh.easyshell.data.mariadb.dto.ShellMariadbDataExportTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.trees.mariadb.event.ShellMariadbEventTreeItem;
import cn.oyzh.easyshell.trees.mariadb.function.ShellMariadbFunctionTreeItem;
import cn.oyzh.easyshell.trees.mariadb.procedure.ShellMariadbProcedureTreeItem;
import cn.oyzh.easyshell.trees.mariadb.root.ShellMariadbRootTreeItem;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTableTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * msyql页面工厂
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewFactory {

    /**
     * 导出数据
     *
     * @param client    客户端
     * @param dbName    数据库名称
     * @param tableName 表名称
     */
    public static void exportData(ShellMariadbClient client, String dbName, String tableName) {
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
    public static void exportData(ShellMariadbClient client, String dbName, String tableName, int exportMode, ShellMariadbDataExportTable exportTable) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMariadbDataExportController.class);
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
    public static void importData(ShellMariadbClient client, String dbName) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMariadbDataImportController.class);
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
     * @param dumpType 导出类型 1.库 2.表
     */
    public static void dumpData(ShellMariadbClient client, String dbName, String tableName, int dumpType) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMariadbDataDumpController.class);
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
    public static void runSqlFile(ShellMariadbClient client, String dbName) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMariadbDataRunSqlFileController.class);
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
    public static void databaseUpdate(MariadbDatabase database, ShellMariadbRootTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMariadbDatabaseUpdateController.class, StageManager.getFrontWindow());
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
            StageAdapter adapter = StageManager.parseStage(ShellMariadbDataTransportController.class);
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
    public static StageAdapter addDatabase(ShellMariadbRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellMariadbDatabaseAddController.class, StageManager.getFrontWindow());
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
    public static void tableInfo(ShellMariadbTableTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMariadbTableInfoController.class, StageManager.getFrontWindow());
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
    public static void viewInfo(ShellMariadbViewTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMariadbViewInfoController.class, StageManager.getFrontWindow());
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
    public static void functionInfo(ShellMariadbFunctionTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMariadbFunctionInfoController.class, StageManager.getFrontWindow());
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
    public static void procedureInfo(ShellMariadbProcedureTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMariadbProcedureInfoController.class, StageManager.getFrontWindow());
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
    public static void eventInfo(ShellMariadbEventTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellMariadbEventInfoController.class, StageManager.getFrontWindow());
            fxView.setProp("item", treeItem);
            fxView.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

}
