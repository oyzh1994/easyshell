package cn.oyzh.easyshell.util.mongo;

import cn.oyzh.common.util.Base64Util;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoRecordProperty;
import org.bson.BsonBinary;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/08/26
 */
public class ShellMongoDataUtil {

    /**
     * 转义符号
     *
     * @param str 内容
     * @return 转义后的内容
     */
    public static String escapeQuotes(String str) {
        if (str != null && (str.contains("'") ||
                str.contains("\"") ||
                str.contains("\\") ||
                str.contains("\r") ||
                str.contains("\n"))) {
            StringBuilder sb = new StringBuilder();
            for (char c : str.toCharArray()) {
                if (c == '\'') {
                    //                    sb.append("\\'");
                    sb.append(c);
                } else if (c == '"') {
                    sb.append("\\\"");
                } else if (c == '\\') {
                    sb.append("\\\\");
                } else if (c == '\r') {
                    sb.append("\\r");
                } else if (c == '\n') {
                    sb.append("\\n");
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        return str;
    }

    /**
     * 获取记录脚本
     *
     * @param record 记录
     * @param skipId 是否跳过id
     * @return 脚本
     */
    public static String getRecordScript(MongoRecord record, boolean skipId) {
        StringBuilder builder = new StringBuilder();
        for (MongoColumn column : record.getColumns()) {
            String colName = column.getName();
            MongoRecordProperty property = record.getProperty(colName);
            if (property == null) {
                continue;
            }
            if (skipId && column.is_id()) {
                continue;
            }
            Object value = column.is_id() ? property.getOriginal() : property.get();
            buildRecordData(column, value, builder, 1);
        }
        return "{" + builder.substring(1) + "\n}";
    }

    /**
     * 构建记录值
     *
     * @param value 值
     * @param deep  当前深度
     * @return 结果
     */
    public static Object buildRecordValue(Object value, int deep) {
        String type = ShellMongoUtil.getType(value);
        return buildRecordValue(value, type, deep);
    }

    /**
     * 构建记录值
     *
     * @param value 值
     * @param type  类型
     * @param deep  当前深度
     * @return 结果
     */
    public static Object buildRecordValue(Object value, String type, int deep) {
        if ("int".equalsIgnoreCase(type)) {
            if (value == null) {
                return "Int32()";
            }
            return "Int32(" + value + ")";
        }

        if ("long".equalsIgnoreCase(type)) {
            if (value == null) {
                return "Long()";
            }
            return "Long(" + value + ")";
        }

        if ("double".equalsIgnoreCase(type) || "boolean".equalsIgnoreCase(type)) {
            return value;
        }

        if ("obejectid".equalsIgnoreCase(type)) {
            if (value == null) {
                return "ObjectId()";
            }
            if (value instanceof String s) {
                return "ObjectId('" + s + "')";
            }
            ObjectId id = (ObjectId) value;
            return "ObjectId('" + id.toHexString() + "')";
        }

        if ("date".equalsIgnoreCase(type)) {
            if (value == null) {
                return "ISODate()";
            }
            if (value instanceof String s) {
                return "ISODate('" + s + "')";
            }
            Date date = (Date) value;
            return "ISODate('" + ShellMongoUtil.DATE_FORMAT.format(date) + "')";
        }

        if ("binary".equalsIgnoreCase(type)) {
            if (value == null) {
                return "Binary.createFromBase64()";
            }
            byte[] bytes;
            if (value instanceof byte[] bytes1) {
                bytes = bytes1;
            } else if (value instanceof Binary binary) {
                bytes = binary.getData();
            } else if (value instanceof BsonBinary binary) {
                bytes = binary.getData();
            } else {
                bytes = new byte[]{};
            }
            return "Binary.createFromBase64('" + Base64Util.encodeToString(bytes) + "', 0)";
        }

        if ("list".equalsIgnoreCase(type)) {
            if (value instanceof String s) {
                return s;
            }
            StringBuilder sb = new StringBuilder();
            List<?> list = (List<?>) value;
            if (list != null && !list.isEmpty()) {
                for (Object val1 : list) {
                    Object val = buildRecordValue(val1, deep + 1);
                    sb.append(",\n").repeat("\t", deep + 1).append(val);
                }
                sb = new StringBuilder(sb.substring(1));
            }
            return "[" + sb + "\n" + "\t".repeat(deep) + "]";
        }

        if ("object".equalsIgnoreCase(type)) {
            if (value instanceof String s) {
                return s;
            }
            StringBuilder sb = new StringBuilder();
            Map<String, Object> document = (Map<String, Object>) value;
            if (document != null && !document.isEmpty()) {
                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    buildRecordData(entry.getKey(), entry.getValue(), sb, deep + 1);
                }
                sb = new StringBuilder(sb.substring(1));
            }
            return "{" + sb + "\n" + "\t".repeat(deep) + "}";
        }

        if (value != null) {
            return "'" + value + "'";
        }
        return null;
    }

    /**
     * 构建记录数据
     *
     * @param column  字段
     * @param value   值
     * @param builder 缓存
     * @param deep    深度
     */
    private static void buildRecordData(MongoColumn column, Object value, StringBuilder builder, int deep) {
        builder.append(",\n");
        builder.repeat("\t", deep);
        builder.append(column.getName())
                .append(": ")
                .append(buildRecordValue(value, column.getType(), deep));
    }

    /**
     * 构建记录数据
     *
     * @param colName 字段名
     * @param value   值
     * @param builder 缓存
     * @param deep    深度
     */
    private static void buildRecordData(String colName, Object value, StringBuilder builder, int deep) {
        builder.append(",\n");
        builder.repeat("\t", deep);
        builder.append(colName)
                .append(": ")
                .append(buildRecordValue(value, deep));
    }

    /**
     * 转换为插入脚本
     *
     * @param record 记录
     * @return 结果
     */
    public static String toInsertScript(MongoRecord record) {
        MongoColumn column = record.getColumns().getFirst();
        String script = getRecordScript(record, false);
        return toInsertScript(column.getCollectionName(), script);
    }

    /**
     * 转换为插入脚本
     *
     * @param collectionName 记录
     * @param doc            文档
     * @return 结果
     */
    public static String toInsertScript(String collectionName, String doc) {
        String sql = """
                db.getCollection('$collection').insert($doc);
                """;
        return sql.replace("$collection", collectionName).replace("$doc", doc);
    }

    /**
     * 转换为插入脚本
     *
     * @param records 记录列表
     * @return 结果
     */
    public static List<String> toInsertScript(List<MongoRecord> records) {
        List<String> list = new ArrayList<>();
        for (MongoRecord record : records) {
            list.add(toInsertScript(record));
        }
        return list;
    }

    /**
     * 转换为更新脚本
     *
     * @param record 记录
     * @return 结果
     */
    public static String toUpdateScript(MongoRecord record) {
        MongoColumn column = record._idColumn();
        Object id = record._idValue();
        String script = getRecordScript(record, true);
        return toUpdateScript(column.getCollectionName(), id, script);
    }

    /**
     * 转换为更新脚本
     *
     * @param collectionName 记录
     * @param id             id
     * @param doc            文档
     * @return 结果
     */
    public static String toUpdateScript(String collectionName, Object id, String doc) {
        String sql = """
                db.getCollection('$collection').update({_id: '$id'},{$set: $doc});
                """;
        Object idVal = buildRecordValue(id, 0);
        return sql.replace("$collection", collectionName).replace("$id", idVal.toString()).replace("$doc", doc);
    }

    /**
     * 转换为替换脚本
     *
     * @param function 记录
     * @return 结果
     */
    public static String toReplaceScript(MongoFunction function) {
        String script = """
                db.getCollection('$collectionName').replaceOne(
                    { _id: '$id' },
                    { _id: '$name', value: Code('$code') },
                    { upsert: true }
                );
                """;
        script = script.replace("$collectionName", ShellMongoUtil.SYSTEM_JS);
        script = script.replace("$id", function.getName());
        script = script.replace("$name", function.getName());
        script = script.replace("$code", function.getCode());
        return script;
    }
}
