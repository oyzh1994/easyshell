package cn.oyzh.easyshell.data.dameng.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.util.dameng.ShellDamengDataUtil;
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
 * Dameng类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class ShellDamengTypeFileWriter extends DBDataTypeFileWriter {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导出配置
     * @return 参数化后的值
     */
    public Object parameterized(DamengColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return null;
        }
        value = ShellDamengDataUtil.valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
        if (column.isDateType() || column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return DateUtil.format(date, config.getDateFormat());
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, config.getDateFormat());
            }
        }
        if (column.supportJson()) {
            return TextUtil.escape(value.toString());
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
                return b ? "1" : "0";
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
}
