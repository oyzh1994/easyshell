package cn.oyzh.easyshell.util.sqlserver;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordPrimaryKey;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * SqlServer数据工具类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerDataUtil {

    /**
     * 转义单引号，SqlServer使用两个单引号进行转义
     *
     * @param str 内容
     * @return 转义后的内容
     */
    public static String escapeQuotes(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("'", "''");
    }

    /**
     * 转换为插入sql
     *
     * @param columns       字段列表
     * @param record        记录
     * @param includeFields 包含字段
     * @return 插入sql
     */
    public static String toInsertSql(SqlServerColumns columns, SqlServerRecord record, boolean includeFields) {
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
    public static List<String> toInsertSql(SqlServerColumns columns, List<SqlServerRecord> records) {
        return toInsertSql(columns, records, false);
    }

    /**
     * 转换为插入sql，生成形如 INSERT INTO "schema"."table" ("c1",...) VALUES (...); 的语句
     *
     * @param columns       字段列表
     * @param records       记录
     * @param includeFields 包含字段
     * @return 插入sql
     */
    public static List<String> toInsertSql(SqlServerColumns columns, List<SqlServerRecord> records, boolean includeFields) {
        List<String> list = new ArrayList<>();
        List<SqlServerColumn> columnList = columns.sortOfPosition();
        final String sqlBase = "INSERT INTO " + tableFullName(columns);
        for (SqlServerRecord record : records) {
            StringBuilder sql = new StringBuilder(sqlBase);
            if (includeFields) {
                sql.append(" (");
                for (SqlServerColumn dbColumn : columnList) {
                    sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.SQLSERVER)).append(", ");
                }
                if (sql.toString().endsWith(", ")) {
                    sql.delete(sql.length() - 2, sql.length());
                }
                sql.append(")");
            }
            sql.append(" VALUES (");
            for (SqlServerColumn dbColumn : columnList) {
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
    public static String toUpdateSql(SqlServerColumns columns, SqlServerRecord record) {
        SqlServerRecordPrimaryKey primaryKey = ShellSqlServerUtil.initPrimaryKey(columns, record);
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ")
                .append(tableFullName(columns))
                .append(" SET ");
        boolean doSet = false;
        for (SqlServerColumn column : columns) {
            if (primaryKey != null && column == primaryKey.getColumn()) {
                continue;
            }
            Object value = record.getValue(column.getName());
            value = valueStandardization(value);
            value = parameterizedForSql(column, value);
            builder.append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER));
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
            for (SqlServerColumn column : columns) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" AND ");
                }
                builder.append(DBUtil.wrap(column.getName(), DBDialect.SQLSERVER));
                Object value = record.getValue(column.getName());
                value = valueStandardization(value);
                value = parameterizedForSql(column, value);
                // NULL值需要使用IS NULL，= NULL在SqlServer中不成立
                if ("NULL".equals(value)) {
                    builder.append(" IS NULL");
                } else {
                    builder.append(" = ").append(value);
                }
            }
            // SqlServer不支持UPDATE ... LIMIT，此处仅保留条件
            builder.append(";");
        } else {
            builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.SQLSERVER));
            builder.append(" = ");
            Object value = parameterizedForSql(primaryKey.getColumn(), primaryKey.getData());
            builder.append(value).append(";");
        }
        return builder.toString();
    }

    /**
     * 获取表全名，形如"schema"."table"
     *
     * @param columns 字段列表
     * @return 表全名
     */
    public static String tableFullName(SqlServerColumns columns) {
        String tableName = columns.tableName();
        String schema = columns.schema();
        if (StringUtil.isBlank(schema)) {
            return DBUtil.wrap(tableName, DBDialect.SQLSERVER);
        }
        return DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
    }

    /**
     * 参数化，sql
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterizedForSql(SqlServerColumn column, Object value) {
        if (value == null) {
            return "NULL";
        }
        value = valueStandardization(value);
        // SqlServer中空字符串不能转换为数值、布尔、日期等类型
        if (value instanceof CharSequence sequence && sequence.length() == 0
                && !column.supportString() && !column.supportText() && !column.supportJson()) {
            return "NULL";
        }
        // 几何类型，SqlServer内置几何类型可以直接使用文本字面量
        if (column.supportGeometry()) {
            return quote(value);
        }
        // SQL Server二进制类型使用0x十六进制字面量
        if (column.supportBinary()) {
            byte[] bytes;
            if (value instanceof byte[] data) {
                bytes = data;
            } else {
                bytes = value.toString().getBytes(StandardCharsets.UTF_8);
            }
            if (bytes.length == 0) {
                return "0x";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        // 布尔类型，使用TRUE/FALSE
        if (column.supportBoolean()) {
            String bool = formatBoolean(value);
            return bool == null ? quote(value) : bool;
        }
        // 位串类型，使用B'...'字面量
        if (column.supportBit()) {
            String str = toBitText(column, value);
            if (StringUtil.startWithAnyIgnoreCase(str, "B'", "X'")) {
                return str;
            }
            return "B'" + str + "'";
        }
        // 日期时间类型
        if (column.isDateType() || column.isTimeType() || column.supportTimestamp()) {
            return parameterizedForDateTime(column, value);
        }
        if (column.supportJson()) {
            return quote(toJsonText(value));
        }
        // XML、uniqueidentifier等按字符串处理
        if (column.supportText() || column.supportString()) {
            return quote(value);
        }
        // 数值类型
        if (column.supportInteger() || column.supportDigits() || value instanceof Number) {
            return value;
        }
        return quote(value);
    }

    /**
     * 将JSON值转换为SqlServer JSON函数可接受的文本。
     *
     * @param value JSON 值
     * @return JSON 文本
     */
    public static String toJsonText(Object value) {
        value = valueStandardization(value);
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence sequence) {
            return sequence.toString();
        }
        return JSON.toJSONString(value);
    }

    /**
     * 将位串值转换为 SqlServer 参数需要的 0/1 文本。
     *
     * @param column 字段
     * @param value  位串值
     * @return 位串文本
     */
    public static String toBitText(DBColumn column, Object value) {
        value = valueStandardization(value);
        if (value == null) {
            return null;
        }
        String bits;
        if (value instanceof Boolean bool) {
            bits = bool ? "1" : "0";
        } else if (value instanceof byte[] bytes) {
            bits = TextUtil.byteToBitStr(bytes);
        } else {
            bits = value.toString().trim();
            if (bits.length() >= 3 && bits.charAt(1) == '\'' && bits.endsWith("'")) {
                bits = bits.substring(2, bits.length() - 1);
            }
        }
        if (column != null
                && column.getSize() != null
                && bits != null
                && bits.length() > column.getSize()
                && (value instanceof byte[]
                || StringUtil.equalsAnyIgnoreCase(column.getType(), "BIT", "BIT WITHOUT LENGTH"))) {
            return bits.substring(bits.length() - column.getSize());
        }
        return bits;
    }

    /**
     * 参数化日期时间值
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    private static String parameterizedForDateTime(SqlServerColumn column, Object value) {
        // 日期
        if (column.isDateType()) {
            if (value instanceof Timestamp timestamp) {
                return "'" + DateUtil.format(timestamp, "yyyy-MM-dd") + "'";
            }
            if (value instanceof java.sql.Date date) {
                return "'" + DateUtil.format(date, "yyyy-MM-dd") + "'";
            }
            if (value instanceof Date date) {
                return "'" + DateUtil.format(date, "yyyy-MM-dd") + "'";
            }
        } else if (column.isTimeType()) {
            // 时间
            if (value instanceof Time time) {
                return "'" + DateUtil.format(time, "HH:mm:ss") + "'";
            }
            if (value instanceof Date date) {
                return "'" + DateUtil.format(date, "HH:mm:ss") + "'";
            }
        } else {
            // 时间戳，包含TIMESTAMP WITH TIME ZONE
            if (value instanceof Timestamp timestamp) {
                return "'" + DateUtil.format(timestamp, "yyyy-MM-dd HH:mm:ss.SSS") + "'";
            }
            if (value instanceof Date date) {
                return "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
            }
        }
        // java.time类型（如LocalDate、LocalDateTime）及字符串形式的日期时间值，直接使用ISO格式
        return quote(value);
    }

    /**
     * 包装字符串值
     *
     * @param value 值
     * @return 包装后的值
     */
    private static String quote(Object value) {
        return "N'" + escapeQuotes(value.toString()) + "'";
    }

    /**
     * 格式化布尔值，SqlServer支持true/false、t/f、yes/no、on/off、1/0等形式
     *
     * @param value 值
     * @return 格式化后的布尔值
     */
    public static String formatBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool ? "1" : "0";
        }
        if (value instanceof Number number) {
            return number.intValue() == 0 ? "0" : "1";
        }
        if (value instanceof CharSequence sequence) {
            String str = sequence.toString().trim();
            if (StringUtil.equalsAnyIgnoreCase(str, "TRUE", "T", "1", "YES", "Y", "ON")) {
                return "1";
            }
            if (StringUtil.equalsAnyIgnoreCase(str, "FALSE", "F", "0", "NO", "N", "OFF")) {
                return "0";
            }
        }
        return null;
    }

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
        // 大文本
        if (value instanceof Clob clob) {
            try {
                return clob.getSubString(1, Math.toIntExact(clob.length()));
            } catch (SQLException ex) {
                throw new ShellException(ex);
            }
        }
        // 大二进制
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
        // 复合类型、引用和行标识按数据库驱动提供的文本表示处理
        if (value instanceof Struct || value instanceof Ref || value instanceof RowId) {
            return value.toString();
        }
        // UUID
        if (value instanceof UUID uuid) {
            return uuid.toString();
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
    public static Object valueStandardization(SqlServerColumn column, Object value) {
        value = valueStandardization(value);
        if (column != null && column.supportBoolean() && value instanceof Number number) {
            return number.intValue() != 0;
        }
        return value;
    }
}
