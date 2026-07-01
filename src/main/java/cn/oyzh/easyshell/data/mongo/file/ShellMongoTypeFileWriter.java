package cn.oyzh.easyshell.data.mongo.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.easyshell.data.mongo.config.ShellMongoDataExportConfig;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.util.mongo.ShellMongoDataUtil;
import org.bson.types.ObjectId;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2024-09-04
 */
public abstract class ShellMongoTypeFileWriter implements Closeable {

    protected void init() throws Exception {

    }

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @param config 导出配置
     * @return 参数化后的值
     */
    public Object parameterized(MongoColumn column, Object value, ShellMongoDataExportConfig config) {
        if (value == null) {
            return "";
        }
        if (column.supportString()) {
            return ShellMongoDataUtil.escapeQuotes(value.toString());
        }
        if (column.supportObjectId()) {
            if (value instanceof ObjectId id) {
                return id.toHexString();
            }
            return value.toString();
        }
        if (column.supportInteger() || column.supportDigits() || column.supportBoolean()) {
            return value;
        }
        if (column.supportDate()) {
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
        if (column.supportList() || column.supportObject() || column.supportCode()) {
            return JSONUtil.toJson(value);
        }
        return value.toString();
    }

    /**
     * 写入头
     *
     * @throws Exception 异常
     */
    public void writeHeader() throws Exception {

    }

    /**
     * 写入尾
     *
     * @throws Exception 异常
     */
    public void writeTrial() throws Exception {

    }

    /**
     * 写入对象
     *
     * @param object 对象
     * @throws Exception 异常
     */
    public abstract void writeObject(Map<String, Object> object) throws Exception;

    /**
     * 写入多个对象
     *
     * @param objects 对象
     * @throws Exception 异常
     */
    public void writeObjects(List<Map<String, Object>> objects) throws Exception {
        for (Map<String, Object> object : objects) {
            this.writeObject(object);
        }
    }

    protected String formatLine(Object[] objects, String fieldSeparator, String txtIdentifier, String recordSeparator) {
        List<Object> list = new ArrayList<>();
        for (Object object : objects) {
            list.add(Objects.requireNonNullElse(object, ""));
        }
        return this.formatLine(list, fieldSeparator, txtIdentifier, recordSeparator);
    }

    protected String formatLine(List<?> list, String fieldSeparator, String txtIdentifier, String recordSeparator) {
        StringBuilder sb = new StringBuilder();
        for (Object val : list) {
            sb.append(fieldSeparator)
                    .append(txtIdentifier)
                    .append(val)
                    .append(txtIdentifier);
        }
        sb.append(recordSeparator);
        return sb.substring(1);
    }

}
