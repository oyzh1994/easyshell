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
import oracle.sql.TIMESTAMP;
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
                    if (!dbColumn.isDmlWritable()) {
                        continue;
                    }
                    sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.ORACLE)).append(", ");
                }
                if (sql.toString().endsWith(", ")) {
                    sql.delete(sql.length() - 2, sql.length());
                }
                sql.append(")");
            }
            sql.append(" VALUES (");
            for (OracleColumn dbColumn : columnList) {
                if (!dbColumn.isDmlWritable()) {
                    continue;
                }
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
