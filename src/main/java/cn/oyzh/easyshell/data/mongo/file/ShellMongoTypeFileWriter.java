package cn.oyzh.easyshell.data.mongo.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.util.mongo.ShellMongoDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.file.DBDataTypeFileWriter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Mongo类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class ShellMongoTypeFileWriter extends DBDataTypeFileWriter {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导出配置
     * @return 参数化后的值
     */
    public Object parameterized(MongoColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return null;
        }
        value = ShellMongoDataUtil.valueStandardization(value);
        if (column.supportString()) {
            return TextUtil.escape(value.toString());
        }
        if (column.supportObjectId()) {
            if (value instanceof ObjectId id) {
                return id.toHexString();
            }
            return value.toString();
        }
        if (column.supportInteger() || column.supportBigInteger() || column.supportDigits() || column.supportBoolean()) {
            return value;
        }
        if (column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return DateUtil.format(date, config.getDateFormat());
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, config.getDateFormat());
            }
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportJson() || column.supportJsonArray() || column.supportCode()) {
            return JSONUtil.toJson(value);
        }
        return value.toString();
    }
}
