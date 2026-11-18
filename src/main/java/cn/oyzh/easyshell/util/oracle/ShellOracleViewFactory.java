package cn.oyzh.easyshell.util.oracle;

import cn.oyzh.easyshell.controller.oracle.data.ShellOracleDataDumpController;
import cn.oyzh.easyshell.controller.oracle.data.ShellOracleDataExportController;
import cn.oyzh.easyshell.controller.oracle.data.ShellOracleDataImportController;
import cn.oyzh.easyshell.controller.oracle.data.ShellOracleDataRunSqlFileController;
import cn.oyzh.easyshell.controller.oracle.data.ShellOracleDataTransportController;
import cn.oyzh.easyshell.controller.oracle.function.ShellOracleFunctionInfoController;
import cn.oyzh.easyshell.controller.oracle.procedure.ShellOracleProcedureInfoController;
import cn.oyzh.easyshell.controller.oracle.schema.ShellOracleSchemaAddController;
import cn.oyzh.easyshell.controller.oracle.schema.ShellOracleSchemaUpdateController;
import cn.oyzh.easyshell.controller.oracle.sequence.ShellOracleSequenceInfoController;
import cn.oyzh.easyshell.controller.oracle.table.ShellOracleTableInfoController;
import cn.oyzh.easyshell.controller.oracle.view.ShellOracleViewInfoController;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.data.oracle.dto.ShellOracleDataExportTable;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.oracle.function.ShellOracleFunctionTreeItem;
import cn.oyzh.easyshell.trees.oracle.procedure.ShellOracleProcedureTreeItem;
import cn.oyzh.easyshell.trees.oracle.root.ShellOracleRootTreeItem;
import cn.oyzh.easyshell.trees.oracle.sequence.ShellOracleSequenceTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTableTreeItem;
import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * msyql页面工厂
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleViewFactory {

    /**
     * 导出数据
     *
     * @param client    客户端
     * @param schema    模式
     * @param tableName 表名称
     */
    public static void exportData(ShellOracleClient client, String schema, String tableName) {
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
    public static void exportData(ShellOracleClient client, String schema, String tableName, int exportMode, ShellOracleDataExportTable exportTable) {
        try {
            Class<?> clazz = ShellOracleDataExportController.class;
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
    public static void importData(ShellOracleClient client, String schema) {
        try {
            Class<?> clazz = ShellOracleDataImportController.class;
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
    public static void dumpData(ShellOracleClient client, String schema, String tableName, int dumpType) {
        try {
            Class<?> clazz = ShellOracleDataDumpController.class;
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
    public static void runSqlFile(ShellOracleClient client, String schema) {
        try {
            Class<?> clazz = ShellOracleDataRunSqlFileController.class;
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
            Class<?> clazz = ShellOracleDataTransportController.class;
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
    public static StageAdapter addSchema(ShellOracleRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellOracleSchemaAddController.class, StageManager.getPrimaryStage());
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
    public static void updateSchema(OracleSchema schema, ShellOracleRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellOracleSchemaUpdateController.class, StageManager.getPrimaryStage());
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
    public static void tableInfo(ShellOracleTableTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellOracleTableInfoController.class, StageManager.getPrimaryStage());
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
    public static void viewInfo(ShellOracleViewTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellOracleViewInfoController.class, StageManager.getPrimaryStage());
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
    public static void functionInfo(ShellOracleFunctionTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellOracleFunctionInfoController.class, StageManager.getPrimaryStage());
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
    public static void procedureInfo(ShellOracleProcedureTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellOracleProcedureInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void sequenceInfo(ShellOracleSequenceTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellOracleSequenceInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }
}
