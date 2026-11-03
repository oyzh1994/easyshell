package cn.oyzh.easyshell.util.sqlite;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.generator.record.SQLiteRecordSqlGenerator;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite 数据值和 DML 转换工具。
 *
 * @since 2026-11-02
 */
public final class ShellSqliteDataUtil {

    private ShellSqliteDataUtil() {
    }

    /**
     * 转换为插入sql
     *
     * @param columns       字段列表
     * @param record        记录
     * @param includeFields 包含字段
     * @return 插入sql
     */
    public static String toInsertSql(SQLiteColumns columns, SQLiteRecord record, boolean includeFields) {
        List<String> sqlList = toInsertSql(columns, List.of(record), includeFields);
        return CollectionUtil.getFirst(sqlList);
    }

    /**
     * 转换为插入sql
     *
     * @param columns 字段列表
     * @param records 记录
     * @return 插入sql
     */
    public static List<String> toInsertSql(SQLiteColumns columns, List<SQLiteRecord> records) {
        return toInsertSql(columns, records, false);
    }

    /**
     * 转换为插入sql
     *
     * @param columns       字段列表
     * @param records       记录
     * @param includeFields 包含字段
     * @return 插入sql
     */
    public static List<String> toInsertSql(SQLiteColumns columns, List<SQLiteRecord> records, boolean includeFields) {
        List<String> sqlList = new ArrayList<>();
        for (SQLiteRecord record : records) {
            Map<String, Object> values = record.getValues();
            List<String> insertColumns = insertColumns(columns, values);
            if (insertColumns.isEmpty()) {
                sqlList.add("INSERT INTO " + SQLiteGeneratorUtil.quote(columns.tableName()) + " DEFAULT VALUES;");
            } else if (includeFields) {
                sqlList.add(toInsertSql(columns.tableName(), columns, List.of(values)).getFirst());
            } else {
                Map<String, Object> normalized = normalize(columns, values);
                List<String> data = insertColumns.stream()
                        .map(column -> SQLiteGeneratorUtil.literal(normalized.get(column)))
                        .toList();
                sqlList.add("INSERT INTO " + SQLiteGeneratorUtil.quote(columns.tableName())
                        + " VALUES (" + String.join(", ", data) + ");");
            }
        }
        return sqlList;
    }

    /**
     * 转换为修改sql
     *
     * @param columns 字段列表
     * @param record  记录
     * @return 修改sql
     */
    public static String toUpdateSql(SQLiteColumns columns, SQLiteRecord record) {
        List<String> keyColumns = columns.primaryKeys().stream()
                .map(SQLiteColumn::getName)
                .toList();
        return toUpdateSql(columns.tableName(), columns, record.getValues(),
                record.getOriginalValues(), keyColumns);
    }

    public static List<String> toInsertSql(String tableName, List<SQLiteColumn> columns,
                                           List<Map<String, Object>> records) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("SQLite insert columns are empty");
        }
        if (records == null) {
            throw new IllegalArgumentException("SQLite insert records are null");
        }
        List<String> sqlList = new ArrayList<>();
        for (Map<String, Object> record : records) {
            List<String> insertColumns = insertColumns(columns, record);
            if (insertColumns.isEmpty()) {
                sqlList.add("INSERT INTO " + SQLiteGeneratorUtil.quote(tableName) + " DEFAULT VALUES;");
            } else {
                sqlList.add(SQLiteRecordSqlGenerator.insert(tableName, insertColumns,
                        normalize(columns, record)));
            }
        }
        return sqlList;
    }

    public static String toUpdateSql(String tableName, List<SQLiteColumn> columns,
                                     Map<String, Object> record, Map<String, Object> primaryKey) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("SQLite update columns are empty");
        }
        if (record == null) {
            throw new IllegalArgumentException("SQLite update record is null");
        }
        if (primaryKey == null || primaryKey.isEmpty()) {
            throw new IllegalArgumentException("SQLite update primary key is empty");
        }
        List<String> assignments = new ArrayList<>();
        Map<String, Object> values = normalize(columns, record);
        for (SQLiteColumn column : columns) {
            if (!column.isGenerated() && !primaryKey.containsKey(column.getName())) {
                assignments.add(SQLiteGeneratorUtil.quote(column.getName()) + " = "
                        + SQLiteGeneratorUtil.literal(values.get(column.getName())));
            }
        }
        List<String> conditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : primaryKey.entrySet()) {
            conditions.add(SQLiteGeneratorUtil.quote(entry.getKey()) + " = "
                    + SQLiteGeneratorUtil.literal(entry.getValue()));
        }
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("SQLite update has no writable columns");
        }
        return "UPDATE " + SQLiteGeneratorUtil.quote(tableName) + " SET "
                + String.join(", ", assignments) + " WHERE " + String.join(" AND ", conditions) + ";";
    }

    public static String toUpdateSql(String tableName, List<SQLiteColumn> columns,
                                     Map<String, Object> record, Map<String, Object> original,
                                     List<String> keyColumns) {
        Map<String, Object> values = normalize(columns, record);
        List<String> assignments = new ArrayList<>();
        for (SQLiteColumn column : columns) {
            boolean keyUnchanged = keyColumns != null && keyColumns.contains(column.getName())
                    && java.util.Objects.equals(values.get(column.getName()), original.get(column.getName()));
            if (!column.isGenerated() && !keyUnchanged && values.containsKey(column.getName())) {
                assignments.add(SQLiteGeneratorUtil.quote(column.getName()) + " = "
                        + SQLiteGeneratorUtil.literal(values.get(column.getName())));
            }
        }
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("SQLite update has no writable columns");
        }
        return "UPDATE " + SQLiteGeneratorUtil.quote(tableName) + " SET "
                + String.join(", ", assignments) + whereSql(columns, original, keyColumns) + ";";
    }

    public static String toDeleteSql(String tableName, List<SQLiteColumn> columns,
                                     Map<String, Object> original, List<String> keyColumns) {
        return "DELETE FROM " + SQLiteGeneratorUtil.quote(tableName)
                + whereSql(columns, original, keyColumns) + ";";
    }

    private static String whereSql(List<SQLiteColumn> columns, Map<String, Object> original,
                                   List<String> keyColumns) {
        List<String> conditions = new ArrayList<>();
        boolean useKeys = keyColumns != null && !keyColumns.isEmpty();
        for (SQLiteColumn column : columns) {
            if (column.isGenerated()) {
                continue;
            }
            if (useKeys && !keyColumns.contains(column.getName())) {
                continue;
            }
            Object value = original.get(column.getName());
            conditions.add(SQLiteGeneratorUtil.quote(column.getName())
                    + (value == null ? " IS NULL" : " = " + SQLiteGeneratorUtil.literal(value)));
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("SQLite record condition is empty");
        }
        return " WHERE " + String.join(" AND ", conditions);
    }

    public static Map<String, Object> normalize(List<SQLiteColumn> columns, Map<String, Object> record) {
        Map<String, Object> values = new HashMap<>(record);
        for (SQLiteColumn column : columns) {
            if (column.isGenerated()) {
                values.remove(column.getName());
                continue;
            }
            Object value = values.get(column.getName());
            if (value == null) {
                continue;
            }
            if (value instanceof byte[]) {
                continue;
            }
            if (value instanceof CharSequence text && text.isEmpty()) {
                values.put(column.getName(), "");
                continue;
            }
            value = DBUtil.unwrapData(value, DBDialect.SQLITE);
            if (value == null) {
                values.put(column.getName(), null);
                continue;
            }
            String text = String.valueOf(value);
            if (text.isEmpty()) {
                values.put(column.getName(), "");
            } else if (ShellSqliteColumnUtil.isIntegerType(column.getType())) {
                try {
                    values.put(column.getName(), Long.parseLong(text.trim()));
                } catch (NumberFormatException ignored) {
                    values.put(column.getName(), value);
                }
            } else if (ShellSqliteColumnUtil.isFloatingType(column.getType())) {
                try {
                    values.put(column.getName(), Double.parseDouble(text.trim()));
                } catch (NumberFormatException ignored) {
                    values.put(column.getName(), value);
                }
            } else if (ShellSqliteColumnUtil.isDecimalType(column.getType())) {
                try {
                    values.put(column.getName(), new BigDecimal(text.trim()));
                } catch (NumberFormatException ignored) {
                    values.put(column.getName(), value);
                }
            } else {
                values.put(column.getName(), value);
            }
        }
        return values;
    }

    public static String formatValue(Object value, SQLiteColumn column) {
        if (value == null) {
            return "NULL";
        }
        if (ShellSqliteColumnUtil.isBlobType(column == null ? null : column.getType()) && value instanceof byte[] bytes) {
            return SQLiteGeneratorUtil.literal(bytes);
        }
        return String.valueOf(value);
    }

    private static List<String> insertColumns(List<SQLiteColumn> columns, Map<String, Object> record) {
        return columns.stream()
                .filter(column -> !column.isGenerated())
                .filter(column -> record != null && record.containsKey(column.getName()))
                .map(SQLiteColumn::getName)
                .toList();
    }
}
