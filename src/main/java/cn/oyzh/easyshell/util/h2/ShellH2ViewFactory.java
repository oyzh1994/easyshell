package cn.oyzh.easyshell.util.h2;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.h2.data.ShellH2DataDumpController;
import cn.oyzh.easyshell.controller.h2.data.ShellH2DataExportController;
import cn.oyzh.easyshell.controller.h2.data.ShellH2DataImportController;
import cn.oyzh.easyshell.controller.h2.data.ShellH2DataRunSqlFileController;
import cn.oyzh.easyshell.controller.h2.data.ShellH2DataTransportController;
import cn.oyzh.easyshell.controller.h2.schema.ShellH2SchemaAddController;
import cn.oyzh.easyshell.controller.h2.schema.ShellH2SchemaUpdateController;
import cn.oyzh.easyshell.controller.h2.table.ShellH2TableInfoController;
import cn.oyzh.easyshell.controller.h2.view.ShellH2ViewInfoController;
import cn.oyzh.easyshell.data.h2.dto.H2DataExportTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TableTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * H2 页面工厂。
 *
 * @since 2026-11-28
 */
public final class ShellH2ViewFactory {

    private ShellH2ViewFactory() {
    }

    public static void exportData(ShellH2Client client, String tableName) {
        exportData(client, client.schema(), tableName, 0, null);
    }

    public static void exportData(ShellH2Client client, String dbName, String tableName) {
        exportData(client, dbName, tableName, 0, null);
    }

    public static void exportData(ShellH2Client client, String tableName, int exportMode,
                                  H2DataExportTable exportTable) {
        exportData(client, client.schema(), tableName, exportMode, exportTable);
    }

    public static void exportData(ShellH2Client client, String dbName, String tableName,
                                  int exportMode, H2DataExportTable exportTable) {
        try {
            if (StageManager.frontStage(ShellH2DataExportController.class, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellH2DataExportController.class);
            adapter.setProp("dbName", client.schema(dbName));
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

    public static void importData(ShellH2Client client) {
        try {
            if (StageManager.frontStage(ShellH2DataImportController.class, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellH2DataImportController.class);
            adapter.setProp("dbClient", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void dumpData(ShellH2Client client, String tableName) {
        dumpData(client, client.schema(), tableName,
                StringUtil.isBlank(tableName) ? 0 : 2, 0);
    }

    public static void dumpData(ShellH2Client client, String schema, String tableName, int dumpType) {
        dumpData(client, schema, tableName, dumpType, 0);
    }

    public static void dumpData(ShellH2Client client, String tableName, int dumpType, int dataType) {
        dumpData(client, client.schema(), tableName, dumpType, dataType);
    }

    public static void dumpData(ShellH2Client client, String schema, String tableName,
                                int dumpType, int dataType) {
        try {
            if (StageManager.frontStage(ShellH2DataDumpController.class, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellH2DataDumpController.class);
            adapter.setProp("dbClient", client);
            adapter.setProp("dbName", schema);
            adapter.setProp("tableName", tableName);
            adapter.setProp("dumpType", dumpType);
            adapter.setProp("dataType", dataType);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void runSqlFile(ShellH2Client client) {
        runSqlFile(client, client.schema());
    }

    public static void runSqlFile(ShellH2Client client, String schema) {
        try {
            if (StageManager.frontStage(ShellH2DataRunSqlFileController.class, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellH2DataRunSqlFileController.class);
            adapter.setProp("dbClient", client);
            adapter.setProp("dbName", schema);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void transportData(ShellConnect connect) {
        transportData(connect, null);
    }

    public static void transportData(ShellConnect connect, String schema) {
        try {
            Class<?> clazz = ShellH2DataTransportController.class;
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
     * 打开 H2 新增模式页面。
     *
     * @param connectItem 根节点
     * @return 页面适配器
     */
    public static StageAdapter addSchema(ShellH2RootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(
                    ShellH2SchemaAddController.class, StageManager.getPrimaryStage());
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
     * 打开 H2 编辑模式页面。
     *
     * @param schema      模式
     * @param connectItem 根节点
     */
    public static void updateSchema(H2Schema schema, ShellH2RootTreeItem connectItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(
                    ShellH2SchemaUpdateController.class, StageManager.getPrimaryStage());
            adapter.setProp("database", schema);
            adapter.setProp("connectItem", connectItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void tableInfo(ShellH2TableTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellH2TableInfoController.class);
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void viewInfo(ShellH2ViewTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellH2ViewInfoController.class);
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

}
