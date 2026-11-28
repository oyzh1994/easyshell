package cn.oyzh.easyshell.util.h2;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2RecordPrimaryKey;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.generator.record.H2RecordSqlGenerator;
import com.alibaba.fastjson2.JSON;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * H2 数据值和 DML 转换工具。
 *
 * @since 2026-11-02
 */
public final class ShellH2DataUtil {

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .toFormatter();

    private static final DateTimeFormatter OFFSET_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendOffsetId()
            .toFormatter();

    private ShellH2DataUtil() {
    }

    /**
     * 转换为插入sql
     *
     * @param columns       字段列表
     * @param record        记录
     * @param includeFields 包含字段
     * @return 插入sql
     */
    public static String toInsertSql(H2Columns columns, H2Record record, boolean includeFields) {
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
    public static List<String> toInsertSql(H2Columns columns, List<H2Record> records) {
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
    public static List<String> toInsertSql(H2Columns columns, List<H2Record> records, boolean includeFields) {
        List<String> list = new ArrayList<>();
        String tableName = columns.tableName();
        List<H2Column> columnList = columns.sortOfPosition().stream()
                .filter(column -> !column.isGenerated())
                .toList();
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.H2);
        for (H2Record record : records) {
            StringBuilder sql = new StringBuilder(sqlBase);
            if (columnList.isEmpty()) {
                sql.append(" DEFAULT VALUES;");
                list.add(sql.toString());
                continue;
            }
            if (includeFields) {
                sql.append(" (");
                for (H2Column column : columnList) {
                    sql.append(DBUtil.wrap(column.getName(), DBDialect.H2)).append(", ");
                }
                if (sql.toString().endsWith(", ")) {
                    sql.delete(sql.length() - 2, sql.length());
                }
                sql.append(")");
            }
            sql.append(" VALUES (");
            for (H2Column column : columnList) {
                Object value = normalizeValue(column, record.getValue(column.getName()));
                sql.append(H2GeneratorUtil.literal(value)).append(", ");
            }
            if (sql.toString().endsWith(", ")) {
                sql.delete(sql.length() - 2, sql.length());
            }
            sql.append(");");
            list.add(sql.toString());
        }
        return list;
    }

    /**
     * 转换为修改sql
     *
     * @param columns 字段列表
     * @param record  记录
     * @return 修改sql
     */
    public static String toUpdateSql(H2Columns columns, H2Record record) {
        H2RecordPrimaryKey primaryKey = ShellH2Util.initPrimaryKey(columns, record);
        String tableName = columns.tableName();
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ")
                .append(DBUtil.wrap(tableName, DBDialect.H2))
                .append(" SET ");
        boolean doSet = false;
        for (H2Column column : columns) {
            if (column.isGenerated()) {
                continue;
            }
            if (primaryKey != null && column == primaryKey.getColumn()) {
                continue;
            }
            Object value = normalizeValue(column, record.getValue(column.getName()));
            builder.append(DBUtil.wrap(column.getName(), DBDialect.H2));
            builder.append(" = ");
            builder.append(H2GeneratorUtil.literal(value));
            builder.append(", ");
            doSet = true;
        }
        if (doSet) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append(" WHERE ");
        if (primaryKey == null) {
            boolean first = true;
            for (H2Column column : columns) {
                if (column.isGenerated()) {
                    continue;
                }
                if (first) {
                    first = false;
                } else {
                    builder.append(" AND ");
                }
                Object value = normalizeValue(column, record.getValue(column.getName()));
                builder.append(DBUtil.wrap(column.getName(), DBDialect.H2));
                builder.append(value == null ? " IS NULL" : " = " + H2GeneratorUtil.literal(value));
            }
        } else {
            Object value = normalizeValue(primaryKey.getColumn(), primaryKey.getData());
            builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.H2));
            builder.append(value == null ? " IS NULL" : " = " + H2GeneratorUtil.literal(value));
        }
        builder.append(";");
        return builder.toString();
    }

    public static List<String> toInsertSql(String tableName, List<H2Column> columns,
                                           List<Map<String, Object>> records) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("H2 insert columns are empty");
        }
        if (records == null) {
            throw new IllegalArgumentException("H2 insert records are null");
        }
        List<String> sqlList = new ArrayList<>();
        for (Map<String, Object> record : records) {
            List<String> insertColumns = insertColumns(columns, record);
            if (insertColumns.isEmpty()) {
                sqlList.add("INSERT INTO " + H2GeneratorUtil.quote(tableName) + " DEFAULT VALUES;");
            } else {
                sqlList.add(H2RecordSqlGenerator.insertWithTypes(tableName,
                        columns.stream()
                                .filter(column -> insertColumns.contains(column.getName()))
                                .toList(),
                        normalize(columns, record)));
            }
        }
        return sqlList;
    }

    public static String toUpdateSql(String tableName, List<H2Column> columns,
                                     Map<String, Object> record, Map<String, Object> primaryKey) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("H2 update columns are empty");
        }
        if (record == null) {
            throw new IllegalArgumentException("H2 update record is null");
        }
        if (primaryKey == null || primaryKey.isEmpty()) {
            throw new IllegalArgumentException("H2 update primary key is empty");
        }
        List<String> assignments = new ArrayList<>();
        Map<String, Object> values = normalize(columns, record);
        for (H2Column column : columns) {
            if (!column.isGenerated() && !primaryKey.containsKey(column.getName())) {
                assignments.add(H2GeneratorUtil.quote(column.getName()) + " = "
                        + H2GeneratorUtil.literal(values.get(column.getName())));
            }
        }
        List<String> conditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : primaryKey.entrySet()) {
            conditions.add(H2GeneratorUtil.quote(entry.getKey()) + " = "
                    + H2GeneratorUtil.literal(entry.getValue()));
        }
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("H2 update has no writable columns");
        }
        return "UPDATE " + H2GeneratorUtil.quote(tableName) + " SET "
                + String.join(", ", assignments) + " WHERE " + String.join(" AND ", conditions) + ";";
    }

    public static String toUpdateSql(String tableName, List<H2Column> columns,
                                     Map<String, Object> record, Map<String, Object> original,
                                     List<String> keyColumns) {
        Map<String, Object> values = normalize(columns, record);
        List<String> assignments = new ArrayList<>();
        for (H2Column column : columns) {
            boolean keyUnchanged = keyColumns != null && keyColumns.contains(column.getName())
                    && java.util.Objects.equals(values.get(column.getName()), original.get(column.getName()));
            if (!column.isGenerated() && !keyUnchanged && values.containsKey(column.getName())) {
                assignments.add(H2GeneratorUtil.quote(column.getName()) + " = "
                        + H2GeneratorUtil.literal(values.get(column.getName())));
            }
        }
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("H2 update has no writable columns");
        }
        return "UPDATE " + H2GeneratorUtil.quote(tableName) + " SET "
                + String.join(", ", assignments) + whereSql(columns, original, keyColumns) + ";";
    }

    public static String toDeleteSql(String tableName, List<H2Column> columns,
                                     Map<String, Object> original, List<String> keyColumns) {
        return "DELETE FROM " + H2GeneratorUtil.quote(tableName)
                + whereSql(columns, original, keyColumns) + ";";
    }

    private static String whereSql(List<H2Column> columns, Map<String, Object> original,
                                   List<String> keyColumns) {
        List<String> conditions = new ArrayList<>();
        boolean useKeys = keyColumns != null && !keyColumns.isEmpty();
        for (H2Column column : columns) {
            if (column.isGenerated()) {
                continue;
            }
            if (useKeys && !keyColumns.contains(column.getName())) {
                continue;
            }
            Object value = original.get(column.getName());
            conditions.add(H2GeneratorUtil.quote(column.getName())
                    + (value == null ? " IS NULL" : " = " + H2GeneratorUtil.literal(value)));
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("H2 record condition is empty");
        }
        return " WHERE " + String.join(" AND ", conditions);
    }

    public static Map<String, Object> normalize(List<H2Column> columns, Map<String, Object> record) {
        // 外部文件表头（如 CSV）与实际列名可能存在大小写差异，按大小写不敏感匹配
        Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (record != null) {
            lookup.putAll(record);
        }
        Map<String, Object> values = new HashMap<>();
        for (H2Column column : columns) {
            if (column.isGenerated()) {
                continue;
            }
            if (lookup.containsKey(column.getName())) {
                values.put(column.getName(), normalizeValue(column, lookup.get(column.getName())));
            }
        }
        return values;
    }

    private static Object normalizeValue(H2Column column, Object value) {
        value = valueStandardization(column, value);
        if (value == null || value instanceof byte[]) {
            return value;
        }
        if (ShellH2ColumnUtil.isDateType(column.getType())
                || ShellH2ColumnUtil.isTimeType(column.getType())) {
            value = normalizeTemporalValue(column, value);
        }
        if (value instanceof CharSequence text
                && (text.isEmpty() || (ShellH2ColumnUtil.isJsonType(column.getType())
                && text.toString().isBlank()))) {
            return ShellH2ColumnUtil.isTextType(column.getType()) ? text.toString() : null;
        }
        value = DBUtil.unwrapData(value, DBDialect.H2);
        if (value == null) {
            return null;
        }
        if (ShellH2ColumnUtil.isBlobType(column.getType())
                && value instanceof CharSequence text) {
            byte[] bytes = parseBlobLiteral(text.toString());
            if (bytes != null) {
                return bytes;
            }
        }
        String text = String.valueOf(value);
        if (text.isBlank() && !ShellH2ColumnUtil.isTextType(column.getType())) {
            return null;
        }
        if (ShellH2ColumnUtil.isBooleanType(column.getType())) {
            if (value instanceof Boolean) {
                return value;
            }
            String bool = text.trim();
            if ("true".equalsIgnoreCase(bool) || "1".equals(bool)) {
                return true;
            }
            if ("false".equalsIgnoreCase(bool) || "0".equals(bool)) {
                return false;
            }
            return value;
        }
        if (ShellH2ColumnUtil.isIntegerType(column.getType())) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return value;
            }
        }
        if (ShellH2ColumnUtil.isFloatingType(column.getType())) {
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException ignored) {
                return value;
            }
        }
        if (ShellH2ColumnUtil.isDecimalType(column.getType())) {
            try {
                return new BigDecimal(text.trim());
            } catch (NumberFormatException ignored) {
                return value;
            }
        }
        return value;
    }

    /**
     * 按 H2 字段类型归一化日期与时间文本。
     *
     * <p>JSON/CSV 等文件通常把 DATE 和 TIME 导出成完整的日期时间字符串，
     * H2 强类型解析不接受这种格式，这里按列类型裁剪并校验。</p>
     *
     * @param column 字段
     * @param value  值
     * @return 归一化后的值
     */
    public static Object normalizeTemporalValue(H2Column column, Object value) {
        if (column == null || value == null) {
            return value;
        }
        if (ShellH2ColumnUtil.isDateType(column.getType())) {
            LocalDate date = toLocalDate(value);
            return date == null ? value : date.toString();
        }
        if (ShellH2ColumnUtil.isTimeType(column.getType())) {
            String time = toTimeText(value);
            return time == null ? value : time;
        }
        return value;
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof CharSequence text) {
            String date = text.toString().trim();
            if (date.length() >= 10) {
                date = date.substring(0, 10);
            }
            try {
                return LocalDate.parse(date);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String toTimeText(Object value) {
        if (value instanceof LocalTime time) {
            return time.format(TIME_FORMATTER);
        }
        if (value instanceof OffsetTime time) {
            return time.format(OFFSET_TIME_FORMATTER);
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalTime().format(TIME_FORMATTER);
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.toOffsetTime().format(OFFSET_TIME_FORMATTER);
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.toOffsetDateTime().toOffsetTime().format(OFFSET_TIME_FORMATTER);
        }
        if (value instanceof java.sql.Time time) {
            return time.toLocalTime().format(TIME_FORMATTER);
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault())
                    .toLocalTime().format(TIME_FORMATTER);
        }
        if (value instanceof CharSequence text) {
            String time = text.toString().trim();
            int separator = Math.max(time.indexOf('T'), time.indexOf(' '));
            if (separator >= 0 && separator + 1 < time.length()) {
                time = time.substring(separator + 1).trim();
            }
            try {
                return time.indexOf('+') >= 0 || time.lastIndexOf('-') > 0
                        ? OffsetTime.parse(time).format(OFFSET_TIME_FORMATTER)
                        : LocalTime.parse(time).format(TIME_FORMATTER);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
        return null;
    }

    private static byte[] parseBlobLiteral(String value) {
        String text = value.trim();
        String hex = null;
        if (text.length() >= 3
                && (text.startsWith("X'") || text.startsWith("x'"))
                && text.endsWith("'")) {
            hex = text.substring(2, text.length() - 1);
        } else if (text.length() > 2
                && (text.startsWith("0x") || text.startsWith("0X"))) {
            hex = text.substring(2);
        }
        if (hex == null || (hex.length() & 1) != 0
                || !hex.matches("[0-9a-fA-F]*")) {
            return null;
        }
        try {
            return HexFormat.of().parseHex(hex);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static String formatValue(Object value, H2Column column) {
        value = valueStandardization(column, value);
        if (value == null) {
            return null;
        }
        if (ShellH2ColumnUtil.isBlobType(column == null ? null : column.getType()) && value instanceof byte[] bytes) {
            return H2GeneratorUtil.literal(bytes);
        }
        return String.valueOf(value);
    }

    /**
     * 值规整化
     *
     * @param column 字段
     * @param value  值
     * @return 规整后的值
     */
    public static Object valueStandardization(H2Column column, Object value) {
        return jdbcValue(column, value);
    }

    /**
     * 转换为适合通过 PreparedStatement 绑定的 H2 值。
     *
     * <p>H2 的 JSON 是强类型列。Fastjson 对象直接交给 JDBC 会被绑定成
     * {@code JAVA_OBJECT}，无法转换为 JSON，因此这里统一序列化为 JSON 文本。</p>
     *
     * @param column 字段
     * @param value  原始值
     * @return JDBC 绑定值
     */
    public static Object jdbcValue(DBColumn column, Object value) {
        if (column != null && ShellH2ColumnUtil.isJsonType(column.getType())) {
            if (value instanceof byte[] bytes) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            if (value != null && !(value instanceof CharSequence)) {
                return JSON.toJSONString(value);
            }
        }
        return valueStandardization(value);
    }

    /**
     * 值规整化，处理H2 JDBC返回的LOB、XML等标准JDBC扩展类型。
     *
     * @param value 值
     * @return 规整后的值
     */
    public static Object valueStandardization(Object value) {
        if (value == null || value instanceof byte[]) {
            return value;
        }
        if (value instanceof Clob clob) {
            try {
                return clob.getSubString(1, Math.toIntExact(clob.length()));
            } catch (SQLException ex) {
                throw new ShellException(ex);
            }
        }
        if (value instanceof Blob blob) {
            try {
                return blob.getBytes(1, Math.toIntExact(blob.length()));
            } catch (SQLException ex) {
                throw new ShellException(ex);
            }
        }
        if (value instanceof SQLXML sqlXml) {
            try {
                return sqlXml.getString();
            } catch (SQLException ex) {
                throw new ShellException(ex);
            }
        }
        if (value instanceof Array || value instanceof Struct || value instanceof Ref || value instanceof RowId) {
            return value.toString();
        }
        return value;
    }

    private static List<String> insertColumns(List<H2Column> columns, Map<String, Object> record) {
        // 记录键来自外部文件表头，与实际列名可能存在大小写差异，按大小写不敏感匹配
        Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (record != null) {
            lookup.putAll(record);
        }
        return columns.stream()
                .filter(column -> !column.isGenerated())
                .filter(column -> lookup.containsKey(column.getName()))
                .map(H2Column::getName)
                .toList();
    }
}
