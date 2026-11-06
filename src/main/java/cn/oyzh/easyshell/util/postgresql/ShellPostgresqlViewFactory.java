package cn.oyzh.easyshell.util.postgresql;

import cn.oyzh.easyshell.controller.postgresql.data.ShellPostgresqlDataDumpController;
import cn.oyzh.easyshell.controller.postgresql.data.ShellPostgresqlDataExportController;
import cn.oyzh.easyshell.controller.postgresql.data.ShellPostgresqlDataImportController;
import cn.oyzh.easyshell.controller.postgresql.data.ShellPostgresqlDataRunSqlFileController;
import cn.oyzh.easyshell.controller.postgresql.data.ShellPostgresqlDataTransportController;
import cn.oyzh.easyshell.controller.postgresql.function.ShellPostgresqlFunctionInfoController;
import cn.oyzh.easyshell.controller.postgresql.procedure.ShellPostgresqlProcedureInfoController;
import cn.oyzh.easyshell.controller.postgresql.schema.ShellPostgresqlSchemaAddController;
import cn.oyzh.easyshell.controller.postgresql.schema.ShellPostgresqlSchemaUpdateController;
import cn.oyzh.easyshell.controller.postgresql.sequence.ShellPostgresqlSequenceInfoController;
import cn.oyzh.easyshell.controller.postgresql.table.ShellPostgresqlTableInfoController;
import cn.oyzh.easyshell.controller.postgresql.view.ShellPostgresqlViewInfoController;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.data.postgresql.dto.ShellPostgresqlDataExportTable;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.postgresql.function.ShellPostgresqlFunctionTreeItem;
import cn.oyzh.easyshell.trees.postgresql.procedure.ShellPostgresqlProcedureTreeItem;
import cn.oyzh.easyshell.trees.postgresql.root.ShellPostgresqlRootTreeItem;
import cn.oyzh.easyshell.trees.postgresql.sequence.ShellPostgresqlSequenceTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTableTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * PostgreSQL页面工厂
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlViewFactory {

    /**
     * 导出数据
     *
     * @param client    客户端
     * @param schema    模式
     * @param tableName 表名称
     */
    public static void exportData(ShellPostgresqlClient client, String schema, String tableName) {
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
    public static void exportData(ShellPostgresqlClient client, String schema, String tableName, int exportMode, ShellPostgresqlDataExportTable exportTable) {
        try {
            Class<?> clazz = ShellPostgresqlDataExportController.class;
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
    public static void importData(ShellPostgresqlClient client, String schema) {
        try {
            Class<?> clazz = ShellPostgresqlDataImportController.class;
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
    public static void dumpData(ShellPostgresqlClient client, String schema, String tableName, int dumpType) {
        try {
            Class<?> clazz = ShellPostgresqlDataDumpController.class;
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
    public static void runSqlFile(ShellPostgresqlClient client, String schema) {
        try {
            Class<?> clazz = ShellPostgresqlDataRunSqlFileController.class;
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
            Class<?> clazz = ShellPostgresqlDataTransportController.class;
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
    public static StageAdapter addSchema(ShellPostgresqlRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellPostgresqlSchemaAddController.class, StageManager.getPrimaryStage());
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
    public static void updateSchema(PostgresqlSchema schema, ShellPostgresqlRootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellPostgresqlSchemaUpdateController.class, StageManager.getPrimaryStage());
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
    public static void tableInfo(ShellPostgresqlTableTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellPostgresqlTableInfoController.class, StageManager.getPrimaryStage());
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
    public static void viewInfo(ShellPostgresqlViewTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellPostgresqlViewInfoController.class, StageManager.getPrimaryStage());
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
    public static void functionInfo(ShellPostgresqlFunctionTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellPostgresqlFunctionInfoController.class, StageManager.getPrimaryStage());
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
    public static void procedureInfo(ShellPostgresqlProcedureTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellPostgresqlProcedureInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void sequenceInfo(ShellPostgresqlSequenceTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellPostgresqlSequenceInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }
}
