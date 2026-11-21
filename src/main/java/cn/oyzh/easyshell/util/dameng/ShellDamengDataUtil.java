package cn.oyzh.easyshell.util.dameng;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBDataUtil;
import cn.oyzh.fx.db.util.DBUtil;
import dm.jdbc.driver.DmdbArray;
import dm.jdbc.driver.DmdbBFile;
import dm.jdbc.driver.DmdbBlob;
import dm.jdbc.driver.DmdbClob;
import dm.jdbc.driver.DmdbIntervalDT;
import dm.jdbc.driver.DmdbIntervalYM;
import dm.jdbc.driver.DmdbNumeric;
import dm.jdbc.driver.DmdbRowId;
import dm.jdbc.driver.DmdbSQLXML;
import dm.jdbc.driver.DmdbStruct;
import dm.jdbc.driver.DmdbType;
import dm.jdbc.internal.desc.Column;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengDataUtil {

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
    //    public static Object parameterizedForJson(DamengColumn column, Object value) {
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
    //    public static Object parameterizedForXml(DamengColumn column, Object value) {
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
    //    public static Object parameterizedForCsv(DamengColumn column, Object value) {
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
    //    public static Object parameterizedForSql(DamengColumn column, Object value) {
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
    //    public static Object parameterizedForHtml(DamengColumn column, Object value) {
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
    //    public static Object parameterizedForXls(DamengColumn column, Object value) {
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
        boolean includeColumnNames = includeFields
                || columnList.stream().anyMatch(DamengColumn::isGenerated);
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.DAMENG);
        for (DamengRecord record : records) {
            StringBuilder sql = new StringBuilder(sqlBase);
            if (includeColumnNames) {
                sql.append(" (");
                for (DamengColumn dbColumn : columnList) {
                    if (dbColumn.isGenerated()) {
                        continue;
                    }
                    sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.DAMENG)).append(", ");
                }
                if (sql.toString().endsWith(", ")) {
                    sql.delete(sql.length() - 2, sql.length());
                }
                sql.append(")");
            }
            sql.append(" VALUES (");
            for (DamengColumn dbColumn : columnList) {
                if (dbColumn.isGenerated()) {
                    continue;
                }
                Object value = record.getValue(dbColumn.getName());
                String complexLiteral = complexLiteral(dbColumn, value);
                if (complexLiteral != null) {
                    sql.append(complexLiteral).append(", ");
                    continue;
                }
                value = valueStandardization(dbColumn, value);
                value = DBDataUtil.parameterizedForSql(dbColumn, value, DBDialect.DAMENG);
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
        boolean doSet = false;
        for (DamengColumn column : columns) {
            if (column.isGenerated()) {
                continue;
            }
            if (primaryKey != null && column == primaryKey.getColumn()) {
                continue;
            }
            Object value = record.getValue(column.getName());
            String complexLiteral = complexLiteral(column, value);
            if (complexLiteral != null) {
                builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
                builder.append(" = ").append(complexLiteral).append(", ");
                doSet = true;
                continue;
            }
            value = valueStandardization(column, value);
            value = DBDataUtil.parameterizedForSql(column, value, DBDialect.DAMENG);
            builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
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
            for (DamengColumn column : columns) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" AND ");
                }
                builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG));
                builder.append(" = ");
                Object value = record.getValue(column.getName());
                String complexLiteral = complexLiteral(column, value);
                if (complexLiteral != null) {
                    builder.append(complexLiteral);
                    continue;
                }
                value = valueStandardization(column, value);
                value = DBDataUtil.parameterizedForSql(column, value, DBDialect.DAMENG);
                builder.append(value);
            }
            builder.append(" LIMIT 1");
        } else {
            builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.DAMENG));
            builder.append(" = ");
            Object value = valueStandardization(primaryKey.getColumn(), primaryKey.getData());
            value = DBDataUtil.parameterizedForSql(primaryKey.getColumn(), value, DBDialect.DAMENG);
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
    //    public static List<Map<String, Object>> toInsertJson(DamengColumns columns, List<DamengRecord> records) {
    //        List<Map<String, Object>> list = new ArrayList<>();
    //        List<DamengColumn> columnList = columns.sortOfPosition();
    //        for (DamengRecord record : records) {
    //            Map<String, Object> object = new HashMap<>();
    //            for (DamengColumn dbColumn : columnList) {
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
    //    public static List<Map<String, Object>> toInsertXml(DamengColumns columns, List<DamengRecord> records) {
    //        List<Map<String, Object>> list = new ArrayList<>();
    //        List<DamengColumn> columnList = columns.sortOfPosition();
    //        for (DamengRecord record : records) {
    //            Map<String, Object> object = new HashMap<>();
    //            for (DamengColumn dbColumn : columnList) {
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
    //    public static List<List<Object>> toInsertCsv(DamengColumns columns, List<DamengRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<DamengColumn> columnList = columns.sortOfPosition();
    //        for (DamengRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (DamengColumn dbColumn : columnList) {
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
    //    public static List<List<Object>> toInsertHtml(DamengColumns columns, List<DamengRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<DamengColumn> columnList = columns.sortOfPosition();
    //        for (DamengRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (DamengColumn dbColumn : columnList) {
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
    //    public static List<List<Object>> toInsertXls(DamengColumns columns, List<DamengRecord> records) {
    //        List<List<Object>> list = new ArrayList<>();
    //        List<DamengColumn> columnList = columns.sortOfPosition();
    //        for (DamengRecord record : records) {
    //            List<Object> object = new ArrayList<>();
    //            for (DamengColumn dbColumn : columnList) {
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
        if (value == null) {
            return null;
        }
        if (value instanceof DmdbClob clob) {
            return clob.data == null ? readClob(clob) : clob.data;
        }
        if (value instanceof DmdbBlob blob) {
            return blob.data == null ? readBlob(blob) : blob.data;
        }
        if (value instanceof DmdbIntervalYM || value instanceof DmdbIntervalDT) {
            return value.toString();
        }
        if (value instanceof DmdbNumeric numeric) {
            return numeric.toBigDecimal(false);
        }
        if (value instanceof DmdbBFile bFile) {
            return bFile.getFullName();
        }
        if (value instanceof DmdbRowId rowId) {
            return rowId.toString();
        }
        if (value instanceof DmdbSQLXML sqlXml) {
            try {
                return sqlXml.getString();
            } catch (SQLException ex) {
                return sqlXml;
            }
        }
        if (value instanceof DmdbArray array) {
            return arrayLiteral(array);
        }
        if (value instanceof DmdbStruct struct) {
            return structLiteral(struct);
        }
        if (value instanceof Ref) {
            return value.toString();
        }
        if (value instanceof Clob clob) {
            return readClob(clob);
        }
        if (value instanceof Blob blob) {
            return readBlob(blob);
        }
        if (value instanceof java.sql.SQLXML sqlXml) {
            try {
                return sqlXml.getString();
            } catch (SQLException ex) {
                return sqlXml;
            }
        }
        if (value instanceof java.sql.Array || value instanceof java.sql.Struct || value instanceof java.sql.RowId) {
            return value.toString();
        }
        return value;
    }

    /**
     * 值规整化
     *
     * @param column 字段
     * @param value  值
     * @return 规整后的值
     */
    public static Object valueStandardization(DamengColumn column, Object value) {
        value = valueStandardization(value);
        if (column != null && column.supportBit() && value instanceof Boolean bool) {
            return new byte[]{(byte) (bool ? 1 : 0)};
        }
        return value;
    }

    /**
     * 判断值是否可以直接作为达梦用户自定义类型构造表达式。
     *
     * @param column 字段
     * @param value  值
     * @return 结果
     */
    public static boolean isComplexLiteral(DamengColumn column, Object value) {
        return complexLiteral(column, value) != null;
    }

    /**
     * 获取可直接嵌入 SQL 的达梦复杂类型构造表达式。
     *
     * @param column 字段
     * @param value  值
     * @return 构造表达式，不匹配时返回 null
     */
    public static String complexLiteral(DamengColumn column, Object value) {
        if (column == null || !column.isUserDefinedType()
                || !(value instanceof CharSequence sequence)) {
            return null;
        }
        String text = sequence.toString().trim();
        String type = column.getType() == null ? "" : column.getType().toUpperCase();
        String upper = text.toUpperCase();
        if (type.isEmpty() || !text.endsWith(")")
                || (!upper.startsWith(type + "(") && !upper.contains("." + type + "("))) {
            return null;
        }
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (quote != 0) {
                if (current == quote) {
                    if (i + 1 < text.length() && text.charAt(i + 1) == quote) {
                        i++;
                    } else {
                        quote = 0;
                    }
                }
            } else if (current == '\'' || current == '"') {
                quote = current;
            } else if (current == '(') {
                depth++;
            } else if (current == ')' && --depth < 0) {
                return null;
            }
        }
        if (depth != 0 || quote != 0) {
            return null;
        }
        int qualifiedType = upper.indexOf("." + type + "(");
        if (qualifiedType >= 0) {
            text = text.substring(qualifiedType + 1);
        }
        return text;
    }

    private static String structLiteral(DmdbStruct struct) {
        try {
            Object[] attributes = struct.getAttributes();
            StringBuilder builder = new StringBuilder(struct.getSQLTypeName()).append("(");
            for (int i = 0; i < attributes.length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                String typeName = null;
                Column typeColumn = null;
                if (struct.m_strctDesc != null
                        && struct.m_strctDesc.m_fieldsObj != null
                        && i < struct.m_strctDesc.m_fieldsObj.length
                        && struct.m_strctDesc.m_fieldsObj[i] != null
                        && struct.m_strctDesc.m_fieldsObj[i].column != null) {
                    typeColumn = struct.m_strctDesc.m_fieldsObj[i].column;
                    typeName = typeColumn.typeName;
                }
                builder.append(complexAttributeLiteral(attributes[i], typeName, typeColumn));
            }
            return builder.append(")").toString();
        } catch (SQLException ex) {
            return struct.toString();
        }
    }

    private static String arrayLiteral(DmdbArray array) {
        try {
            Object values = array.getArray();
            String itemTypeName = array.m_arrDesc == null || array.m_arrDesc.m_arrObj == null
                    || array.m_arrDesc.m_arrObj.column == null
                    ? null : array.m_arrDesc.m_arrObj.column.typeName;
            Column itemType = array.m_arrDesc == null || array.m_arrDesc.m_arrObj == null
                    ? null : array.m_arrDesc.m_arrObj.column;
            String arrayTypeName = array.m_arrDesc == null
                    ? array.getBaseTypeName() : array.m_arrDesc.getFulName();
            StringBuilder builder = new StringBuilder(arrayTypeName).append("(");
            int length = values == null ? 0 : java.lang.reflect.Array.getLength(values);
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(complexAttributeLiteral(
                        java.lang.reflect.Array.get(values, i), itemTypeName, itemType));
            }
            return builder.append(")").toString();
        } catch (SQLException ex) {
            return array.toString();
        }
    }

    private static String complexAttributeLiteral(Object value, String typeName, Column column) {
        if (value == null) {
            return "NULL";
        }
        if ((column != null && (DmdbType.isCharacter(column.type)
                || column.type == DmdbType.CLOB))
                || (typeName != null && StringUtil.containsAnyIgnoreCase(typeName,
                "CHAR", "CLOB", "TEXT"))) {
            return DBUtil.wrapString(value.toString(), DBDialect.DAMENG);
        }
        return value.toString();
    }

    private static String readClob(Clob clob) {
        try {
            return clob.getSubString(1, Math.toIntExact(clob.length()));
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to read Dameng CLOB value", ex);
        }
    }

    private static byte[] readBlob(Blob blob) {
        try {
            return blob.getBytes(1, Math.toIntExact(blob.length()));
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to read Dameng BLOB value", ex);
        }
    }
}
