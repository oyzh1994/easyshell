package cn.oyzh.easyshell.data.oracle.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.util.oracle.OracleComplexValueUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Oracle Sql类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleSqlTypeFileWriter extends ShellOracleTypeFileWriter {

    /**
     * 字段列表
     */
    private OracleColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private final LineFileWriter writer;

    /**
     * 构造 Oracle Sql类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellOracleSqlTypeFileWriter(String filePath, DBDataExportConfig config, OracleColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    /**
     * 表名称
     */
    private String table;

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public void writeHeader() throws Exception {
        super.writeHeader();
    }

    @Override
    public void writeTrial() throws Exception {
        super.writeTrial();
    }

    @Override
    public void writeRecord(Map<String, Object> object) throws Exception {
        String tableName = this.columns.tableName();
        List<OracleColumn> columnList = this.columns.sortOfPosition();
        if (columnList.stream().anyMatch(column -> this.requiresLargeLiteral(
                column, object.get(column.getName())))) {
            this.writeLargeRecord(tableName, columnList, object);
            return;
        }
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.ORACLE);
        StringBuilder sql = new StringBuilder(sqlBase);
        if (this.config.isIncludeTitle()) {
            sql.append("(");
            for (OracleColumn dbColumn : columnList) {
                sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.ORACLE)).append(", ");
            }
            if (sql.toString().endsWith(", ")) {
                sql.delete(sql.length() - 2, sql.length());
            }
            sql.append(")");
        }
        sql.append(" VALUES (");
        for (OracleColumn dbColumn : columnList) {
            Object val = object.get(dbColumn.getName());
            val = this.parameterized(dbColumn, val, this.config);
            sql.append(val).append(", ");
        }
        if (sql.toString().endsWith(", ")) {
            sql.delete(sql.length() - 2, sql.length());
        }
        sql.append(");");
        this.writer.writeLine(sql.toString());
    }

    /**
     * Oracle 单字符串字面量不能超过 4000 字节；LONG/LOB/BLOB 大值使用匿名块变量分块赋值。
     */
    private void writeLargeRecord(String tableName, List<OracleColumn> columns,
                                  Map<String, Object> object) throws Exception {
        Map<OracleColumn, String> variables = new LinkedHashMap<>();
        List<String> declarations = new ArrayList<>();
        List<String> assignments = new ArrayList<>();
        int variableIndex = 0;
        for (OracleColumn column : columns) {
            Object value = object.get(column.getName());
            if (!this.requiresLargeLiteral(column, value)) {
                continue;
            }
            String variable = "v" + (++variableIndex);
            variables.put(column, variable);
            String type = column.getType() == null ? "" : column.getType().toUpperCase();
            if (type.contains("LONG") && type.contains("RAW")) {
                declarations.add("    " + variable + " LONG RAW;");
                assignments.add("    " + variable + " := " + this.rawBinaryChunks((byte[]) value) + ";");
            } else if (type.contains("LONG")) {
                declarations.add("    " + variable + " LONG;");
                assignments.add("    " + variable + " := " + this.rawChunks(value.toString()) + ";");
            } else if (type.contains("NCLOB")) {
                declarations.add("    " + variable + " NCLOB;");
                assignments.add("    " + variable + " := " + this.clobChunks(value.toString(), true) + ";");
            } else if (type.contains("BLOB")) {
                declarations.add("    " + variable + " BLOB;");
                assignments.add("    " + variable + " := TO_BLOB("
                        + this.rawBinaryChunks((byte[]) value) + ");");
            } else if (type.contains("RAW")) {
                declarations.add("    " + variable + " RAW(32767);");
                assignments.add("    " + variable + " := "
                        + this.rawBinaryChunks((byte[]) value) + ";");
            } else if (type.contains("XMLTYPE")) {
                declarations.add("    " + variable + " XMLTYPE;");
                assignments.add("    " + variable + " := XMLTYPE("
                        + this.clobChunks(value.toString(), false) + ");");
            } else {
                declarations.add("    " + variable + " CLOB;");
                assignments.add("    " + variable + " := " + this.clobChunks(value.toString(), false) + ";");
            }
        }
        StringBuilder sql = new StringBuilder("BEGIN\n  DECLARE\n");
        sql.append(String.join("\n", declarations)).append("\n  BEGIN\n");
        sql.append(String.join("\n", assignments)).append('\n');
        sql.append("    INSERT INTO ").append(DBUtil.wrap(tableName, DBDialect.ORACLE)).append("(");
        for (OracleColumn column : columns) {
            sql.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE)).append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(") VALUES (");
        for (OracleColumn column : columns) {
            Object value = object.get(column.getName());
            if (variables.containsKey(column)) {
                sql.append(variables.get(column));
            } else {
                sql.append(this.parameterized(column, value, this.config));
            }
            sql.append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(");\n  END;\nEND;\n/");
        this.writer.writeLine(sql.toString());
    }

    private boolean requiresLargeLiteral(OracleColumn column, Object value) {
        if (value == null) {
            return false;
        }
        String type = column.getType() == null ? "" : column.getType().toUpperCase();
        if (type.contains("LONG") && !type.contains("RAW")) {
            return true;
        }
        if (value instanceof byte[] bytes) {
            return bytes.length > 500;
        }
        return value instanceof CharSequence sequence
                && (sequence.length() > 1000 || type.contains("XMLTYPE"));
    }

    private String rawChunks(String value) {
        List<String> chunks = new ArrayList<>();
        for (String chunk : this.split(value, 1000)) {
            chunks.add(DBUtil.wrapString(chunk, DBDialect.ORACLE));
        }
        return String.join(" || ", chunks);
    }

    private String clobChunks(String value, boolean nclob) {
        List<String> chunks = new ArrayList<>();
        String function = nclob ? "TO_NCLOB" : "TO_CLOB";
        for (String chunk : this.split(value, 1000)) {
            chunks.add(function + "(" + DBUtil.wrapString(chunk, DBDialect.ORACLE) + ")");
        }
        return String.join(" || ", chunks);
    }

    private String rawBinaryChunks(byte[] bytes) {
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < bytes.length; start += 500) {
            int end = Math.min(bytes.length, start + 500);
            byte[] chunk = java.util.Arrays.copyOfRange(bytes, start, end);
            chunks.add("HEXTORAW('" + HexUtil.encodeHexStr(chunk, false) + "')");
        }
        return String.join(" || ", chunks);
    }

    private List<String> split(String value, int size) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < value.length()) {
            int end = Math.min(value.length(), start + size);
            if (end < value.length() && Character.isHighSurrogate(value.charAt(end - 1))) {
                end--;
            }
            chunks.add(value.substring(start, end));
            start = end;
        }
        return chunks;
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        this.config = null;
        this.columns = null;
    }

    @Override
    public Object parameterized(OracleColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return "NULL";
        }
        value = ShellOracleDataUtil.valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
        if (column.isObjectType() || column.isCollectionType()) {
            return OracleComplexValueUtil.toSqlLiteral(column, value, false);
        }
        return ShellOracleDataUtil.parameterizedForSql(column, value);
    }
}
