package cn.oyzh.easyshell.data.mariadb.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.file.DBDataTypeFileWriter;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mariadb类型文件写入器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public abstract class ShellMariadbTypeFileWriter extends DBDataTypeFileWriter {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导出配置
     * @return 参数化后的值
     */
    public Object parameterized(MariadbColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return null;
        }
        value = ShellMariadbDataUtil.valueStandardization(column, value);
        if (column.supportGeometry()) {
            return "ST_GeomFromText('" + value + "')";
        }
        if (column.isDateType() || column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return DateUtil.format(date, config.getDateFormat());
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, config.getDateFormat());
            }
        }
        if (column.supportJson()) {
            return value.toString();
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBit()) {
            if (value instanceof Boolean bool) {
                return bool ? "b'1'" : "b'0'";
            }
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            String bits = TextUtil.byteToBitStr(bytes);
            if (column.getSize() != null && bits.length() > column.getSize()) {
                bits = bits.substring(bits.length() - column.getSize());
            }
            return "b'" + bits + "'";
        }
        if (column.supportEnum()) {
            return value.toString();
        }
        if (column.supportString()) {
            return value.toString();
        }
        if (column.supportInteger() || column.supportDigits()) {
            return value;
        }
        return value.toString();
    }

    /**
     * 转义 XML 文本节点和属性值。
     *
     * @param value 原始值
     * @return 转义后的值
     */
    protected String escapeXml(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
