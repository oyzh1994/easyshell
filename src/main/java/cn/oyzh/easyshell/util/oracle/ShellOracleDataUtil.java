package cn.oyzh.easyshell.util.oracle;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleRecordPrimaryKey;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.common.util.HexUtil;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleDataUtil {

    //    /**
    //     * 转义符号
    //     *
    //     * @param str 内容
    //     * @return 转义后的内容
    //     */
    //    public static String escapeQuotes(String str) {
    //        if (str != null && (str.contains("'") ||
    //                str.contains("\"") ||
    //                str.contains("\\") ||
    //                str.contains("\r") ||
    //                str.contains("\n"))) {
    //            StringBuilder sb = new StringBuilder();
    //            for (char c : str.toCharArray()) {
    //                if (c == '\'') {
    //                    //                    sb.append("\\'");
    //                    sb.append(c);
    //                } else if (c == '"') {
    //                    sb.append("\\\"");
    //                } else if (c == '\\') {
    //                    sb.append("\\\\");
    //                } else if (c == '\r') {
    //                    sb.append("\\r");
    //                } else if (c == '\n') {
    //                    sb.append("\\n");
    //                } else {
    //                    sb.append(c);
    //                }
    //            }
    //            return sb.toString();
    //        }
    //        return str;
    //    }

    //    /**
    //     * 参数化，json
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForJson(OracleColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        value = valueStandardization(value);
    //        //        if (column.supportGeometry()) {
    //        //            return "ST_GeomFromText('" + value + "')";
    //        //        }
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return DateHelper.formatDate(date);
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //            if (value instanceof Date date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //        }
    //        if (column.supportJson()) {
    //            return value.toString();
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBoolean()) {
    //            if (value instanceof Boolean b) {
    //                return b ? "1" : "0";
    //            }
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        //        if (column.supportEnum()) {
    //        //            return value.toString();
    //        //        }
    //        if (column.supportString()) {
    //            return TextUtil.escape((String) value);
    //        }
    //        if (column.supportInteger() || column.supportDigits()) {
    //            return value;
    //        }
    //        return value.toString();
    //    }
    //
    //    /**
    //     * 参数化，xml
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForXml(OracleColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        value = valueStandardization(value);
    //        //        if (column.supportGeometry()) {
    //        //            return "ST_GeomFromText('" + value + "')";
    //        //        }
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return DateHelper.formatDate(date);
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //            if (value instanceof Date date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //        }
    //        if (column.supportJson()) {
    //            return value.toString();
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBoolean()) {
    //            if (value instanceof Boolean b) {
    //                return b ? "1" : "0";
    //            }
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        //        if (column.supportEnum()) {
    //        //            return value.toString();
    //        //        }
    //        if (column.supportString()) {
    //            return TextUtil.escape((String) value);
    //        }
    //        if (column.supportInteger() || column.supportDigits()) {
    //            return value;
    //        }
    //        return value.toString();
    //    }
    //
    //    /**
    //     * 参数化，csv
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForCsv(OracleColumn column, Object value) {
    //        if (value == null) {
    //            return "";
    //        }
    //        value = valueStandardization(value);
    //        //        if (column.supportGeometry()) {
    //        //            return "\"ST_GeomFromText('" + value + "')\"";
    //        //        }
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return "\"" + DateHelper.formatDate(date) + "\"";
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return "\"" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "\"";
    //            }
    //            if (value instanceof Date date) {
    //                return "\"" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "\"";
    //            }
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBoolean()) {
    //            if (value instanceof Boolean b) {
    //                return b ? "1" : "0";
    //            }
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "\"b'" + TextUtil.byteToBitStr(bytes) + "'\"";
    //        }
    //        if (column.supportString()) {
    //            return "\"" + TextUtil.escape((String) value) + "\"";
    //        }
    //        return "\"" + value + "\"";
    //    }
    //
    //    /**
    //     * 参数化，sql
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForSql(OracleColumn column, Object value) {
    //        if (value == null) {
    //            return "NULL";
    //        }
    //        value = valueStandardization(value);
    //        //        if (column.supportGeometry()) {
    //        //            return "ST_GeomFromText('" + value + "')";
    //        //        }
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return "'" + DateHelper.formatDate(date) + "'";
    //        }
    //        if (column.supportTimestamp()) {
    //            return "'" + value + "'";
    //        }
    //        if (column.supportJson()) {
    //            return "'" + value + "'";
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "NULL";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBoolean()) {
    //            if (value instanceof Boolean b) {
    //                return b ? "1" : "0";
    //            }
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "NULL";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        //        if (column.supportEnum()) {
    //        //            return "'" + value + "'";
    //        //        }
    //        if (column.supportString()) {
    //            String str = TextUtil.escape((String) value);
    //            return "'" + str + "'";
    //        }
    //        return value;
    //    }
    //
    //    /**
    //     * 参数化，html
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForHtml(OracleColumn column, Object value) {
    //        if (value == null) {
    //            return "";
    //        }
    //        //        if (column.supportGeometry()) {
    //        //            return "ST_GeomFromText('" + value + "')";
    //        //        }
    //        value = valueStandardization(value);
    //        if (column.isDateType()) {
    //            Date date = (Date) value;
    //            return DateHelper.formatDate(date);
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //            if (value instanceof Date date) {
    //                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
    //            }
    //        }
    //        if (column.supportJson()) {
    //            return value.toString();
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBoolean()) {
    //            if (value instanceof Boolean b) {
    //                return b ? "1" : "0";
    //            }
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        //        if (column.supportEnum()) {
    //        //            return value.toString();
    //        //        }
    //        if (column.supportString()) {
    //            return TextUtil.escape((String) value);
    //        }
    //        if (column.supportInteger() || column.supportDigits()) {
    //            return value;
    //        }
    //        return value.toString();
    //    }
    //
    //    /**
    //     * 参数化，xls
    //     *
    //     * @param column 字段
    //     * @param value  值
    //     * @return 参数化后的值
    //     */
    //    public static Object parameterizedForXls(OracleColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        //        if (column.supportGeometry()) {
    //        //            return "ST_GeomFromText('" + value + "')";
    //        //        }
    //        value = valueStandardization(value);
    //        if (column.isDateType()) {
    //            if (value instanceof Date date) {
    //                return DateHelper.formatDate(date);
    //            }
    //        }
    //        if (column.supportTimestamp()) {
    //            if (value instanceof LocalDateTime date) {
    //                return DateUtil.format(date, "yyyy/M/dd HH:mm:ss");
    //            }
    //            if (value instanceof Date date) {
    //                return DateUtil.format(date, "yyyy/M/dd HH:mm:ss");
    //            }
    //        }
    //        if (column.supportJson()) {
    //            return value.toString();
    //        }
    //        if (column.supportBinary()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "0x" + HexUtil.encodeHexStr(bytes, false);
    //        }
    //        if (column.supportBoolean()) {
    //            if (value instanceof Boolean b) {
    //                return b ? "1" : "0";
    //            }
    //        }
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        //        if (column.supportEnum()) {
    //        //            return value.toString();
    //        //        }
    //        if (column.supportString()) {
    //            return TextUtil.escape((String) value);
    //        }
    //        return value;
    //    }

    /**
     * 转换为插入sql
     *
     * @param columns       字段列表
     * @param record        记录
     * @param includeFields 包含字段
     * @return 插入sql
     */
    public static String toInsertSql(OracleColumns columns, OracleRecord record, boolean includeFields) {
        List<String> list = toInsertSql(columns, List.of(record), includeFields);
        return CollectionUtil.getFirst(list);
    }

    /**
     * 转换为插入sql
     *
     * @param columns 字段列表
     * @param records 记录
     * @return 插入sql
     */
    public static List<String> toInsertSql(OracleColumns columns, List<OracleRecord> records) {
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
    public static List<String> toInsertSql(OracleColumns columns, List<OracleRecord> records, boolean includeFields) {
        List<String> list = new ArrayList<>();
        String tableName = columns.tableName();
        List<OracleColumn> columnList = columns.sortOfPosition();
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.ORACLE);
        for (OracleRecord record : records) {
            StringBuilder sql = new StringBuilder(sqlBase);
            if (includeFields) {
                sql.append(" (");
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
                Object value = record.getValue(dbColumn.getName());
                value = valueStandardization(value);
                value = parameterizedForSql(dbColumn, value);
                sql.append(value).append(", ");
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
    public static String toUpdateSql(OracleColumns columns, OracleRecord record) {
        OracleRecordPrimaryKey primaryKey = ShellOracleUtil.initPrimaryKey(columns, record);
        String tableName = columns.tableName();
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ")
                .append(DBUtil.wrap(columns.schema(), tableName, DBDialect.ORACLE))
                .append(" SET ");
        boolean doSet = false;
        for (OracleColumn column : columns) {
            if (primaryKey != null && column == primaryKey.getColumn()) {
                continue;
            }
            Object value = record.getValue(column.getName());
            value = valueStandardization(value);
            value = parameterizedForSql(column, value);
            builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE));
            builder.append(" = ");
            builder.append(value);
            builder.append(", ");
            doSet = true;
        }
        if (doSet) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append(" WHERE ");
        if (primaryKey == null) {
            // 参数
            boolean first = true;
            for (OracleColumn column : columns) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" AND ");
                }
                builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE));
                builder.append(" = ");
                Object value = record.getValue(column.getName());
                value = parameterizedForSql(column, value);
                builder.append(value);
            }
        } else {
            builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.ORACLE));
            builder.append(" = ");
            Object value = parameterizedForSql(primaryKey.getColumn(), primaryKey.getData());
            builder.append(value);
        }
        builder.append(";");
        return builder.toString();
    }

    //    /**
    //     * 转换为插入json
    //     *
    //     * @param columns 字段列表
    //     * @param records 记录
    //     * @return 插入json
    //     */
    //    public static List<Map<String, Object>> toInsertJson(OracleColumns columns, List<OracleRecord> records) {
    //        List<Map<String, Object>> list = new ArrayList<>();
    //        List<OracleColumn> columnList = columns.sortOfPosition();
    //        for (OracleRecord record : records) {
    //            Map<String, Object> object = new HashMap<>();
    //            for (OracleColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = DBDataUtil.parameterizedForJson(dbColumn, value);
    //                object.put(dbColumn.getName(), value);
    //            }
    //            list.add(object);
    //        }
    //        return list;
    //    }

    //    /**
    //     * 转换为插入xml
    //     *
    //     * @param columns 字段列表
    //     * @param records 记录
    //     * @return 插入xml
    //     */
    //    public static List<Map<String, Object>> toInsertXml(OracleColumns columns, List<OracleRecord> records) {
    //        List<Map<String, Object>> list = new ArrayList<>();
    //        List<OracleColumn> columnList = columns.sortOfPosition();
    //        for (OracleRecord record : records) {
    //            Map<String, Object> object = new HashMap<>();
    //            for (OracleColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = parameterizedForXml(dbColumn, value);
    //                object.put(dbColumn.getName(), value);
    //            }
    //            list.add(object);
    //        }
    //        return list;
    //    }
    //
    //    /**
    //     * 转换为插入csv
    //     *
    //     * @param columns 字段列表
    //     * @param records 记录
    //     * @return 插入csv
    //     */
    //    public static List<List<Object>> toInsertCsv(OracleColumns columns, List<OracleRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<OracleColumn> columnList = columns.sortOfPosition();
    //        for (OracleRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (OracleColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = parameterizedForCsv(dbColumn, value);
    //                object.add(value);
    //            }
    //            list.add(object);
    //        }
    //        return list;
    //    }
    //
    //    /**
    //     * 转换为插入html
    //     *
    //     * @param columns 字段列表
    //     * @param records 记录
    //     * @return 插入html
    //     */
    //    public static List<List<Object>> toInsertHtml(OracleColumns columns, List<OracleRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<OracleColumn> columnList = columns.sortOfPosition();
    //        for (OracleRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (OracleColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = parameterizedForHtml(dbColumn, value);
    //                object.add(value);
    //            }
    //            list.add(object);
    //        }
    //        return list;
    //    }
    //
    //    /**
    //     * 转换为插入xls
    //     *
    //     * @param columns 字段列表
    //     * @param records 记录
    //     * @return 插入xls
    //     */
    //    public static List<List<Object>> toInsertXls(OracleColumns columns, List<OracleRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<OracleColumn> columnList = columns.sortOfPosition();
    //        for (OracleRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (OracleColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = parameterizedForXls(dbColumn, value);
    //                object.add(value);
    //            }
    //            list.add(object);
    //        }
    //        return list;
    //    }

    /**
     * 值规整化
     *
     * @param value 值
     * @return 规整后的值
     */
    public static Object valueStandardization(Object value) {
        if (value instanceof Clob clob) {
            try {
                return clob.getSubString(1, Math.toIntExact(clob.length()));
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to read Oracle CLOB value", ex);
            }
        }
        if (value instanceof Blob blob) {
            try {
                return blob.getBytes(1, Math.toIntExact(blob.length()));
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to read Oracle BLOB value", ex);
            }
        }
        return value;
    }

    /** 生成Oracle SQL值，日期和二进制使用Oracle原生函数，避免依赖NLS_DATE_FORMAT。 */
    public static Object parameterizedForSql(OracleColumn column, Object value) {
        if (value == null) {
            return "NULL";
        }
        if (column.supportBinary()) {
            byte[] bytes = value instanceof byte[] array ? array : HexUtil.decodeHexStr(value.toString());
            return "HEXTORAW('" + HexUtil.encodeHexStr(bytes, false) + "')";
        }
        if (column.isDateType()) {
            if (value instanceof Date date) {
                return "TO_DATE('" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:SS')";
            }
            if (value instanceof CharSequence) {
                return "TO_DATE('" + value + "','YYYY-MM-DD HH24:MI:SS')";
            }
        }
        if (column.supportTimestamp()) {
            if (value instanceof OffsetDateTime date) {
                return "TO_TIMESTAMP_TZ('" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXXX"))
                        + "','YYYY-MM-DD HH24:MI:SS.FF3TZH:TZM')";
            }
            if (value instanceof Instant date) {
                return timestampLiteral(date.atOffset(ZoneOffset.UTC).toLocalDateTime());
            }
            if (value instanceof Date date) {
                return timestampLiteral(new java.sql.Timestamp(date.getTime()).toLocalDateTime());
            }
            if (value instanceof LocalDateTime date) {
                return timestampLiteral(date);
            }
        }
        return DBUtil.wrapData(value, DBDialect.ORACLE);
    }

    private static String timestampLiteral(LocalDateTime value) {
        return "TO_TIMESTAMP('" + value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                + "','YYYY-MM-DD HH24:MI:SS.FF3')";
    }
}
