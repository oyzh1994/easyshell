package cn.oyzh.easyshell.util.dameng;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.easyshell.util.dameng.ShellDamengUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import dm.jdbc.driver.DmdbBlob;
import dm.jdbc.driver.DmdbClob;

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
public class DamengDataUtil {

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

    /**
     * 参数化，json
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForJson(DamengColumn column, Object value) {
        if (value == null) {
            return null;
        }
        value = valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
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
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        //        if (column.supportEnum()) {
        //            return value.toString();
        //        }
        if (column.supportString()) {
            return TextUtil.escape((String) value);
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
    public static Object parameterizedForXml(DamengColumn column, Object value) {
        if (value == null) {
            return null;
        }
        value = valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
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
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        //        if (column.supportEnum()) {
        //            return value.toString();
        //        }
        if (column.supportString()) {
            return TextUtil.escape((String) value);
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
    public static Object parameterizedForCsv(DamengColumn column, Object value) {
        if (value == null) {
            return "";
        }
        value = valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "\"ST_GeomFromText('" + value + "')\"";
        //        }
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
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "\"b'" + TextUtil.byteToBitStr(bytes) + "'\"";
        }
        if (column.supportString()) {
            return "\"" + TextUtil.escape((String) value) + "\"";
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
    public static Object parameterizedForSql(DamengColumn column, Object value) {
        if (value == null) {
            return "NULL";
        }
        value = valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
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
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "NULL";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        //        if (column.supportEnum()) {
        //            return "'" + value + "'";
        //        }
        if (column.supportString()) {
            String str = TextUtil.escape((String) value);
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
    public static Object parameterizedForHtml(DamengColumn column, Object value) {
        if (value == null) {
            return "";
        }
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
        value = valueStandardization(value);
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
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        //        if (column.supportEnum()) {
        //            return value.toString();
        //        }
        if (column.supportString()) {
            return TextUtil.escape((String) value);
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
    public static Object parameterizedForXls(DamengColumn column, Object value) {
        if (value == null) {
            return null;
        }
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
        value = valueStandardization(value);
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
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        //        if (column.supportEnum()) {
        //            return value.toString();
        //        }
        if (column.supportString()) {
            return TextUtil.escape((String) value);
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
    public static String toInsertSql(DamengColumns columns, DamengRecord record, boolean includeFields) {
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
    public static List<String> toInsertSql(DamengColumns columns, List<DamengRecord> records) {
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
    public static List<String> toInsertSql(DamengColumns columns, List<DamengRecord> records, boolean includeFields) {
        List<String> list = new ArrayList<>();
        String tableName = columns.tableName();
        List<DamengColumn> columnList = columns.sortOfPosition();
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.DAMENG);
        for (DamengRecord record : records) {
            StringBuilder sql = new StringBuilder(sqlBase);
            if (includeFields) {
                sql.append("(");
                for (DamengColumn dbColumn : columnList) {
                    sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.DAMENG)).append(", ");
                }
                if (sql.toString().endsWith(", ")) {
                    sql.delete(sql.length() - 2, sql.length());
                }
                sql.append(")");
            }
            sql.append(" VALUES (");
            for (DamengColumn dbColumn : columnList) {
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
    public static String toUpdateSql(DamengColumns columns, DamengRecord record) {
        DamengRecordPrimaryKey primaryKey = ShellDamengUtil.initPrimaryKey(columns, record);
        String tableName = columns.tableName();
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ")
                .append(DBUtil.wrap(columns.schema(), tableName, DBDialect.DAMENG))
                .append(" SET ");
        for (DamengColumn column : columns) {
            if (primaryKey != null && column == primaryKey.getColumn()) {
                continue;
            }
            Object value = record.getValue(column.getName());
            value = parameterizedForSql(column, value);
            builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
            builder.append(" = ");
            builder.append(value);
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append(" WHERE ");
        if (primaryKey == null) {
            // 参数
            boolean first = true;
            for (DamengColumn column : columns) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" AND ");
                }
                builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
                builder.append(" = ");
                Object value = record.getValue(column.getName());
                value = parameterizedForSql(column, value);
                builder.append(value);
            }
            builder.append(" LIMIT 1");
        } else {
            builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.DAMENG));
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
    public static List<Map<String, Object>> toInsertJson(DamengColumns columns, List<DamengRecord> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<DamengColumn> columnList = columns.sortOfPosition();
        for (DamengRecord record : records) {
            Map<String, Object> object = new HashMap<>();
            for (DamengColumn dbColumn : columnList) {
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
    public static List<Map<String, Object>> toInsertXml(DamengColumns columns, List<DamengRecord> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<DamengColumn> columnList = columns.sortOfPosition();
        for (DamengRecord record : records) {
            Map<String, Object> object = new HashMap<>();
            for (DamengColumn dbColumn : columnList) {
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
    public static List<List<Object>> toInsertCsv(DamengColumns columns, List<DamengRecord> records) {
        List<List<Object>> list = new ArrayList<>();
        List<DamengColumn> columnList = columns.sortOfPosition();
        for (DamengRecord record : records) {
            List<Object> object = new ArrayList<>();
            for (DamengColumn dbColumn : columnList) {
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
    public static List<List<Object>> toInsertHtml(DamengColumns columns, List<DamengRecord> records) {
        List<List<Object>> list = new ArrayList<>();
        List<DamengColumn> columnList = columns.sortOfPosition();
        for (DamengRecord record : records) {
            List<Object> object = new ArrayList<>();
            for (DamengColumn dbColumn : columnList) {
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
    public static List<List<Object>> toInsertXls(DamengColumns columns, List<DamengRecord> records) {
        List<List<Object>> list = new ArrayList<>();
        List<DamengColumn> columnList = columns.sortOfPosition();
        for (DamengRecord record : records) {
            List<Object> object = new ArrayList<>();
            for (DamengColumn dbColumn : columnList) {
                Object value = record.getValue(dbColumn.getName());
                value = parameterizedForXls(dbColumn, value);
                object.add(value);
            }
            list.add(object);
        }
        return list;
    }

    /**
     * 值规整化
     *
     * @param value 值
     * @return 规整后的值
     */
    public static Object valueStandardization(Object value) {
        if (value instanceof DmdbClob clob) {
            return clob.data;
        }
        if (value instanceof DmdbBlob blob) {
            return blob.data;
        }
        return value;
    }
}
