package cn.oyzh.easyshell.data.sqlserver.file;

import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerDataImportHelper;
import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerDataUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.file.DBDataTypeFileWriter;
import cn.oyzh.fx.db.util.DBUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * SqlServer类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class ShellSqlServerTypeFileWriter extends DBDataTypeFileWriter {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导出配置
     * @return 参数化后的值
     */
    public Object parameterized(SqlServerColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return null;
        }
        value = ShellSqlServerDataUtil.valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
        if (column.isDateType() || column.supportTimestamp() || column.isTimeType()) {
            String date = this.formatTemporal(value, config);
            if (date != null) {
                return date;
            }
        }
        if (column.supportJson()) {
            return value.toString();
        }
        if (column.supportBinary()) {
            // SQL Server binary使用0x十六进制文本格式
            if (value instanceof byte[] bytes) {
                if (bytes.length == 0) {
                    return "0x";
                }
                return "0x" + HexUtil.encodeHexStr(bytes, false);
            }
        }
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
            return StringUtil.equalsAnyIgnoreCase(value.toString(), "true", "t", "1", "yes", "y") ? "1" : "0";
        }
        if (column.supportBit()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
            return StringUtil.equalsAnyIgnoreCase(value.toString(), "true", "t", "1", "yes", "y") ? "1" : "0";
        }
        //        if (column.supportEnum()) {
        //            return value.toString();
        //        }
        if (column.supportString()) {
            return value.toString();
        }
        if (column.supportInteger() || column.supportDigits()) {
            return value;
        }
        return value.toString();
    }

    /**
     * 转换为SqlServer Sql字面量
     *
     * @param column 字段
     * @param value  值
     * @param config 导出配置
     * @return Sql字面量
     */
    protected String toSqlLiteral(SqlServerColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return "NULL";
        }
        value = ShellSqlServerDataUtil.valueStandardization(value);
        if (column.isDateType() || column.supportTimestamp() || column.isTimeType()) {
            String date = this.formatTemporal(value, config);
            if (date != null) {
                return "'" + date + "'";
            }
        }
        if (column.supportBinary()) {
            // SQL Server binary十六进制字面量
            if (value instanceof byte[] bytes) {
                if (bytes.length == 0) {
                    return "0x";
                }
                return "0x" + HexUtil.encodeHexStr(bytes, false);
            }
        }
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
            return StringUtil.equalsAnyIgnoreCase(value.toString(), "true", "t", "1", "yes", "y") ? "1" : "0";
        }
        if (column.supportBit()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
            return StringUtil.equalsAnyIgnoreCase(value.toString(), "true", "t", "1", "yes", "y") ? "1" : "0";
        }
        //        if (column.supportEnum()) {
        //            return "'" + value + "'";
        //        }
        if (column.supportInteger() || column.supportDigits() || value instanceof Number) {
            return value.toString();
        }
        // 字符串、json、uuid等统一使用单引号字面量
        return ShellSqlServerDataImportHelper.sqlStringLiteral(value.toString());
    }

    /**
     * 格式化日期时间
     *
     * @param value  值
     * @param config 导出配置
     * @return 格式化后的日期时间
     */
    private String formatTemporal(Object value, DBDataExportConfig config) {
        String pattern = config.getDateFormat();
        if (StringUtil.isBlank(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (value instanceof LocalDateTime date) {
            return DateUtil.format(date, pattern);
        }
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        if (value instanceof LocalTime time) {
            return time.toString();
        }
        if (value instanceof java.sql.Time time) {
            return DateUtil.format(time, "HH:mm:ss");
        }
        if (value instanceof Date date) {
            return DateUtil.format(date, pattern);
        }
        return null;
    }

    /**
     * 转换为位串
     *
     * @param value 值
     * @return 位串
     */
    private String bitString(Object value) {
        if (value instanceof byte[] bytes) {
            return TextUtil.byteToBitStr(bytes);
        }
        if (value instanceof Boolean b) {
            return b ? "1" : "0";
        }
        return value.toString().replaceAll("[^01]", "");
    }
}
