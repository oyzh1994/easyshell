package cn.oyzh.easyshell.data.mariadb;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbDataUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.util.DBDataUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Mariadb数据导入助手
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDataImportHelper {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导入配置
     * @return 参数化后的值
     * @throws ParseException 解析异常
     */
    public static Object parameterized(MariadbColumn column, Object value, DBDataImportConfig config) throws ParseException {
        if (value == null) {
            return null;
        }
        if (value.toString().isEmpty()) {
            return null;
        }
        if (column.isDateType()) {
            if (value instanceof CharSequence date) {
                Date date1 = DateUtil.parse(date, config.getDateFormat());
                return DateUtil.format(date1, "yyyy-MM-dd HH:mm:ss");
            }
        }
        if (column.supportTimestamp()) {
            if (value instanceof CharSequence date) {
                LocalDateTime date1 = DateUtil.parseLocalDateTime(date, config.getDateFormat());
                return DateUtil.format(date1, "yyyy-MM-dd HH:mm:ss");
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            }
        }
        if (column.supportString()) {
            return value.toString();
        }
        return value;
    }

    /**
     * 将导入值转换为 MariaDB SQL 字面量。
     *
     * @param column 字段
     * @param value  原始值
     * @param config 导入配置
     * @return SQL 字面量
     */
    private static String toSqlLiteral(MariadbColumn column, Object value, DBDataImportConfig config) {
        try {
            value = parameterized(column, value, config);
        } catch (ParseException ignored) {
            // 保留数据库可识别的原始日期文本
        }
        if (value == null) {
            return "NULL";
        }
        String text = value.toString();
        if (column.supportGeometry()) {
            if (StringUtil.startWithIgnoreCase(text.strip(), "ST_GEOMFROMTEXT(")) {
                return text;
            }
            return "ST_GeomFromText(" + DBUtil.wrapString(text, DBDialect.MARIADB) + ")";
        }
        if (column.supportBit()) {
            String bits = text.strip();
            if (bits.startsWith("b'") && bits.endsWith("'")) {
                bits = bits.substring(2, bits.length() - 1);
            } else if (StringUtil.startWithIgnoreCase(bits, "0x")) {
                bits = TextUtil.byteToBitStr(HexUtil.decodeHexStr(bits.substring(2)));
            }
            if (!bits.matches("[01]+")) {
                return "NULL";
            }
            if (column.getSize() != null && bits.length() > column.getSize()) {
                bits = bits.substring(bits.length() - column.getSize());
            }
            return "b'" + bits + "'";
        }
        if (column.supportBinary()) {
            String binary = text.strip();
            if (StringUtil.startWithIgnoreCase(binary, "0x")) {
                return binary;
            }
            return "0x" + HexUtil.bytesToHex(text.getBytes(java.nio.charset.StandardCharsets.UTF_8), false);
        }
        if (column.supportJson()) {
            return DBUtil.wrapString(DBDataUtil.normalizeJson(text), DBDialect.MARIADB);
        }
        if (column.isYearType() && text.matches("\\d{4}.*")) {
            return text.substring(0, 4);
        }
        if (StringUtil.equalsAnyIgnoreCase(text, "true", "false")
                && (column.supportBoolean()
                || (column.getSize() != null && column.getSize() == 1
                && StringUtil.equalsIgnoreCase(column.getType(), "TINYINT")))) {
            return Boolean.parseBoolean(text) ? "1" : "0";
        }
        return DBUtil.wrapString(text, DBDialect.MARIADB);
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
    public static List<String> toInsertSql(MariadbColumns columns, List<MariadbRecord> records, DBDataImportConfig config) throws Exception {
        List<String> insertSql = new ArrayList<>();
        for (MariadbRecord record : records) {
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(DBUtil.wrap(columns.tableName(), DBDialect.MARIADB));
            sql.append("(");
            for (MariadbColumn column : columns) {
                if (column.isGenerated()) {
                    continue;
                }
                sql.append(DBUtil.wrap(column.getName(), DBDialect.MARIADB)).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(") VALUES (");
            for (MariadbColumn column : columns) {
                if (column.isGenerated()) {
                    continue;
                }
                Object val = record.getValue(column.getName());
                val = DBUtil.unwrapData(val, DBDialect.MARIADB);
                sql.append(toSqlLiteral(column, val, config)).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(")");
            insertSql.add(sql.toString());
//            System.out.println(sql);
        }
        return insertSql;
    }
}
