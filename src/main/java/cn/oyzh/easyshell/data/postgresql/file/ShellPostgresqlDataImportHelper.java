package cn.oyzh.easyshell.data.postgresql.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.util.DBUtil;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Postgresql数据导入助手
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlDataImportHelper {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导入配置
     * @return 参数化后的值
     * @throws ParseException 解析异常
     */
    public static Object parameterized(PostgresqlColumn column, Object value, DBDataImportConfig config) throws ParseException {
        if (value == null) {
            return null;
        }
        if (value.toString().isEmpty()) {
            return null;
        }
        if (column.isDateType()) {
            if (value instanceof CharSequence date) {
                Date date1 = DateUtil.parse(date, config.getDateFormat());
                return DateUtil.format(date1, "yyyy-MM-dd HH:mm:ss");
            }
            if (value instanceof LocalDate date) {
                return date.toString();
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            }
        }
        if (column.supportTimestamp()) {
            if (value instanceof CharSequence date) {
                LocalDateTime date1 = DateUtil.parseLocalDateTime(date, config.getDateFormat());
                return DateUtil.format(date1, "yyyy-MM-dd HH:mm:ss");
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            }
        }
        if (column.supportString()) {
            return value.toString();
        }
        return value;
    }

    /**
     * 转换为插入sql
     *
     * @param columns 字段列表
     * @param records 记录
     * @param config  配置
     * @return 插入sql
     * @throws Exception 异常
     */
    public static List<String> toInsertSql(PostgresqlColumns columns, List<PostgresqlRecord> records, DBDataImportConfig config) throws Exception {
        List<String> insertSql = new ArrayList<>();
        // PostgreSQL使用"schema"."table"定位表
        String table = StringUtil.isBlank(columns.schema())
                ? DBUtil.wrap(columns.tableName(), DBDialect.POSTGRESQL)
                : DBUtil.wrap(columns.schema(), columns.tableName(), DBDialect.POSTGRESQL);
        for (PostgresqlRecord record : records) {
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(table);
            sql.append(" (");
            for (PostgresqlColumn column : columns) {
                sql.append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL)).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(") VALUES (");
            for (PostgresqlColumn column : columns) {
                Object val = record.getValue(column.getName());
                val = DBUtil.unwrapData(val, DBDialect.POSTGRESQL);
                val = parameterized(column, val, config);
                sql.append(toSqlLiteral(column, val)).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(")");
            insertSql.add(sql.toString());
        }
        return insertSql;
    }

    /**
     * 转换为PostgreSQL Sql字面量
     *
     * @param column 字段
     * @param value  值
     * @return Sql字面量
     */
    public static String toSqlLiteral(PostgresqlColumn column, Object value) {
        if (value == null) {
            return "NULL";
        }
        if (column.isDateType() || column.supportTimestamp() || column.isTimeType()) {
            if (value instanceof LocalDateTime date) {
                return "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
            }
            if (value instanceof LocalDate date) {
                return "'" + date + "'";
            }
            if (value instanceof java.time.LocalTime time) {
                return "'" + time + "'";
            }
            if (value instanceof java.time.OffsetDateTime date) {
                return "'" + date + "'";
            }
            if (value instanceof Date date) {
                return "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
            }
        }
        if (column.supportBinary()) {
            // PostgreSQL bytea十六进制格式
            if (value instanceof byte[] bytes) {
                if (bytes.length == 0) {
                    return "NULL";
                }
                return "'\\x" + HexUtil.encodeHexStr(bytes, false) + "'::bytea";
            }
            String str = value.toString();
            if (StringUtil.startWithIgnoreCase(str, "\\x")) {
                return "'" + str + "'::bytea";
            }
            if (StringUtil.startWithIgnoreCase(str, "0x")) {
                return "'\\x" + str.substring(2) + "'::bytea";
            }
            return DBUtil.wrapData(str, DBDialect.POSTGRESQL).toString();
        }
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "TRUE" : "FALSE";
            }
            return StringUtil.equalsAnyIgnoreCase(value.toString(), "true", "t", "1", "yes", "y") ? "TRUE" : "FALSE";
        }
        if (column.supportBit()) {
            if (value instanceof byte[] bytes) {
                return "B'" + TextUtil.byteToBitStr(bytes) + "'";
            }
            if (value instanceof Boolean b) {
                return b ? "B'1'" : "B'0'";
            }
            return "B'" + value.toString().replaceAll("[^01]", "") + "'";
        }
        if (column.supportInteger() || column.supportDigits() || value instanceof Number) {
            return value.toString();
        }
        // 字符串、json、uuid等统一使用单引号字面量
        return DBUtil.wrapData(value.toString(), DBDialect.POSTGRESQL).toString();
    }

    /**
     * 转换为插入sql（数据库记录直接转储）
     *
     * @param columns 字段列表
     * @param records 记录
     * @param schema  模式名称，为空则不限定模式
     * @return 插入sql
     */
    public static List<String> toInsertSql(PostgresqlColumns columns, List<PostgresqlRecord> records, String schema) {
        List<String> insertSql = new ArrayList<>();
        String table = StringUtil.isBlank(schema)
                ? DBUtil.wrap(columns.tableName(), DBDialect.POSTGRESQL)
                : DBUtil.wrap(schema, columns.tableName(), DBDialect.POSTGRESQL);
        List<PostgresqlColumn> columnList = columns.sortOfPosition();
        for (PostgresqlRecord record : records) {
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(table);
            sql.append(" (");
            for (PostgresqlColumn column : columnList) {
                sql.append(DBUtil.wrap(column.getName(), DBDialect.POSTGRESQL)).append(", ");
            }
            sql.delete(sql.length() - 2, sql.length());
            sql.append(") VALUES (");
            for (PostgresqlColumn column : columnList) {
                sql.append(toSqlLiteral(column, record.getValue(column.getName()))).append(", ");
            }
            sql.delete(sql.length() - 2, sql.length());
            sql.append(");");
            insertSql.add(sql.toString());
        }
        return insertSql;
    }

    /**
     * 拆分sql语句
     *
     * <p>按PostgreSQL顶层分号拆分，忽略字符串、双引号标识符、注释以及$$（或$tag$）引用中的分号。</p>
     *
     * @param sql sql内容
     * @return 语句列表
     */
    public static List<String> splitSql(String sql) {
        List<String> list = new ArrayList<>();
        if (StringUtil.isBlank(sql)) {
            return list;
        }
        int start = 0;
        int end;
        while ((end = indexOfStatementEnd(sql, start)) >= 0) {
            String statement = sql.substring(start, end).strip();
            if (!statement.isEmpty()) {
                list.add(statement);
            }
            start = end + 1;
        }
        String rest = sql.substring(start).strip();
        if (!rest.isEmpty()) {
            list.add(rest);
        }
        return list;
    }

    /**
     * 查找语句结束符位置
     *
     * @param sql   sql内容
     * @param start 起始位置
     * @return 结束符位置，未结束返回-1
     */
    private static int indexOfStatementEnd(String sql, int start) {
        int state = 0; // 0普通 1单引号 2双引号 3$$引用 4行注释 5块注释
        String dollarTag = null;
        for (int i = start; i < sql.length(); ) {
            char c = sql.charAt(i);
            switch (state) {
                case 0 -> {
                    if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                        state = 4;
                        i += 2;
                    } else if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                        state = 5;
                        i += 2;
                    } else if (c == '\'') {
                        state = 1;
                        i++;
                    } else if (c == '"') {
                        state = 2;
                        i++;
                    } else if (c == '$') {
                        dollarTag = readDollarTag(sql, i);
                        if (dollarTag == null) {
                            i++;
                        } else {
                            state = 3;
                            i += dollarTag.length();
                        }
                    } else if (c == ';') {
                        return i;
                    } else {
                        i++;
                    }
                }
                case 1 -> {
                    if (c == '\'') {
                        if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                            i += 2;
                        } else {
                            state = 0;
                            i++;
                        }
                    } else {
                        i++;
                    }
                }
                case 2 -> {
                    if (c == '"') {
                        if (i + 1 < sql.length() && sql.charAt(i + 1) == '"') {
                            i += 2;
                        } else {
                            state = 0;
                            i++;
                        }
                    } else {
                        i++;
                    }
                }
                case 3 -> {
                    if (sql.startsWith(dollarTag, i)) {
                        i += dollarTag.length();
                        dollarTag = null;
                        state = 0;
                    } else {
                        i++;
                    }
                }
                case 4 -> {
                    if (c == '\n') {
                        state = 0;
                    }
                    i++;
                }
                case 5 -> {
                    if (c == '*' && i + 1 < sql.length() && sql.charAt(i + 1) == '/') {
                        state = 0;
                        i += 2;
                    } else {
                        i++;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 读取$$引用标签
     *
     * @param sql   sql内容
     * @param index 起始位置
     * @return 标签，非$$引用返回null
     */
    private static String readDollarTag(String sql, int index) {
        int next = sql.indexOf('$', index + 1);
        if (next < 0) {
            return null;
        }
        String tag = sql.substring(index, next + 1);
        if (tag.length() == 2) {
            return tag;
        }
        String name = tag.substring(1, tag.length() - 1);
        return name.matches("[A-Za-z_][A-Za-z0-9_]*") ? tag : null;
    }

    /**
     * 生成序列值重置sql
     *
     * <p>PostgreSQL的serial/identity自增列在带值导入后，序列当前值可能落后于数据，
     * 需要按字段最大值重置序列，否则后续新增记录会主键冲突。</p>
     *
     * @param columns 字段列表
     * @param schema  模式名称，null则使用字段中的模式，空字符串表示不限定模式
     * @return 重置sql列表
     */
    public static List<String> buildSequenceResetSql(PostgresqlColumns columns, String schema) {
        List<String> sqlList = new ArrayList<>();
        if (columns == null || columns.isEmpty()) {
            return sqlList;
        }
        String tableName = columns.tableName();
        if (StringUtil.isBlank(tableName)) {
            return sqlList;
        }
        if (schema == null) {
            schema = columns.schema();
        }
        String table = StringUtil.isBlank(schema)
                ? DBUtil.wrap(tableName, DBDialect.POSTGRESQL)
                : DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL);
        // pg_get_serial_sequence的第一个参数是文本形式的表名
        String sequenceTable = "'" + table.replace("'", "''") + "'";
        for (PostgresqlColumn column : columns) {
            if (!(column.isAutoIncrement() || column.isDefaultValueSequence() || column.isIdentityAutoIncrementType())) {
                continue;
            }
            String columnName = column.getName();
            String columnRef = DBUtil.wrap(columnName, DBDialect.POSTGRESQL);
            String columnArg = "'" + columnName.replace("'", "''") + "'";
            String sql = "SELECT setval(pg_get_serial_sequence(" + sequenceTable + ", " + columnArg + "), "
                    + "COALESCE(MAX(" + columnRef + "), 1), MAX(" + columnRef + ") IS NOT NULL) FROM " + table + ";";
            sqlList.add(sql);
        }
        return sqlList;
    }
}
