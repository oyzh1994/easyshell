package cn.oyzh.easyshell.util.sqlite;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordPrimaryKey;
import cn.oyzh.easyshell.sqlite.ShellSqliteConnConfig;
import cn.oyzh.easyshell.sqlite.ShellSqliteHelper;
import cn.oyzh.fx.db.query.DBQueryResults;

import java.util.Properties;

/**
 * SQLite 通用工具入口。
 *
 * @since 2026-11-02
 */
public final class ShellSqliteUtil {

    private ShellSqliteUtil() {
    }

    public static ShellSqliteConnConfig connConfig(ShellConnect connect) {
        return ShellSqliteHelper.connConfig(connect);
    }

    public static String defaultEnvironment() {
        return ShellSqliteHelper.defaultEnvironment();
    }

    public static boolean environmentBoolean(String env, String key, boolean defaultValue) {
        return ShellSqliteHelper.environmentBoolean(env, key, defaultValue);
    }

    public static String setEnvironmentValue(String env, String key, String value) {
        return ShellSqliteHelper.setEnvironmentValue(env, key, value);
    }

    public static Properties parseEnvironment(String env) {
        return ShellSqliteHelper.parseEnvironment(env);
    }

    public static boolean isTransactionControl(String sql) {
        return ShellSqliteHelper.isTransactionControl(sql);
    }

    public static String transactionCommand(String sql) {
        return ShellSqliteHelper.transactionCommand(sql);
    }

    public static String quote(String name) {
        return ShellSqliteHelper.quote(name);
    }

    public static String displayValue(Object value) {
        return ShellSqliteHelper.displayValue(value);
    }

    public static String formatResult(SQLiteExecuteResult result) {
        return ShellSqliteHelper.formatResult(result);
    }

    public static String formatResults(DBQueryResults<SQLiteExecuteResult> results) {
        return ShellSqliteHelper.formatResults(results);
    }

    /**
     * 初始化主键
     *
     * @param columns 字段列表
     * @param record  记录
     * @return 主键
     */
    public static SQLiteRecordPrimaryKey initPrimaryKey(SQLiteColumns columns, SQLiteRecord record) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        SQLiteColumn column = CollectionUtil.getFirst(columns.primaryKeys());
        if (column == null) {
            return null;
        }
        SQLiteRecordPrimaryKey primaryKey = new SQLiteRecordPrimaryKey();
        primaryKey.init(column, record);
        return primaryKey;
    }
}
