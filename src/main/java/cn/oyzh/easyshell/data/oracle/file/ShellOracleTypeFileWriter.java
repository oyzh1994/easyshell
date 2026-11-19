package cn.oyzh.easyshell.data.oracle.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.file.DBDataTypeFileWriter;

import java.io.Closeable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Oracle类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class ShellOracleTypeFileWriter extends DBDataTypeFileWriter {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导出配置
     * @return 参数化后的值
     */
    public Object parameterized(OracleColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return null;
        }
        value = ShellOracleDataUtil.valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
        if (column.isDateType() || column.supportTimestamp()) {
            if (value instanceof OffsetDateTime date) {
                if (isTimestampTz(column)) {
                    return date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }
                return DateUtil.format(date.toLocalDateTime(), config.getDateFormat());
            }
            if (value instanceof Instant instant) {
                OffsetDateTime date = instant.atOffset(ZoneOffset.UTC);
                if (isTimestampTz(column)) {
                    return date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }
                return DateUtil.format(date.toLocalDateTime(), config.getDateFormat());
            }
            if (value instanceof LocalDateTime date) {
                return DateUtil.format(date, config.getDateFormat());
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, config.getDateFormat());
            }
        }
        if (column.isObjectType() || column.isCollectionType()) {
            String json = value instanceof CharSequence sequence
                    ? sequence.toString() : JSONUtil.toJson(value);
            return TextUtil.escape(json);
        }
        if (column.supportJson()) {
            return TextUtil.escape(value instanceof CharSequence sequence
                    ? sequence.toString() : JSONUtil.toJson(value));
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
                return b ? "TRUE" : "FALSE";
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

    private static boolean isTimestampTz(OracleColumn column) {
        String type = column.getType();
        return type != null
                && type.toUpperCase().contains("WITH TIME ZONE")
                && !type.toUpperCase().contains("LOCAL");
    }
}
