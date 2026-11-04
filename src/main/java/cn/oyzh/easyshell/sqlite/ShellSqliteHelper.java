package cn.oyzh.easyshell.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * SQLite 业务辅助工具。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class ShellSqliteHelper {

    private ShellSqliteHelper() {
    }

    /**
     * 创建文件连接配置。
     *
     * @param connect shell连接
     * @return 文件连接配置
     */
    public static ShellSqliteConnConfig connConfig(ShellConnect connect) {
        ShellSqliteConnConfig config = new ShellSqliteConnConfig();
        Properties environment = parseEnvironment(connect.getEnvironment());
        config.setHost(connect.getHost());
        config.setReadOnly(connect.isReadonly());
        config.setCreateIfMissing(parseBoolean(environment.getProperty("createIfMissing", "false")));
        config.setBusyTimeout(connect.connectTimeOutMs());
        config.setJournalMode(environment.getProperty("journal_mode", "DELETE"));
        config.setForeignKeys(parseBoolean(environment.getProperty("foreign_keys", "true")));
        config.setSynchronous(environment.getProperty("synchronous", "NORMAL"));
        config.setEnv(connect.getEnvironment());
        return config;
    }

    /**
     * 获取默认连接环境。
     *
     * @return 环境内容
     */
    public static String defaultEnvironment() {
        return """
                journal_mode=DELETE
                foreign_keys=1
                synchronous=NORMAL
                createIfMissing=true
                """;
    }

    /**
     * 获取环境布尔值。
     *
     * @param env          环境内容
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static boolean environmentBoolean(String env, String key, boolean defaultValue) {
        String value = parseEnvironment(env).getProperty(key);
        return value == null ? defaultValue : parseBoolean(value);
    }

    /**
     * 设置环境参数，保留原有注释与顺序。
     *
     * @param env   环境内容
     * @param key   键
     * @param value 值
     * @return 新环境内容
     */
    public static String setEnvironmentValue(String env, String key, String value) {
        if (StringUtil.isBlank(key) || value == null) {
            return env == null ? "" : env;
        }
        String[] lines = (env == null ? "" : env).split("\\R", -1);
        boolean replaced = false;
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            int index = trimmed.indexOf('=');
            if (!replaced && index > 0 && key.equals(trimmed.substring(0, index).trim())) {
                builder.append(key).append('=').append(value);
                replaced = true;
            } else {
                builder.append(line);
            }
            builder.append(System.lineSeparator());
        }
        if (!replaced) {
            builder.append(key).append('=').append(value).append(System.lineSeparator());
        }
        return builder.toString();
    }

    /**
     * 解析环境参数。
     *
     * @param env 环境内容
     * @return JDBC属性
     */
    public static Properties parseEnvironment(String env) {
        Properties properties = new Properties();
        if (StringUtil.isBlank(env)) {
            return properties;
        }
        for (String line : env.lines().toList()) {
            String value = line.trim();
            if (value.isEmpty() || value.startsWith("#")) {
                continue;
            }
            int index = value.indexOf('=');
            if (index <= 0) {
                throw new IllegalArgumentException("Invalid SQLite environment line: " + line);
            }
            String key = value.substring(0, index).trim();
            if (key.isEmpty()) {
                throw new IllegalArgumentException("Invalid SQLite environment line: " + line);
            }
            properties.setProperty(key, value.substring(index + 1).trim());
        }
        return properties;
    }

    /**
     * 解析布尔配置，兼容SQLite的0/1写法。
     *
     * @param value 值
     * @return 结果
     */
    private static boolean parseBoolean(String value) {
        return "1".equals(value) || Boolean.parseBoolean(value)
                || "yes".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }

    /**
     * 是否显式事务控制语句。
     *
     * @param sql SQL
     * @return 结果
     */
    public static boolean isTransactionControl(String sql) {
        return transactionCommand(sql) != null;
    }

    /**
     * 是否为外键开关PRAGMA。
     *
     * @param sql SQL
     * @return 结果
     */
    public static boolean isForeignKeysPragma(String sql) {
        if (StringUtil.isBlank(sql)) {
            return false;
        }
        String content = stripLeadingComments(sql).trim();
        if (!isCommand(content, "PRAGMA")) {
            return false;
        }
        String argument = content.substring("PRAGMA".length()).trim();
        String name = "foreign_keys";
        return argument.length() >= name.length()
                && argument.regionMatches(true, 0, name, 0, name.length())
                && (argument.length() == name.length()
                || !isIdentifierPart(argument.charAt(name.length())));
    }

    private static boolean isIdentifierPart(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }

    /**
     * 获取显式事务控制命令。
     *
     * @param sql SQL
     * @return 命令，非事务语句返回null
     */
    public static String transactionCommand(String sql) {
        if (sql == null) {
            return null;
        }
        String content = stripLeadingComments(sql).trim().toUpperCase(Locale.ROOT);
        if (isCommand(content, "ROLLBACK") && rollbackToSavepoint(content)) {
            return "ROLLBACK_TO";
        }
        for (String command : new String[]{"BEGIN", "COMMIT", "END", "ROLLBACK", "SAVEPOINT", "RELEASE"}) {
            if (isCommand(content, command)) {
                return command;
            }
        }
        return null;
    }

    private static boolean isCommand(String content, String command) {
        if (!content.startsWith(command)) {
            return false;
        }
        if (content.length() == command.length()) {
            return true;
        }
        char next = content.charAt(command.length());
        return Character.isWhitespace(next) || next == ';';
    }

    private static boolean rollbackToSavepoint(String content) {
        String value = content.substring("ROLLBACK".length()).trim();
        if (value.startsWith("TRANSACTION")) {
            value = value.substring("TRANSACTION".length()).trim();
        }
        return value.startsWith("TO");
    }

    private static String stripLeadingComments(String sql) {
        int index = 0;
        while (index < sql.length()) {
            while (index < sql.length() && Character.isWhitespace(sql.charAt(index))) {
                index++;
            }
            if (index + 1 < sql.length() && sql.charAt(index) == '-' && sql.charAt(index + 1) == '-') {
                int lineEnd = sql.indexOf('\n', index + 2);
                index = lineEnd < 0 ? sql.length() : lineEnd + 1;
                continue;
            }
            if (index + 1 < sql.length() && sql.charAt(index) == '/' && sql.charAt(index + 1) == '*') {
                int blockEnd = sql.indexOf("*/", index + 2);
                if (blockEnd < 0) {
                    return sql.substring(index);
                }
                index = blockEnd + 2;
                continue;
            }
            break;
        }
        return sql.substring(index);
    }

    /**
     * 包装SQLite标识符。
     *
     * @param name 名称
     * @return 标识符
     */
    public static String quote(String name) {
        return DBUtil.wrap(name, cn.oyzh.fx.db.DBDialect.SQLITE);
    }

    /**
     * 格式化显示值。
     *
     * @param value 值
     * @return 文本
     */
    public static String displayValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof byte[] bytes) {
            StringBuilder builder = new StringBuilder("X'");
            for (byte item : bytes) {
                builder.append(String.format("%02X", item));
            }
            return builder.append("'").toString();
        }
        return String.valueOf(value);
    }

    /**
     * 格式化终端结果。
     *
     * @param result 查询结果
     * @return 文本表格
     */
    public static String formatResult(SQLiteExecuteResult result) {
        if (result == null) {
            return "";
        }
        if (result.getColumns().isEmpty()) {
            return "OK";
        }
        List<String> headers = result.getColumns().stream().map(column -> displayValue(column.getName())).toList();
        List<List<String>> rows = new ArrayList<>();
        for (SQLiteRecord record : result.getRecords()) {
            List<String> row = new ArrayList<>();
            for (SQLiteColumn column : result.getColumns()) {
                row.add(displayValue(record.getValue(column)));
            }
            rows.add(row);
        }
        int columnCount = headers.size();
        int[] widths = new int[columnCount];
        for (int index = 0; index < columnCount; index++) {
            widths[index] = headers.get(index).length();
            for (List<String> row : rows) {
                widths[index] = Math.max(widths[index], row.get(index).length());
            }
        }
        StringBuilder builder = new StringBuilder();
        appendRow(builder, headers, widths);
        for (int index = 0; index < columnCount; index++) {
            builder.append("-".repeat(widths[index])).append("  ");
        }
        builder.append(System.lineSeparator());
        for (List<String> row : rows) {
            appendRow(builder, row, widths);
        }
        builder.append("#").append(result.getCount()).append(" row(s) in set");
        if (result.getUsedMs() > 0) {
            builder.append(" (").append(result.getUsedMs()).append(" ms)");
        }
        return builder.toString();
    }

    /**
     * 格式化多语句结果。
     *
     * @param results 结果集合
     * @return 文本
     */
    public static String formatResults(DBQueryResults<SQLiteExecuteResult> results) {
        if (results == null) {
            return "";
        }
        if (!results.isSuccess()) {
            return "ERROR: " + results.getErrMsg();
        }
        StringBuilder builder = new StringBuilder();
        List<SQLiteExecuteResult> resultItems = results.getResults() == null ? List.of() : results.getResults();
        for (SQLiteExecuteResult result : resultItems) {
            if (builder.length() > 0) {
                builder.append(System.lineSeparator());
            }
            if (!result.isSuccess()) {
                builder.append("ERROR: ").append(result.getMsg());
            } else if (result.getUpdateCount() > 0) {
                builder.append("Query OK, ").append(result.getUpdateCount()).append(" rows affected");
                if (result.getUsedMs() > 0) {
                    builder.append(" (").append(result.getUsedMs()).append(" ms)");
                }
            } else {
                builder.append(formatResult(result));
            }
        }
        return builder.toString();
    }

    private static void appendRow(StringBuilder builder, List<String> values, int[] widths) {
        for (int index = 0; index < values.size(); index++) {
            builder.append(String.format("%-" + (widths[index] + 2) + "s", values.get(index)));
        }
        builder.append(System.lineSeparator());
    }
}
