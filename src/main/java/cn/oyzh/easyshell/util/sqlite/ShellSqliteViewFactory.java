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
        try {
            if (StageManager.frontStage(ShellSqliteDataImportController.class, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellSqliteDataImportController.class);
            adapter.setProp("dbClient", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
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
        try {
            if (StageManager.frontStage(ShellSqliteDataRunSqlFileController.class, "dbClient", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellSqliteDataRunSqlFileController.class);
            adapter.setProp("dbClient", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void transportData(ShellConnect connect) {
        try {
            Class<?> clazz = ShellSqliteDataTransportController.class;
            if (StageManager.frontStage(clazz, "connect", connect)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("connect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void tableInfo(ShellSqliteTableTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqliteTableInfoController.class);
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    public static void viewInfo(ShellSqliteViewTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSqliteViewInfoController.class);
            adapter.setProp("item", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
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

}
