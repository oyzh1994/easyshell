package cn.oyzh.easyshell.util.sqlite;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
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
}
