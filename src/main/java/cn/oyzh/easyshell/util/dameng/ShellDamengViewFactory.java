package cn.oyzh.easyshell.util.dameng;

import cn.oyzh.easyshell.controller.dameng.data.ShellDamengDataDumpController;
import cn.oyzh.easyshell.controller.dameng.data.ShellDamengDataExportController;
import cn.oyzh.easyshell.controller.dameng.data.ShellDamengDataImportController;
import cn.oyzh.easyshell.controller.dameng.data.ShellDamengDataRunSqlFileController;
import cn.oyzh.easyshell.controller.dameng.data.ShellDamengDataTransportController;
import cn.oyzh.easyshell.controller.dameng.function.ShellDamengFunctionInfoController;
import cn.oyzh.easyshell.controller.dameng.procedure.ShellDamengProcedureInfoController;
import cn.oyzh.easyshell.controller.dameng.table.ShellDamengTableInfoController;
import cn.oyzh.easyshell.controller.dameng.view.DamengViewInfoController;
import cn.oyzh.easyshell.controller.dameng.database.ShellDamengSchemaAddController;
import cn.oyzh.easyshell.controller.mysql.database.ShellMysqlDatabaseAddController;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.dto.ShellDamengDataExportTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.dameng.function.DamengFunctionTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.DamengProcedureTreeItem;
import cn.oyzh.easyshell.trees.dameng.root.DBRootTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.DamengTableTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.DamengViewTreeItem;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * msyql页面工厂
 *
 * @author oyzh
 * @since 2025-11-07
 */
public class ShellDamengViewFactory {

    /**
     * 导出数据
     *
     * @param client    客户端
     * @param schema    模式
     * @param tableName 表名称
     */
    public static void exportData(ShellDamengClient client, String schema, String tableName) {
        exportData(client, schema, tableName, 0, null);
    }

    /**
     * 导出数据
     *
     * @param client      客户端
     * @param schema      模式
     * @param tableName   表名称
     * @param exportMode  导出模式
     * @param exportTable 导出表
     */
    public static void exportData(ShellDamengClient client, String schema, String tableName, int exportMode, ShellDamengDataExportTable exportTable) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDamengDataExportController.class, StageManager.getFrontWindow());
            adapter.setProp("dbName", schema);
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
     * @param schema 模式
     */
    public static void importData(ShellDamengClient client, String schema) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDamengDataImportController.class, StageManager.getFrontWindow());
            adapter.setProp("dbName", schema);
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
     * @param schema    模式
     * @param tableName 表名称
     * @param dumpType  导出类型 1.库 2.表
     */
    public static void dumpData(ShellDamengClient client, String schema, String tableName, int dumpType) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDamengDataDumpController.class, StageManager.getFrontWindow());
            adapter.setProp("dumpType", dumpType);
            adapter.setProp("dbName", schema);
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
     * @param schema 模式
     */
    public static void runSqlFile(ShellDamengClient client, String schema) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDamengDataRunSqlFileController.class, StageManager.getFrontWindow());
            adapter.setProp("dbName", schema);
            adapter.setProp("dbClient", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    //    /**
    //     * 编辑数据库
    //     *
    //     * @param database 数据库
    //     * @param treeItem 树节点
    //     */
    //    public static void databaseUpdate(DamengDatabase database, ShellDamengRootTreeItem treeItem) {
    //        try {
    //            StageAdapter adapter = StageManager.parseStage(ShellDamengSchemaUpdateController.class, StageManager.getFrontWindow());
    //            adapter.setProp("database", database);
    //            adapter.setProp("connectItem", treeItem);
    //            adapter.display();
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            MessageBox.exception(ex);
    //        }
    //    }

    /**
     * 传输数据
     *
     * @param connect 连接
     * @param schema  模式
     */
    public static void transportData(ShellConnect connect, String schema) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDamengDataTransportController.class, StageManager.getFrontWindow());
            adapter.setProp("connect", connect);
            adapter.setProp("dbName", schema);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    //    /**
    //     * 添加数据库
    //     *
    //     * @param connectItem 根节点
    //     * @return 窗口适配器
    //     */
    //    public static StageAdapter addDatabase(ShellDamengRootTreeItem connectItem) {
    //        try {
    //            StageAdapter adapter = StageManager.parseStage(ShellDamengSchemaAddController.class, StageManager.getFrontWindow());
    //            adapter.setProp("connectItem", connectItem);
    //            adapter.showAndWait();
    //            return adapter;
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            MessageBox.exception(ex);
    //        }
    //        return null;
    //    }

    /**
     * 添加数据库
     *
     * @param connectItem 根节点
     * @return 窗口适配器
     */
    public static StageAdapter addSchema(DBRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDamengSchemaAddController.class, StageManager.getFrontWindow());
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
    public static void tableInfo(DamengTableTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellDamengTableInfoController.class, StageManager.getFrontWindow());
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
    public static void viewInfo(DamengViewTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(DamengViewInfoController.class, StageManager.getFrontWindow());
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
    public static void functionInfo(DamengFunctionTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellDamengFunctionInfoController.class, StageManager.getFrontWindow());
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
    public static void procedureInfo(DamengProcedureTreeItem treeItem) {
        try {
            StageAdapter fxView = StageManager.parseStage(ShellDamengProcedureInfoController.class, StageManager.getFrontWindow());
            fxView.setProp("item", treeItem);
            fxView.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

}
