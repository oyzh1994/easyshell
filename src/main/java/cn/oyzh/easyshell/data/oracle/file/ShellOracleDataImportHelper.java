package cn.oyzh.easyshell.data.oracle.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.util.oracle.OracleComplexValueUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.util.DBUtil;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Oracle数据导入助手
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleDataImportHelper {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导入配置
     * @return 参数化后的值
     * @throws ParseException 解析异常
     */
    public static Object parameterized(OracleColumn column, Object value, DBDataImportConfig config) throws ParseException {
        if (value == null) {
            return null;
        }
        if (value.toString().isEmpty()) {
            return null;
        }
        if (column.isObjectType() || column.isCollectionType()) {
            return OracleComplexValueUtil.toSqlLiteral(column, value);
        }
        if (column.supportBoolean()) {
            if (value instanceof Boolean bool) {
                return bool ? "TRUE" : "FALSE";
            }
            if (value instanceof Number number) {
                return number.intValue() != 0 ? "TRUE" : "FALSE";
            }
            String text = value.toString().trim();
            if ("1".equals(text) || "true".equalsIgnoreCase(text)) {
                return "TRUE";
            }
            if ("0".equals(text) || "false".equalsIgnoreCase(text)) {
                return "FALSE";
            }
        }
        String type = column.getType() == null ? "" : column.getType().toUpperCase();
        if (isIntervalYm(type)) {
            return "INTERVAL " + DBUtil.wrapData(value, DBDialect.ORACLE) + " YEAR TO MONTH";
        }
        if (isIntervalDs(type)) {
            return "INTERVAL " + DBUtil.wrapData(value, DBDialect.ORACLE) + " DAY TO SECOND(6)";
        }
        if (isRowId(type)) {
            return DBUtil.wrapData(value, DBDialect.ORACLE);
        }
        if (isXmlType(type)) {
            return "XMLTYPE(" + DBUtil.wrapData(value, DBDialect.ORACLE) + ")";
        }
        if (isVector(type)) {
            return "TO_VECTOR(" + DBUtil.wrapData(value, DBDialect.ORACLE) + ")";
        }
        if (column.supportJson()) {
            String json = value instanceof CharSequence sequence ? sequence.toString() : JSONUtil.toJson(value);
            return "JSON(" + DBUtil.wrapData(json, DBDialect.ORACLE) + ")";
        }
        if (column.isDateType()) {
            if (value instanceof CharSequence date) {
                Date date1 = DateUtil.parse(date, config.getDateFormat());
                return "TO_DATE('" + DateUtil.format(date1, "yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:SS')";
            }
            if (value instanceof Date date) {
                return "TO_DATE('" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "','YYYY-MM-DD HH24:MI:SS')";
            }
        }
        if (column.supportTimestamp()) {
            if (value instanceof CharSequence date) {
                OffsetDateTime offsetDateTime = parseOffsetDateTime(date.toString(), config.getDateFormat());
                if (offsetDateTime != null) {
                    if (isTimestampTz(column)) {
                        return timestampTzLiteral(offsetDateTime);
                    }
                    return timestampLiteral(offsetDateTime.toLocalDateTime());
                }
                LocalDateTime date1 = parseLocalDateTime(date.toString(), config.getDateFormat());
                return timestampLiteral(date1);
            }
            if (value instanceof Date date) {
                return timestampLiteral(new java.sql.Timestamp(date.getTime()).toLocalDateTime());
            }
            if (value instanceof OffsetDateTime date) {
                if (isTimestampTz(column)) {
                    return timestampTzLiteral(date);
                }
                return timestampLiteral(date.toLocalDateTime());
            }
            if (value instanceof Instant date) {
                return timestampLiteral(date.atOffset(ZoneOffset.UTC).toLocalDateTime());
            }
        }
        if (column.supportBinary()) {
            byte[] bytes = value instanceof byte[] array ? array
                    : HexUtil.decodeHexStr(value.toString().replaceFirst("^(?i)0x", ""));
            return "HEXTORAW('" + HexUtil.encodeHexStr(bytes, false) + "')";
        }
        return DBUtil.wrapData(value, DBDialect.ORACLE);
    }

    private static String timestampLiteral(LocalDateTime value) {
        return "TO_TIMESTAMP('" + value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
                + "','YYYY-MM-DD HH24:MI:SS.FF6')";
    }

    private static String timestampTzLiteral(OffsetDateTime value) {
        return "TO_TIMESTAMP_TZ('" + value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX"))
                + "','YYYY-MM-DD HH24:MI:SS.FF6TZH:TZM')";
    }

    private static boolean isTimestampTz(OracleColumn column) {
        String type = column.getType();
        return type != null && type.toUpperCase().contains("WITH TIME ZONE");
    }

    private static boolean isIntervalYm(String type) {
        return type.contains("INTERVAL YEAR");
    }

    private static boolean isIntervalDs(String type) {
        return type.contains("INTERVAL DAY");
    }

    private static boolean isRowId(String type) {
        return "ROWID".equals(type) || "UROWID".equals(type);
    }

    private static boolean isXmlType(String type) {
        return type.contains("XMLTYPE");
    }

    private static boolean isVector(String type) {
        return "VECTOR".equals(type);
    }

    private static OffsetDateTime parseOffsetDateTime(String value, String dateFormat) {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS XXX"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS XXX"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXXX"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"));
        if (StringUtil.isNotBlank(dateFormat)
                && !dateFormat.contains("X") && !dateFormat.contains("Z")) {
            formatters.add(DateTimeFormatter.ofPattern(dateFormat + " XXX"));
        }
        for (DateTimeFormatter formatter : formatters) {
            try {
                return OffsetDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static LocalDateTime parseLocalDateTime(String value, String dateFormat) throws ParseException {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return DateUtil.parseLocalDateTime(value, dateFormat);
        } catch (Exception ex) {
            throw new ParseException("Unable to parse Oracle timestamp value: " + value, 0);
        }
    }

    /**
     * 转换为插入sql
     *
     * @param columns 字段列表
     * @param records 记录
     * @param config  配置
     * @return 插入sql
     * @throws Exception 异常
     */
    public static List<String> toInsertSql(OracleColumns columns, List<OracleRecord> records, DBDataImportConfig config) throws Exception {
        List<String> insertSql = new ArrayList<>();
        List<OracleColumn> columnList = columns.stream()
                .filter(OracleColumn::isDmlWritable)
                .toList();
        if (columnList.isEmpty()) {
            return insertSql;
        }
        for (OracleRecord record : records) {
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(DBUtil.wrap(columns.tableName(), DBDialect.ORACLE));
            sql.append("(");
            for (OracleColumn column : columnList) {
                sql.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE)).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(") VALUES (");
            for (OracleColumn column : columnList) {
                Object val = record.getValue(column.getName());
                val = DBUtil.unwrapData(val, DBDialect.ORACLE);
                val = parameterized(column, val, config);
                sql.append(val == null ? "NULL" : val).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(")");
            insertSql.add(sql.toString());
        }
        return insertSql;
    }
}
