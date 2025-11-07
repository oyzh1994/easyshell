package cn.oyzh.easyshell.util.mysql;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/08/26
 */
public class ShellMysqlDataUtil {

    /**
     * 转义符号
     *
     * @param str 内容
     * @return 转义后的内容
     */
    public static String escapeQuotes(String str) {
        if (str != null && (str.contains("'") ||
                str.contains("\"") ||
                str.contains("\\") ||
                str.contains("\r") ||
                str.contains("\n"))) {
            StringBuilder sb = new StringBuilder();
            for (char c : str.toCharArray()) {
                if (c == '\'') {
                    sb.append("\\'");
                } else if (c == '"') {
                    sb.append("\\\"");
                } else if (c == '\\') {
                    sb.append("\\\\");
                } else if (c == '\r') {
                    sb.append("\\r");
                } else if (c == '\n') {
                    sb.append("\\n");
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        return str;
    }

    /**
     * 参数化，json
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForJson(MysqlColumn column, Object value) {
        if (value == null) {
            return null;
        }
        if (column.supportGeometry()) {
            return "ST_GeomFromText('" + value + "')";
        }
        if (column.isDateType()) {
            Date date = (Date) value;
            return DateHelper.formatDate(date);
        }
        if (column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
            }
        }
        if (column.supportJson()) {
            return value.toString();
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        if (column.supportEnum()) {
            return value.toString();
        }
        if (column.supportString()) {
            return escapeQuotes((String) value);
        }
        if (column.supportInteger() || column.supportDigits()) {
            return value;
        }
        return value.toString();
    }

    /**
     * 参数化，xml
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForXml(MysqlColumn column, Object value) {
        if (value == null) {
            return null;
        }
        if (column.supportGeometry()) {
            return "ST_GeomFromText('" + value + "')";
        }
        if (column.isDateType()) {
            Date date = (Date) value;
            return DateHelper.formatDate(date);
        }
        if (column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
            }
        }
        if (column.supportJson()) {
            return value.toString();
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        if (column.supportEnum()) {
            return value.toString();
        }
        if (column.supportString()) {
            return escapeQuotes((String) value);
        }
        if (column.supportInteger() || column.supportDigits()) {
            return value;
        }
        return value.toString();
    }

    /**
     * 参数化，csv
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForCsv(MysqlColumn column, Object value) {
        if (value == null) {
            return "";
        }
        if (column.supportGeometry()) {
            return "\"ST_GeomFromText('" + value + "')\"";
        }
        if (column.isDateType()) {
            Date date = (Date) value;
            return "\"" + DateHelper.formatDate(date) + "\"";
        }
        if (column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return "\"" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "\"";
            }
            if (value instanceof Date date) {
                return "\"" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "\"";
            }
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "\"b'" + TextUtil.byteToBitStr(bytes) + "'\"";
        }
        if (column.supportString()) {
            return "\"" + escapeQuotes((String) value) + "\"";
        }
        return "\"" + value + "\"";
    }

    /**
     * 参数化，sql
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForSql(MysqlColumn column, Object value) {
        if (value == null) {
            return "NULL";
        }
        if (column.supportGeometry()) {
            return "ST_GeomFromText('" + value + "')";
        }
        if (column.isDateType()) {
            Date date = (Date) value;
            return "'" + DateHelper.formatDate(date) + "'";
        }
        if (column.supportTimestamp()) {
            return "'" + value + "'";
        }
        if (column.supportJson()) {
            return "'" + value + "'";
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "NULL";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBit()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "NULL";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        if (column.supportEnum()) {
            return "'" + value + "'";
        }
        if (column.supportString()) {
            String str = escapeQuotes((String) value);
            return "'" + str + "'";
        }
        return value;
    }

    /**
     * 参数化，html
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForHtml(MysqlColumn column, Object value) {
        if (value == null) {
            return "";
        }
        if (column.supportGeometry()) {
            return "ST_GeomFromText('" + value + "')";
        }
        if (column.isDateType()) {
            Date date = (Date) value;
            return DateHelper.formatDate(date);
        }
        if (column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, "d/M/yyyy HH:mm:ss");
            }
        }
        if (column.supportJson()) {
            return value.toString();
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        if (column.supportEnum()) {
            return value.toString();
        }
        if (column.supportString()) {
            return escapeQuotes((String) value);
        }
        if (column.supportInteger() || column.supportDigits()) {
            return value;
        }
        return value.toString();
    }

    /**
     * 参数化，xls
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForXls(MysqlColumn column, Object value) {
        if (value == null) {
            return null;
        }
        if (column.supportGeometry()) {
            return "ST_GeomFromText('" + value + "')";
        }
        if (column.isDateType()) {
            if (value instanceof Date date) {
                return DateHelper.formatDate(date);
            }
        }
        if (column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return DateUtil.format(date, "yyyy/M/dd HH:mm:ss");
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, "yyyy/M/dd HH:mm:ss");
            }
        }
        if (column.supportJson()) {
            return value.toString();
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        if (column.supportEnum()) {
            return value.toString();
        }
        if (column.supportString()) {
            return escapeQuotes((String) value);
        }
        return value;
    }

    /**
     * 转换为插入sql
     *
     * @param columns       字段列表
     * @param record        记录
     * @param includeFields 包含字段
     * @return 插入sql
     */
    public static String toInsertSql(MysqlColumns columns, MysqlRecord record, boolean includeFields) {
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
    public static List<String> toInsertSql(MysqlColumns columns, List<MysqlRecord> records) {
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
    public static List<String> toInsertSql(MysqlColumns columns, List<MysqlRecord> records, boolean includeFields) {
        List<String> list = new ArrayList<>();
        String tableName = columns.tableName();
        List<MysqlColumn> columnList = columns.sortOfPosition();
        final String sqlBase = "INSERT INTO " + ShellMysqlUtil.wrap(tableName, DBDialect.MYSQL);
        for (MysqlRecord record : records) {
            StringBuilder sql = new StringBuilder(sqlBase);
            if (includeFields) {
                sql.append("(");
                for (MysqlColumn dbColumn : columnList) {
                    sql.append(ShellMysqlUtil.wrap(dbColumn.getName(), DBDialect.MYSQL)).append(", ");
                }
                if (sql.toString().endsWith(", ")) {
                    sql.delete(sql.length() - 2, sql.length());
                }
                sql.append(")");
            }
            sql.append(" VALUES (");
            for (MysqlColumn dbColumn : columnList) {
                Object value = record.getValue(dbColumn.getName());
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
    public static String toUpdateSql(MysqlColumns columns, MysqlRecord record) {
        MysqlRecordPrimaryKey primaryKey = ShellMysqlUtil.initPrimaryKey(columns, record);
        String tableName = columns.tableName();
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ")
                .append(ShellMysqlUtil.wrap(columns.dbName(), tableName, DBDialect.MYSQL))
                .append(" SET ");
        for (MysqlColumn column : columns) {
            if (primaryKey != null && column == primaryKey.getColumn()) {
                continue;
            }
            Object value = record.getValue(column.getName());
            value = parameterizedForSql(column, value);
            builder.append(ShellMysqlUtil.wrap(column.getName(), DBDialect.MYSQL));
            builder.append(" = ");
            if (column.isGeometryType()) {
                builder.append(" ST_GeomFromText(").append(value).append(")");
            } else {
                builder.append(value);
            }
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append(" WHERE ");
        if (primaryKey == null) {
            // 参数
            boolean first = true;
            for (MysqlColumn column : columns) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" AND ");
                }
                builder.append(ShellMysqlUtil.wrap(column.getName(), DBDialect.MYSQL));
                builder.append(" = ");
                Object value = record.getValue(column.getName());
                value = parameterizedForSql(column, value);
                builder.append(value);
            }
            builder.append(" LIMIT 1");
        } else {
            builder.append(ShellMysqlUtil.wrap(primaryKey.getColumnName(), DBDialect.MYSQL));
            builder.append(" = ");
            Object value = parameterizedForSql(primaryKey.getColumn(), primaryKey.getData());
            builder.append(value);
        }
        builder.append(";");
        return builder.toString();
    }

    /**
     * 转换为插入json
     *
     * @param columns 字段列表
     * @param records 记录
     * @return 插入json
     */
    public static List<Map<String, Object>> toInsertJson(MysqlColumns columns, List<MysqlRecord> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<MysqlColumn> columnList = columns.sortOfPosition();
        for (MysqlRecord record : records) {
            Map<String, Object> object = new HashMap<>();
            for (MysqlColumn dbColumn : columnList) {
                Object value = record.getValue(dbColumn.getName());
                value = parameterizedForJson(dbColumn, value);
                object.put(dbColumn.getName(), value);
            }
            list.add(object);
        }
        return list;
    }

    /**
     * 转换为插入xml
     *
     * @param columns 字段列表
     * @param records 记录
     * @return 插入xml
     */
    public static List<Map<String, Object>> toInsertXml(MysqlColumns columns, List<MysqlRecord> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<MysqlColumn> columnList = columns.sortOfPosition();
        for (MysqlRecord record : records) {
            Map<String, Object> object = new HashMap<>();
            for (MysqlColumn dbColumn : columnList) {
                Object value = record.getValue(dbColumn.getName());
                value = parameterizedForXml(dbColumn, value);
                object.put(dbColumn.getName(), value);
            }
            list.add(object);
        }
        return list;
    }

    /**
     * 转换为插入csv
     *
     * @param columns 字段列表
     * @param records 记录
     * @return 插入csv
     */
    public static List<List<Object>> toInsertCsv(MysqlColumns columns, List<MysqlRecord> records) {
        List<List<Object>> list = new ArrayList<>();
        List<MysqlColumn> columnList = columns.sortOfPosition();
        for (MysqlRecord record : records) {
            List<Object> object = new ArrayList<>();
            for (MysqlColumn dbColumn : columnList) {
                Object value = record.getValue(dbColumn.getName());
                value = parameterizedForCsv(dbColumn, value);
                object.add(value);
            }
            list.add(object);
        }
        return list;
    }

    /**
     * 转换为插入html
     *
     * @param columns 字段列表
     * @param records 记录
     * @return 插入html
     */
    public static List<List<Object>> toInsertHtml(MysqlColumns columns, List<MysqlRecord> records) {
        List<List<Object>> list = new ArrayList<>();
        List<MysqlColumn> columnList = columns.sortOfPosition();
        for (MysqlRecord record : records) {
            List<Object> object = new ArrayList<>();
            for (MysqlColumn dbColumn : columnList) {
                Object value = record.getValue(dbColumn.getName());
                value = parameterizedForHtml(dbColumn, value);
                object.add(value);
            }
            list.add(object);
        }
        return list;
    }

    /**
     * 转换为插入xls
     *
     * @param columns 字段列表
     * @param records 记录
     * @return 插入xls
     */
    public static List<List<Object>> toInsertXls(MysqlColumns columns, List<MysqlRecord> records) {
        List<List<Object>> list = new ArrayList<>();
        List<MysqlColumn> columnList = columns.sortOfPosition();
        for (MysqlRecord record : records) {
            List<Object> object = new ArrayList<>();
            for (MysqlColumn dbColumn : columnList) {
                Object value = record.getValue(dbColumn.getName());
                value = parameterizedForXls(dbColumn, value);
                object.add(value);
            }
            list.add(object);
        }
        return list;
    }
}
