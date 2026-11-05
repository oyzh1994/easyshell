package cn.oyzh.easyshell.llm;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.easyshell.query.dameng.DamengQueryResult;
import cn.oyzh.easyshell.query.mongo.ShellMongoQueryResult;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbQueryResult;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryResult;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBRecord;
import cn.oyzh.fx.db.DBRawQueryResults;
import cn.oyzh.fx.db.query.DBQueryResult;
import cn.oyzh.fx.db.query.DBQueryResults;

import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;

/**
 * Formats database execution results for LLM tools.
 */
public final class LLMDBToolsUtil {

    private static final int MAX_ROWS = 1000;

    private static final int MAX_RESULT_SETS = 20;

    private static final int MAX_CELL_CHARS = 4096;

    private static final int MAX_BINARY_BYTES = 1024;

    private static final int MAX_SERIALIZED_CHARS = 64 * 1024;

    private LLMDBToolsUtil() {
    }

    public static String formatList(String label, Collection<String> values, String filter, int maxResults) {
        String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                if (value == null) {
                    continue;
                }
                if (query.isBlank() || value.toLowerCase(Locale.ROOT).contains(query)) {
                    matched.add(value);
                }
            }
        }
        matched.sort(String.CASE_INSENSITIVE_ORDER);
        int limit = maxResults <= 0 ? 200 : Math.min(maxResults, 1000);
        int shown = Math.min(matched.size(), limit);
        StringBuilder output = new StringBuilder();
        output.append("count: ").append(matched.size()).append('\n');
        output.append("shown: ").append(shown).append('\n');
        for (int index = 0; index < shown; index++) {
            if (label != null && !label.isBlank()) {
                output.append(label).append(": ");
            }
            output.append(matched.get(index)).append('\n');
        }
        return output.toString();
    }

    public static String format(DBQueryResults<? extends DBQueryResult> results) {
        if (results == null) {
            return "No execution result.";
        }
        if (!results.isSuccess()) {
            return "Execution failed: " + results.getErrMsg();
        }
        if (results.isEmpty()) {
            return "OK";
        }
        StringBuilder output = new StringBuilder();
        for (DBQueryResult result : results.getResults()) {
            if (!output.isEmpty()) {
                output.append('\n');
            }
            output.append(formatResult(result));
        }
        return output.toString();
    }

    /**
     * 将原始JDBC结果集安全序列化为JSON，避免模型序列化器遍历JDBC内部对象图。
     *
     * @param results 原始结果集
     * @return 安全的JSON文本
     */
    public static String formatRaw(DBRawQueryResults results) {
        if (results == null) {
            return "No execution result.";
        }
        List<ResultSet> resultSets = results.getResultSets();
        if (resultSets == null || resultSets.isEmpty()) {
            return "OK";
        }

        Map<String, Object> root = new LinkedHashMap<>();
        List<Object> serializedResultSets = new ArrayList<>();
        int resultSetLimit = Math.min(resultSets.size(), MAX_RESULT_SETS);
        boolean truncated = resultSets.size() > resultSetLimit;
        for (int index = 0; index < resultSetLimit; index++) {
            Map<String, Object> serialized = formatResultSet(resultSets.get(index), index + 1);
            serializedResultSets.add(serialized);
            if (Boolean.TRUE.equals(serialized.get("truncated"))) {
                truncated = true;
            }
        }
        root.put("resultSetCount", resultSets.size());
        root.put("returnedResultSets", serializedResultSets.size());
        root.put("truncated", truncated);
        root.put("resultSets", serializedResultSets);

        String json = JSONUtil.toJson(root);
        return json.length() <= MAX_SERIALIZED_CHARS ? json : truncateJson(json);
    }

    private static Map<String, Object> formatResultSet(ResultSet resultSet, int index) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("index", index);
        if (resultSet == null) {
            result.put("error", "Result set is unavailable.");
            return result;
        }
        try {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            List<String> columns = new ArrayList<>(columnCount);
            List<String> columnTypes = new ArrayList<>(columnCount);
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                String label = metadata.getColumnLabel(columnIndex);
                columns.add(label == null || label.isBlank() ? "column" + columnIndex : label);
                columnTypes.add(metadata.getColumnTypeName(columnIndex));
            }

            List<Object> rows = new ArrayList<>();
            boolean truncated = false;
            int usedChars = 0;
            while (rows.size() < MAX_ROWS && resultSet.next()) {
                List<Object> row = new ArrayList<>(columnCount);
                int rowChars = 0;
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    Object value = safeResultValue(resultSet, columnIndex);
                    row.add(value);
                    rowChars += estimateLength(value) + 4;
                }
                if (usedChars + rowChars > MAX_SERIALIZED_CHARS && !rows.isEmpty()) {
                    truncated = true;
                    break;
                }
                rows.add(row);
                usedChars += rowChars;
                if (usedChars >= MAX_SERIALIZED_CHARS) {
                    truncated = true;
                    break;
                }
            }
            if (rows.size() >= MAX_ROWS) {
                truncated = true;
            }
            result.put("columns", columns);
            result.put("columnTypes", columnTypes);
            result.put("rowCount", rows.size());
            result.put("truncated", truncated);
            result.put("rows", rows);
        } catch (SQLException ex) {
            result.put("error", "Failed to serialize result set: " + ex.getMessage());
        }
        return result;
    }

    private static Object safeResultValue(ResultSet resultSet, int columnIndex) {
        try {
            return safeValue(resultSet.getObject(columnIndex));
        } catch (Exception ex) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("type", "unserializable");
            error.put("columnIndex", columnIndex);
            error.put("error", ex.getMessage());
            return error;
        }
    }

    private static Object safeValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof byte[] bytes) {
                return binaryValue(bytes, bytes.length);
            }
            if (value instanceof Blob blob) {
                long length = blob.length();
                int readLength = (int) Math.min(length, MAX_BINARY_BYTES);
                byte[] bytes = readLength == 0 ? new byte[0] : blob.getBytes(1, readLength);
                return binaryValue(bytes, length);
            }
            if (value instanceof Clob clob) {
                long length = clob.length();
                int readLength = (int) Math.min(length, MAX_CELL_CHARS);
                String text = readLength == 0 ? "" : clob.getSubString(1, readLength);
                return textValue(text, length, readLength < length);
            }
            if (value instanceof SQLXML sqlxml) {
                return textValue(sqlxml.getString(), -1, false);
            }
            if (value instanceof java.sql.Array array) {
                return safeValue(array.getArray());
            }
            if (value instanceof String text) {
                return textValue(text, text.length(), text.length() > MAX_CELL_CHARS);
            }
            if (value instanceof Number || value instanceof Boolean) {
                return value;
            }
            if (value instanceof java.time.temporal.TemporalAccessor
                    || value instanceof java.util.Date
                    || value instanceof java.time.Duration) {
                return value.toString();
            }
            Class<?> valueClass = value.getClass();
            if (valueClass.isArray()) {
                int length = Array.getLength(value);
                List<Object> values = new ArrayList<>(length);
                for (int index = 0; index < length; index++) {
                    values.add(safeValue(Array.get(value, index)));
                }
                return values;
            }
            return textValue(String.valueOf(value), -1, false);
        } catch (Exception ex) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("type", "unserializable");
            error.put("valueType", value.getClass().getName());
            error.put("error", ex.getMessage());
            return error;
        }
    }

    private static Object textValue(String text, long originalLength, boolean truncated) {
        if (text == null) {
            return null;
        }
        if (!truncated && text.length() <= MAX_CELL_CHARS) {
            return text;
        }
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", "text");
        value.put("truncated", true);
        value.put("length", originalLength < 0 ? text.length() : originalLength);
        value.put("preview", text.substring(0, Math.min(text.length(), MAX_CELL_CHARS)));
        return value;
    }

    private static Object binaryValue(byte[] bytes, long totalLength) {
        int length = Math.min(bytes.length, MAX_BINARY_BYTES);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", "binary");
        value.put("encoding", "base64");
        value.put("length", totalLength);
        value.put("truncated", totalLength > length);
        value.put("data", Base64.getEncoder().encodeToString(
                java.util.Arrays.copyOf(bytes, length)));
        return value;
    }

    private static int estimateLength(Object value) {
        if (value == null) {
            return 4;
        }
        if (value instanceof String text) {
            return text.length() + 2;
        }
        if (value instanceof Map<?, ?> map) {
            int length = 2;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                length += estimateLength(entry.getKey()) + estimateLength(entry.getValue()) + 4;
            }
            return length;
        }
        if (value instanceof List<?> list) {
            int length = 2;
            for (Object item : list) {
                length += estimateLength(item) + 2;
            }
            return length;
        }
        return String.valueOf(value).length() + 2;
    }

    private static String truncateJson(String json) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("truncated", true);
        value.put("originalLength", json.length());
        value.put("preview", json.substring(0, MAX_SERIALIZED_CHARS));
        return JSONUtil.toJson(value);
    }

    private static String formatResult(DBQueryResult result) {
        if (result == null) {
            return "OK";
        }
        if (!result.isSuccess()) {
            return "ERROR: " + result.getMsg();
        }
        if (result.getUpdateCount() > 0) {
            StringBuilder output = new StringBuilder("OK, ")
                    .append(result.getUpdateCount())
                    .append(" rows affected");
            if (result.getUsedMs() > 0) {
                output.append(" (").append(result.getUsedMs()).append(" ms)");
            }
            return output.toString();
        }
        if (result instanceof ShellMysqlQueryResult mysqlResult
                && mysqlResult.getRecords() != null
                && !mysqlResult.getRecords().isEmpty()) {
            return formatRows(mysqlResult.columnList(), mysqlResult.getRecords(), result);
        }
        if (result instanceof ShellMariadbQueryResult mariadbResult
                && mariadbResult.getRecords() != null
                && !mariadbResult.getRecords().isEmpty()) {
            return formatRows(mariadbResult.columnList(), mariadbResult.getRecords(), result);
        }
        if (result instanceof DamengQueryResult damengResult
                && damengResult.getRecords() != null
                && !damengResult.getRecords().isEmpty()) {
            return formatRows(damengResult.columnList(), damengResult.getRecords(), result);
        }
        if (result instanceof ShellMongoQueryResult mongoResult
                && mongoResult.getRecords() != null
                && !mongoResult.getRecords().isEmpty()) {
            return formatRows(mongoResult.columnList(), mongoResult.getRecords(), result);
        }
        if (result instanceof SQLiteExecuteResult sqliteResult
                && !sqliteResult.getRecords().isEmpty()) {
            return formatRows(sqliteResult.columnList(), sqliteResult.getRecords(), result);
        }
        return "OK";
    }

    private static String formatRows(
            List<? extends DBColumn> columns,
            List<? extends DBRecord> records,
            DBQueryResult result) {
        if (columns == null || columns.isEmpty() || records == null || records.isEmpty()) {
            return "OK";
        }
        StringBuilder output = new StringBuilder();
        output.append(columns.stream().map(DBColumn::getName).reduce((left, right) -> left + " | " + right).orElse(""));
        output.append('\n');
        int shownRows = Math.min(records.size(), MAX_ROWS);
        for (int rowIndex = 0; rowIndex < shownRows; rowIndex++) {
            DBRecord record = records.get(rowIndex);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                if (columnIndex > 0) {
                    output.append(" | ");
                }
                output.append(formatValue(record.getValue(columns.get(columnIndex).getName())));
            }
            output.append('\n');
        }
        if (records.size() > shownRows) {
            output.append("... ").append(records.size() - shownRows).append(" more rows omitted");
        } else {
            output.append(records.size()).append(" row(s)");
        }
        if (result.getUsedMs() > 0) {
            output.append(" (").append(result.getUsedMs()).append(" ms)");
        }
        return output.toString();
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        return value.toString()
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("|", "\\|");
    }
}
