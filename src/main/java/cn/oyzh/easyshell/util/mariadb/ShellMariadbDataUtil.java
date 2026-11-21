package cn.oyzh.easyshell.util.mariadb;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordPrimaryKey;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBDataUtil;
import cn.oyzh.fx.db.util.DBUtil;
import com.alibaba.fastjson2.JSON;
import org.mariadb.jdbc.type.Geometry;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDataUtil {

    //    /**
    //     * 转义符号
    //     *
    //     * @param str 内容
    //     * @return 转义后的内容
    //     */
    //    public static String escapeQuotes(String str) {
    //        if (str == null) {
    //            return null;
    //        }
    //        if (str.contains("'")
    //                || str.contains("\"")
    //                || str.contains("\\")
    //                || str.contains("\r")
    //                || str.contains("\n")) {
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
    //    public static Object parameterizedForJson(MariadbColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
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
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return value.toString();
    //        }
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
    //    public static Object parameterizedForXml(MariadbColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
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
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return value.toString();
    //        }
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
    //    public static Object parameterizedForCsv(MariadbColumn column, Object value) {
    //        if (value == null) {
    //            return "";
    //        }
    //        if (column.supportGeometry()) {
    //            return "\"ST_GeomFromText('" + value + "')\"";
    //        }
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
    //    public static Object parameterizedForSql(MariadbColumn column, Object value) {
    //        if (value == null) {
    //            return "NULL";
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
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
    //        if (column.supportBit()) {
    //            if (value instanceof Boolean b) {
    //                return b ? "1" : "0";
    //            }
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "NULL";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return "'" + value + "'";
    //        }
    //        if (column.supportString()) {
    //            String str = TextUtil.escape((String) value);
    //            return "\"" + str + "\"";
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
    //    public static Object parameterizedForHtml(MariadbColumn column, Object value) {
    //        if (value == null) {
    //            return "";
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
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
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return value.toString();
    //        }
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
    //    public static Object parameterizedForXls(MariadbColumn column, Object value) {
    //        if (value == null) {
    //            return null;
    //        }
    //        if (column.supportGeometry()) {
    //            return "ST_GeomFromText('" + value + "')";
    //        }
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
    //        if (column.supportBit()) {
    //            byte[] bytes = (byte[]) value;
    //            if (bytes.length == 0) {
    //                return "";
    //            }
    //            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
    //        }
    //        if (column.supportEnum()) {
    //            return value.toString();
    //        }
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
    public static String toInsertSql(MariadbColumns columns, MariadbRecord record, boolean includeFields) {
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
    public static List<String> toInsertSql(MariadbColumns columns, List<MariadbRecord> records) {
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
    public static List<String> toInsertSql(MariadbColumns columns, List<MariadbRecord> records, boolean includeFields) {
        List<String> list = new ArrayList<>();
        String tableName = columns.tableName();
        List<MariadbColumn> columnList = columns.sortOfPosition();
        boolean includeColumnNames = includeFields
                || columnList.stream().anyMatch(MariadbColumn::isGenerated);
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.MARIADB) + " ";
        for (MariadbRecord record : records) {
            StringBuilder sql = new StringBuilder(sqlBase);
            if (includeColumnNames) {
                sql.append("(");
                for (MariadbColumn dbColumn : columnList) {
                    if (dbColumn.isGenerated()) {
                        continue;
                    }
                    sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.MARIADB)).append(", ");
                }
                if (sql.toString().endsWith(", ")) {
                    sql.delete(sql.length() - 2, sql.length());
                }
                sql.append(")");
            }
            sql.append(" VALUES (");
            for (MariadbColumn dbColumn : columnList) {
                if (dbColumn.isGenerated()) {
                    continue;
                }
                Object value = record.getValue(dbColumn.getName());
                value = valueStandardization(dbColumn, value);
                value = DBDataUtil.parameterizedForSql(dbColumn, value, DBDialect.MARIADB);
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
    public static String toUpdateSql(MariadbColumns columns, MariadbRecord record) {
        MariadbRecordPrimaryKey primaryKey = ShellMariadbUtil.initPrimaryKey(columns, record);
        String tableName = columns.tableName();
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ")
                .append(DBUtil.wrap(columns.dbName(), tableName, DBDialect.MARIADB))
                .append(" SET ");
        boolean doSet = false;
        for (MariadbColumn column : columns) {
            if (column.isGenerated()) {
                continue;
            }
            if (primaryKey != null && column == primaryKey.getColumn()) {
                continue;
            }
            Object value = record.getValue(column.getName());
            value = valueStandardization(column, value);
            value = DBDataUtil.parameterizedForSql(column, value, DBDialect.MARIADB);
            builder.append(DBUtil.wrap(column.getName(), DBDialect.MARIADB));
            builder.append(" = ");
            if (column.isGeometryType()) {
                builder.append(" ST_GeomFromText(").append(value).append(")");
            } else {
                builder.append(value);
            }
            builder.append(", ");
            doSet = true;
        }
        if (doSet) {
            builder.deleteCharAt(builder.length() - 2);
        }
        builder.append(" WHERE ");
        if (primaryKey == null) {
            // 参数
            boolean first = true;
            for (MariadbColumn column : columns) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" AND ");
                }
                builder.append(DBUtil.wrap(column.getName(), DBDialect.MARIADB));
                builder.append(" = ");
                Object value = record.getValue(column.getName());
                value = valueStandardization(column, value);
                value = DBDataUtil.parameterizedForSql(column, value, DBDialect.MARIADB);
                builder.append(value);
            }
            builder.append(" LIMIT 1");
        } else {
            builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.MARIADB));
            builder.append(" = ");
            Object value = valueStandardization(primaryKey.getColumn(), primaryKey.getData());
            value = DBDataUtil.parameterizedForSql(primaryKey.getColumn(), value, DBDialect.MARIADB);
            builder.append(value);
        }
        builder.append(";");
        return builder.toString();
    }

    /**
     * 值规整化
     *
     * @param column 字段
     * @param value  值
     * @return 规整后的值
     */
    public static Object valueStandardization(MariadbColumn column, Object value) {
        if (column != null && column.supportJson()
                && value != null && !(value instanceof CharSequence)) {
            value = JSON.toJSONString(value);
        }
        value = valueStandardization(value);
        if (column != null && column.supportBit() && value instanceof Boolean bool) {
            return new byte[]{(byte) (bool ? 1 : 0)};
        }
        return value;
    }

    /**
     * 值规整化，处理MariaDB驱动返回的LOB、XML、几何等扩展类型。
     *
     * @param value 值
     * @return 规整后的值
     */
    public static Object valueStandardization(Object value) {
        if (value == null) {
            return null;
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
        if (value instanceof Geometry geometry) {
            return geometry.toString();
        }
        if (value instanceof Array || value instanceof Struct || value instanceof Ref || value instanceof RowId) {
            return value.toString();
        }
        return value;
    }

    //    /**
    //     * 转换为插入json
    //     *
    //     * @param columns 字段列表
    //     * @param records 记录
    //     * @return 插入json
    //     */
    //    public static List<Map<String, Object>> toInsertJson(MariadbColumns columns, List<MariadbRecord> records) {
    //        List<Map<String, Object>> list = new ArrayList<>();
    //        List<MariadbColumn> columnList = columns.sortOfPosition();
    //        for (MariadbRecord record : records) {
    //            Map<String, Object> object = new HashMap<>();
    //            for (MariadbColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = DBDataUtil.parameterizedForJson(dbColumn, value);
    //                object.put(dbColumn.getName(), value);
    //            }
    //            list.add(object);
    //        }
    //        return list;
    //    }
    //
    //    /**
    //     * 转换为插入xml
    //     *
    //     * @param columns 字段列表
    //     * @param records 记录
    //     * @return 插入xml
    //     */
    //    public static List<Map<String, Object>> toInsertXml(MariadbColumns columns, List<MariadbRecord> records) {
    //        List<Map<String, Object>> list = new ArrayList<>();
    //        List<MariadbColumn> columnList = columns.sortOfPosition();
    //        for (MariadbRecord record : records) {
    //            Map<String, Object> object = new HashMap<>();
    //            for (MariadbColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = DBDataUtil.parameterizedForXml(dbColumn, value);
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
    //    public static List<List<Object>> toInsertCsv(MariadbColumns columns, List<MariadbRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<MariadbColumn> columnList = columns.sortOfPosition();
    //        for (MariadbRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (MariadbColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = DBDataUtil.parameterizedForCsv(dbColumn, value);
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
    //    public static List<List<Object>> toInsertHtml(MariadbColumns columns, List<MariadbRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<MariadbColumn> columnList = columns.sortOfPosition();
    //        for (MariadbRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (MariadbColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = DBDataUtil.parameterizedForHtml(dbColumn, value);
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
    //    public static List<List<Object>> toInsertXls(MariadbColumns columns, List<MariadbRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<MariadbColumn> columnList = columns.sortOfPosition();
    //        for (MariadbRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (MariadbColumn dbColumn : columnList) {
    //                Object value = record.getValue(dbColumn.getName());
    //                value = DBDataUtil.parameterizedForXls(dbColumn, value);
    //                object.add(value);
    //            }
    //            list.add(object);
    //        }
    //        return list;
    //    }
}
