package cn.oyzh.easyshell.util.sqlserver;

import cn.oyzh.easyshell.controller.sqlserver.data.ShellSqlServerDataDumpController;
import cn.oyzh.easyshell.controller.sqlserver.data.ShellSqlServerDataExportController;
import cn.oyzh.easyshell.controller.sqlserver.data.ShellSqlServerDataImportController;
import cn.oyzh.easyshell.controller.sqlserver.data.ShellSqlServerDataRunSqlFileController;
import cn.oyzh.easyshell.controller.sqlserver.data.ShellSqlServerDataTransportController;
import cn.oyzh.easyshell.controller.sqlserver.function.ShellSqlServerFunctionInfoController;
import cn.oyzh.easyshell.controller.sqlserver.procedure.ShellSqlServerProcedureInfoController;
import cn.oyzh.easyshell.controller.sqlserver.schema.ShellSqlServerSchemaAddController;
import cn.oyzh.easyshell.controller.sqlserver.schema.ShellSqlServerSchemaUpdateController;
import cn.oyzh.easyshell.controller.sqlserver.sequence.ShellSqlServerSequenceInfoController;
import cn.oyzh.easyshell.controller.sqlserver.table.ShellSqlServerTableInfoController;
import cn.oyzh.easyshell.controller.sqlserver.view.ShellSqlServerViewInfoController;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.data.sqlserver.dto.ShellSqlServerDataExportTable;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.sqlserver.function.ShellSqlServerFunctionTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.procedure.ShellSqlServerProcedureTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.root.ShellSqlServerRootTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.sequence.ShellSqlServerSequenceTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.table.ShellSqlServerTableTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * SqlServer页面工厂
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerViewFactory {

    /**
     * 导出数据
     *
     * @param client    客户端
     * @param schema    模式
     * @param tableName 表名称
     */
    public static void exportData(ShellSqlServerClient client, String schema, String tableName) {
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
    public static void exportData(ShellSqlServerClient client, String schema, String tableName, int exportMode, ShellSqlServerDataExportTable exportTable) {
        try {
            Class<?> clazz = ShellSqlServerDataExportController.class;
            if (StageManager.frontStage(clazz, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("dbName", schema);
            adapter.setProp("dbClient", client);
            adapter.setProp("tableName", tableName);
            adapter.setProp("exportMode", exportMode);
            adapter.setProp("exportTable", exportTable);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 导入数据
     *
     * @param client 客户端
     * @param schema 模式
     */
    public static void importData(ShellSqlServerClient client, String schema) {
        try {
            Class<?> clazz = ShellSqlServerDataImportController.class;
            if (StageManager.frontStage(clazz, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("dbName", schema);
            adapter.setProp("dbClient", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
    public static void dumpData(ShellSqlServerClient client, String schema, String tableName, int dumpType) {
        try {
            Class<?> clazz = ShellSqlServerDataDumpController.class;
            if (StageManager.frontStage(clazz, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("dumpType", dumpType);
            adapter.setProp("dbName", schema);
            adapter.setProp("dbClient", client);
            adapter.setProp("tableName", tableName);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 运行sql文件
     *
     * @param client 客户端
     * @param schema 模式
     */
    public static void runSqlFile(ShellSqlServerClient client, String schema) {
        try {
            Class<?> clazz = ShellSqlServerDataRunSqlFileController.class;
            if (StageManager.frontStage(clazz, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("dbName", schema);
            adapter.setProp("dbClient", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 传输数据
     *
     * @param connect 连接
     * @param schema  模式
     */
    public static void transportData(ShellConnect connect, String schema) {
        try {
            Class<?> clazz = ShellSqlServerDataTransportController.class;
            if (StageManager.frontStage(clazz, "connect", connect)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("connect", connect);
            adapter.setProp("dbName", schema);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 添加数据库
     *
     * @param connectItem 根节点
     * @return 窗口适配器
     */
    public static StageAdapter addSchema(ShellSqlServerRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqlServerSchemaAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("connectItem", connectItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 修改模式
     *
     * @param connectItem 根节点
     */
    public static void updateSchema(SqlServerSchema schema, ShellSqlServerRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqlServerSchemaUpdateController.class, StageManager.getPrimaryStage());
            adapter.setProp("database", schema);
            adapter.setProp("connectItem", connectItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 表信息
     *
     * @param treeItem 表节点
     */
    public static void tableInfo(ShellSqlServerTableTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqlServerTableInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 视图信息
     *
     * @param treeItem 视图节点
     */
    public static void viewInfo(ShellSqlServerViewTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqlServerViewInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 函数信息
     *
     * @param treeItem 函数节点
     */
    public static void functionInfo(ShellSqlServerFunctionTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqlServerFunctionInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 过程信息
     *
     * @param treeItem 过程节点
     */
    public static void procedureInfo(ShellSqlServerProcedureTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqlServerProcedureInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void sequenceInfo(ShellSqlServerSequenceTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqlServerSequenceInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }
}
