package cn.oyzh.easyshell.util.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.sqlite.data.ShellSqliteDataDumpController;
import cn.oyzh.easyshell.controller.sqlite.data.ShellSqliteDataExportController;
import cn.oyzh.easyshell.controller.sqlite.data.ShellSqliteDataImportController;
import cn.oyzh.easyshell.controller.sqlite.data.ShellSqliteDataRunSqlFileController;
import cn.oyzh.easyshell.controller.sqlite.data.ShellSqliteDataTransportController;
import cn.oyzh.easyshell.controller.sqlite.function.ShellSqliteFunctionInfoController;
import cn.oyzh.easyshell.controller.sqlite.table.ShellSqliteTableInfoController;
import cn.oyzh.easyshell.controller.sqlite.view.ShellSqliteViewInfoController;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * SQLite 页面工厂。
 *
 * @since 2026-11-02
 */
public final class ShellSqliteViewFactory {

    private ShellSqliteViewFactory() {
    }

    public static void exportData(ShellSqliteClient client, String tableName) {
        exportData(client, tableName, 0, null);
    }

    public static void exportData(ShellSqliteClient client, String tableName, int exportMode,
                                  SQLiteDataExportTable exportTable) {
        try {
            if (StageManager.frontStage(ShellSqliteDataExportController.class, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellSqliteDataExportController.class);
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

    public static void importData(ShellSqliteClient client) {
        open(ShellSqliteDataImportController.class, client, null);
    }

    public static void dumpData(ShellSqliteClient client, String tableName) {
        dumpData(client, tableName, StringUtil.isBlank(tableName) ? 0 : 2, 0);
    }

    public static void dumpData(ShellSqliteClient client, String tableName, int dumpType, int dataType) {
        try {
            if (StageManager.frontStage(ShellSqliteDataDumpController.class, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellSqliteDataDumpController.class);
            adapter.setProp("dbClient", client);
            adapter.setProp("tableName", tableName);
            adapter.setProp("dumpType", dumpType);
            adapter.setProp("dataType", dataType);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void runSqlFile(ShellSqliteClient client) {
        open(ShellSqliteDataRunSqlFileController.class, client, null);
    }

    public static void transportData(ShellSqliteClient client) {
        open(ShellSqliteDataTransportController.class, client, null);
    }

    public static void transportData(ShellConnect connect) {
        ShellSqliteClient sourceClient = null;
        try {
            if (StageManager.frontStage(ShellSqliteDataTransportController.class, "connect", connect)) {
                return;
            }
            sourceClient = (ShellSqliteClient) ShellClientUtil.newClient(connect);
            sourceClient.start();
            StageAdapter adapter = StageManager.parseStage(ShellSqliteDataTransportController.class);
            adapter.setProp("dbClient", sourceClient);
            adapter.setProp("ownsSourceClient", true);
            adapter.setProp("connect", connect);
            adapter.display();
        } catch (Throwable ex) {
            if (sourceClient != null) {
                sourceClient.close();
            }
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void tableInfo(ShellSqliteTableTreeItem treeItem) {
        openInfo(ShellSqliteTableInfoController.class, treeItem);
    }

    public static void viewInfo(ShellSqliteViewTreeItem treeItem) {
        openInfo(ShellSqliteViewInfoController.class, treeItem);
    }

    public static void functionInfo(ShellSqliteFunctionTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqliteFunctionInfoController.class);
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    private static void openInfo(Class<?> controller, Object treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(controller);
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    private static void open(Class<?> controller, ShellSqliteClient client, String tableName) {
        try {
            if (StageManager.frontStage(controller, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(controller);
            adapter.setProp("dbClient", client);
            adapter.setProp("tableName", tableName);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }
}
