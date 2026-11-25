package cn.oyzh.easyshell.util.oracle;

import cn.oyzh.common.util.HexUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Oracle插入SQL构建器
 *
 * @author oyzh
 * @since 2026-11-25
 */
public final class OracleInsertSqlBuilder {

    /**
     * 分块大小
     */
    private static final int CHUNK_SIZE = 1000;

    /**
     * 构造方法
     */
    private OracleInsertSqlBuilder() {
    }

    /**
     * 构建插入SQL
     *
     * @param columns       字段列表
     * @param record        记录
     * @param includeFields 包含字段
     * @return 插入SQL
     */
    public static String build(OracleColumns columns, Map<String, Object> record, boolean includeFields) {
        List<OracleColumn> columnList = columns.sortOfPosition().stream()
                .filter(OracleColumn::isDmlWritable)
                .toList();
        Map<OracleColumn, Object> values = new LinkedHashMap<>();
        boolean large = false;
        for (OracleColumn column : columnList) {
            Object value = ShellOracleDataUtil.valueStandardization(record.get(column.getName()));
            values.put(column, value);
            large |= requiresLargeLiteral(column, value);
        }
        if (large) {
            return buildLarge(columns.tableName(), columnList, values, includeFields);
        }
        return buildSimple(columns.tableName(), columnList, values, includeFields);
    }

    /**
     * 构建普通插入SQL
     *
     * @param tableName     表名称
     * @param columns       字段列表
     * @param values        字段值
     * @param includeFields 包含字段
     * @return 插入SQL
     */
    private static String buildSimple(String tableName, List<OracleColumn> columns,
                                      Map<OracleColumn, Object> values, boolean includeFields) {
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(DBUtil.wrap(tableName, DBDialect.ORACLE));
        appendColumns(sql, columns, includeFields);
        sql.append(" VALUES (");
        for (OracleColumn column : columns) {
            sql.append(ShellOracleDataUtil.parameterizedForSql(column, values.get(column))).append(", ");
        }
        sql.setLength(sql.length() - 2);
        return sql.append(");").toString();
    }

    /**
     * 构建大字段插入SQL
     *
     * @param tableName     表名称
     * @param columns       字段列表
     * @param values        字段值
     * @param includeFields 包含字段
     * @return 插入SQL
     */
    private static String buildLarge(String tableName, List<OracleColumn> columns,
                                     Map<OracleColumn, Object> values, boolean includeFields) {
        Map<OracleColumn, String> variables = new LinkedHashMap<>();
        List<String> declarations = new ArrayList<>();
        List<String> assignments = new ArrayList<>();
        int variableIndex = 0;
        for (OracleColumn column : columns) {
            Object value = values.get(column);
            if (!requiresLargeLiteral(column, value)) {
                continue;
            }
            String variable = "v" + (++variableIndex);
            variables.put(column, variable);
            String type = column.getType() == null ? "" : column.getType().toUpperCase();
            if (type.contains("LONG") && type.contains("RAW")) {
                declarations.add("    " + variable + " LONG RAW;");
                assignments.add("    " + variable + " := " + rawBinaryChunks((byte[]) value) + ";");
            } else if (type.contains("LONG")) {
                declarations.add("    " + variable + " LONG;");
                assignments.add("    " + variable + " := " + rawChunks(value.toString()) + ";");
            } else if (type.contains("NCLOB")) {
                declarations.add("    " + variable + " NCLOB;");
                assignments.add("    " + variable + " := " + clobChunks(value.toString(), true) + ";");
            } else if (type.contains("BLOB")) {
                declarations.add("    " + variable + " BLOB;");
                assignments.add("    " + variable + " := TO_BLOB(" + rawBinaryChunks((byte[]) value) + ");");
            } else if (type.contains("RAW")) {
                declarations.add("    " + variable + " RAW(32767);");
                assignments.add("    " + variable + " := " + rawBinaryChunks((byte[]) value) + ";");
            } else if (type.contains("XMLTYPE")) {
                declarations.add("    " + variable + " XMLTYPE;");
                assignments.add("    " + variable + " := XMLTYPE(" + clobChunks(value.toString(), false) + ");");
            } else if (type.contains("NVARCHAR") || type.contains("NCHAR")) {
                declarations.add("    " + variable + " NVARCHAR2(32767);");
                assignments.add("    " + variable + " := " + rawChunks(value.toString()) + ";");
            } else if (type.contains("VARCHAR") || type.contains("CHAR")) {
                declarations.add("    " + variable + " VARCHAR2(32767);");
                assignments.add("    " + variable + " := " + rawChunks(value.toString()) + ";");
            } else {
                declarations.add("    " + variable + " CLOB;");
                assignments.add("    " + variable + " := " + clobChunks(value.toString(), false) + ";");
            }
        }
        StringBuilder sql = new StringBuilder("BEGIN\n  DECLARE\n");
        sql.append(String.join("\n", declarations)).append("\n  BEGIN\n");
        sql.append(String.join("\n", assignments)).append('\n');
        sql.append("    INSERT INTO ").append(DBUtil.wrap(tableName, DBDialect.ORACLE));
        appendColumns(sql, columns, includeFields);
        sql.append(" VALUES (");
        for (OracleColumn column : columns) {
            if (variables.containsKey(column)) {
                sql.append(variables.get(column));
            } else {
                sql.append(ShellOracleDataUtil.parameterizedForSql(column, values.get(column)));
            }
            sql.append(", ");
        }
        sql.setLength(sql.length() - 2);
        return sql.append(");\n  END;\nEND;\n/").toString();
    }

    /**
     * 追加字段列表
     *
     * @param sql            SQL
     * @param columns        字段列表
     * @param includeFields  包含字段
     */
    private static void appendColumns(StringBuilder sql, List<OracleColumn> columns, boolean includeFields) {
        if (!includeFields) {
            return;
        }
        sql.append(" (");
        for (OracleColumn column : columns) {
            sql.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE)).append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(")");
    }

    /**
     * 是否需要大字段写入
     *
     * @param column 字段
     * @param value  值
     * @return 结果
     */
    private static boolean requiresLargeLiteral(OracleColumn column, Object value) {
        if (value == null) {
            return false;
        }
        String type = column.getType() == null ? "" : column.getType().toUpperCase();
        if (type.contains("LONG") && !type.contains("RAW")) {
            return true;
        }
        if (type.contains("XMLTYPE")) {
            return true;
        }
        if (value instanceof Clob || value instanceof Blob) {
            return true;
        }
        if (value instanceof byte[] bytes) {
            return bytes.length > 500;
        }
        return value instanceof CharSequence sequence && sequence.length() > CHUNK_SIZE;
    }

    /**
     * 字符串分块
     *
     * @param value 值
     * @return 字面量
     */
    private static String rawChunks(String value) {
        List<String> chunks = new ArrayList<>();
        for (String chunk : split(value)) {
            chunks.add(DBUtil.wrapString(chunk, DBDialect.ORACLE));
        }
        return String.join(" || ", chunks);
    }

    /**
     * CLOB分块
     *
     * @param value 值
     * @param nclob 是否NCLOB
     * @return 字面量
     */
    private static String clobChunks(String value, boolean nclob) {
        List<String> chunks = new ArrayList<>();
        String function = nclob ? "TO_NCLOB" : "TO_CLOB";
        for (String chunk : split(value)) {
            chunks.add(function + "(" + DBUtil.wrapString(chunk, DBDialect.ORACLE) + ")");
        }
        return String.join(" || ", chunks);
    }

    /**
     * BLOB分块
     *
     * @param bytes 字节
     * @return 字面量
     */
    private static String rawBinaryChunks(byte[] bytes) {
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < bytes.length; start += 500) {
            int end = Math.min(bytes.length, start + 500);
            byte[] chunk = java.util.Arrays.copyOfRange(bytes, start, end);
            chunks.add("HEXTORAW('" + HexUtil.encodeHexStr(chunk, false) + "')");
        }
        return String.join(" || ", chunks);
    }

    /**
     * 分块
     *
     * @param value 值
     * @return 分块列表
     */
    private static List<String> split(String value) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < value.length()) {
            int end = Math.min(value.length(), start + CHUNK_SIZE);
            if (end < value.length() && Character.isHighSurrogate(value.charAt(end - 1))) {
                end--;
            }
            chunks.add(value.substring(start, end));
            start = end;
        }
        return chunks;
    }
}
