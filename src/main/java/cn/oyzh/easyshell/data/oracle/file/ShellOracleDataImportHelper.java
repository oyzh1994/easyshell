package cn.oyzh.easyshell.data.oracle.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.util.DBUtil;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
                LocalDateTime date1 = DateUtil.parseLocalDateTime(date, config.getDateFormat());
                return timestampLiteral(date1);
            }
            if (value instanceof Date date) {
                return timestampLiteral(new java.sql.Timestamp(date.getTime()).toLocalDateTime());
            }
            if (value instanceof OffsetDateTime date) {
                return "TO_TIMESTAMP_TZ('" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXXX"))
                        + "','YYYY-MM-DD HH24:MI:SS.FF3TZH:TZM')";
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
        return "TO_TIMESTAMP('" + value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                + "','YYYY-MM-DD HH24:MI:SS.FF3')";
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
        for (OracleRecord record : records) {
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(DBUtil.wrap(columns.tableName(), DBDialect.ORACLE));
            sql.append("(");
            for (OracleColumn column : columns) {
                sql.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE)).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(") VALUES (");
            for (OracleColumn column : columns) {
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
