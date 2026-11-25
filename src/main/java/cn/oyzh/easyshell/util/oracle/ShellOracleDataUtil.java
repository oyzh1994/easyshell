package cn.oyzh.easyshell.util.oracle;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleRecordPrimaryKey;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.common.util.HexUtil;
import oracle.sql.BINARY_DOUBLE;
import oracle.sql.BINARY_FLOAT;
import oracle.sql.INTERVALDS;
import oracle.sql.INTERVALYM;
import oracle.sql.NUMBER;
import oracle.sql.RAW;
import oracle.sql.ROWID;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPTZ;
import oracle.sql.TIMESTAMPLTZ;
import oracle.xdb.XMLType;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleDataUtil {

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
        for (OracleRecord record : records) {
            list.add(OracleInsertSqlBuilder.build(columns, record.toMap(), includeFields));
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
            if (!column.isDmlWritable()) {
                continue;
            }
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
                value = valueStandardization(value);
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

    /**
     * 值规整化
     *
     * @param value 值
     * @return 规整后的值
     */
    public static Object valueStandardization(Object value) {
        if (value instanceof TIMESTAMP timestamp) {
            try {
                return timestamp.timestampValue();
            } catch (SQLException ex) {
                return timestamp.toString();
            }
        }
        if (value instanceof TIMESTAMPTZ timestamptz) {
            try {
                return timestamptz.offsetDateTimeValue().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (SQLException ex) {
                try {
                    return timestamptz.zonedDateTimeValue().toOffsetDateTime()
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (SQLException ignored) {
                    return timestamptz.toString();
                }
            }
        }
        if (value instanceof INTERVALYM || value instanceof INTERVALDS) {
            return value.toString();
        }
        if (value instanceof ROWID rowid) {
            return rowid.stringValue();
        }
        if (value instanceof XMLType xmlType) {
            try {
                return xmlType.getStringVal();
            } catch (SQLException ex) {
                return xmlType;
            }
        }
        if (value instanceof oracle.sql.DATE date) {
            return date.timestampValue();
        }
        if (value instanceof NUMBER number) {
            try {
                return number.bigDecimalValue();
            } catch (SQLException ex) {
                return number;
            }
        }
        if (value instanceof BINARY_FLOAT binaryFloat) {
            try {
                return binaryFloat.floatValue();
            } catch (SQLException ex) {
                return binaryFloat;
            }
        }
        if (value instanceof BINARY_DOUBLE binaryDouble) {
            try {
                return binaryDouble.doubleValue();
            } catch (SQLException ex) {
                return binaryDouble;
            }
        }
        if (value instanceof RAW raw) {
            try {
                return raw.toJdbc();
            } catch (SQLException ex) {
                return raw;
            }
        }
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

    /**
     * 值规整化，处理需要连接上下文的TIMESTAMP WITH LOCAL TIME ZONE等类型。
     *
     * @param connection 连接
     * @param value      值
     * @return 规整后的值
     */
    public static Object valueStandardization(Connection connection, Object value) {
        if (value instanceof TIMESTAMPTZ timestamptz) {
            try {
                return timestamptz.offsetDateTimeValue(connection)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (SQLException ex) {
                try {
                    return timestamptz.zonedDateTimeValue().toOffsetDateTime()
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (SQLException ignored) {
                    try {
                        return timestamptz.stringValue(connection);
                    } catch (SQLException ignoredAgain) {
                        return timestamptz.toString();
                    }
                }
            }
        }
        if (value instanceof TIMESTAMPLTZ timestampltz) {
            try {
                return timestampltz.localDateTimeValue(connection);
            } catch (SQLException ex) {
                try {
                    return timestampltz.timestampValue(connection);
                } catch (SQLException ignored) {
                    return timestampltz;
                }
            }
        }
        return valueStandardization(value);
    }

    /** 生成Oracle SQL值，日期和二进制使用Oracle原生函数，避免依赖NLS_DATE_FORMAT。 */
    public static Object parameterizedForSql(OracleColumn column, Object value) {
        if (value == null) {
            return "NULL";
        }
        if (column.isObjectType() || column.isCollectionType()) {
            return OracleComplexValueUtil.toSqlLiteral(column, value);
        }
        if (value instanceof Boolean bool) {
            return bool ? "TRUE" : "FALSE";
        }
        String type = sqlType(column);
        if (value instanceof INTERVALYM || value instanceof INTERVALDS) {
            return intervalLiteral(type, value.toString());
        }
        if (isIntervalYm(type) || isIntervalDs(type)) {
            return intervalLiteral(type, value.toString());
        }
        if (isRowId(type)) {
            if (value instanceof ROWID rowid) {
                return quote(rowid.stringValue());
            }
            return quote(value.toString());
        }
        if (isXmlType(type)) {
            String xml = value instanceof XMLType xmlType ? xmlValue(xmlType) : value.toString();
            return "XMLTYPE(" + quote(xml) + ")";
        }
        if (isJson(type)) {
            String json = value instanceof CharSequence sequence ? sequence.toString() : JSONUtil.toJson(value);
            return "JSON(" + quote(json) + ")";
        }
        if (isVector(type)) {
            return "TO_VECTOR(" + quote(value.toString()) + ")";
        }
        if (column.supportBinary()) {
            byte[] bytes = value instanceof byte[] array ? array : HexUtil.decodeHexStr(value.toString());
            return "HEXTORAW('" + HexUtil.encodeHexStr(bytes, false) + "')";
        }
        if (column.isDateType()) {
            if (value instanceof Date date) {
                return "TO_DATE('" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:SS')";
            }
            if (value instanceof LocalDateTime date) {
                return "TO_DATE('" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + "','YYYY-MM-DD HH24:MI:SS')";
            }
            if (value instanceof CharSequence) {
                return "TO_DATE('" + value + "','YYYY-MM-DD HH24:MI:SS')";
            }
        }
        if (column.supportTimestamp()) {
            if (value instanceof TIMESTAMPTZ timestamptz) {
                try {
                    return timestampTzLiteral(timestamptz.offsetDateTimeValue());
                } catch (SQLException ex) {
                    return timestampTzLiteral(value.toString());
                }
            }
            if (value instanceof OffsetDateTime date) {
                return timestampTzLiteral(date);
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
            if (value instanceof CharSequence sequence) {
                String text = sequence.toString().trim();
                if (isTimestampTz(type)) {
                    return timestampTzLiteral(text);
                }
                return timestampLiteral(text);
            }
        }
        if (value instanceof CharSequence sequence) {
            return quote(sequence.toString());
        }
        return DBUtil.wrapData(value, DBDialect.ORACLE);
    }

    private static String sqlType(OracleColumn column) {
        return column.getType() == null ? "" : column.getType().toUpperCase(Locale.ROOT);
    }

    private static boolean isIntervalYm(String type) {
        return StringUtil.contains(type, "INTERVAL YEAR");
    }

    private static boolean isIntervalDs(String type) {
        return StringUtil.contains(type, "INTERVAL DAY");
    }

    private static boolean isRowId(String type) {
        return StringUtil.equalsAnyIgnoreCase(type, "ROWID", "UROWID");
    }

    private static boolean isXmlType(String type) {
        return StringUtil.contains(type, "XMLTYPE");
    }

    private static boolean isJson(String type) {
        return StringUtil.equalsIgnoreCase(type, "JSON");
    }

    private static boolean isVector(String type) {
        return StringUtil.equalsIgnoreCase(type, "VECTOR");
    }

    private static boolean isTimestampTz(String type) {
        return StringUtil.contains(type, "TIMESTAMP")
                && StringUtil.contains(type, "WITH TIME ZONE")
                && !StringUtil.contains(type, "LOCAL TIME ZONE");
    }

    private static String intervalLiteral(String type, String value) {
        if (isIntervalYm(type)) {
            return "INTERVAL " + quote(value) + " YEAR TO MONTH";
        }
        return "INTERVAL " + quote(value) + " DAY TO SECOND(6)";
    }

    private static String xmlValue(XMLType xmlType) {
        try {
            return xmlType.getStringVal();
        } catch (SQLException ex) {
            return xmlType.toString();
        }
    }

    private static String quote(String value) {
        return DBUtil.wrapString(value, DBDialect.ORACLE);
    }

    private static String timestampTzLiteral(OffsetDateTime value) {
        return "TO_TIMESTAMP_TZ('" + value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX"))
                + "','YYYY-MM-DD HH24:MI:SS.FF6TZH:TZM')";
    }

    private static String timestampTzLiteral(String value) {
        try {
            return timestampTzLiteral(OffsetDateTime.parse(value));
        } catch (Exception ignored) {
            return "TO_TIMESTAMP_TZ('" + value.replaceFirst("T", " ").replace("'", "''")
                    + "','YYYY-MM-DD HH24:MI:SS.FF6TZH:TZM')";
        }
    }

    private static String timestampLiteral(LocalDateTime value) {
        return "TO_TIMESTAMP('" + value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
                + "','YYYY-MM-DD HH24:MI:SS.FF6')";
    }

    private static String timestampLiteral(String value) {
        return "TO_TIMESTAMP('" + value.replaceFirst("T", " ").replace("'", "''")
                + "','YYYY-MM-DD HH24:MI:SS.FF6')";
    }
}
