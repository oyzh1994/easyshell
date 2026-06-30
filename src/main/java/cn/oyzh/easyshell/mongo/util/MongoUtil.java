package cn.oyzh.easyshell.mongo.util;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * db工具类
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class MongoUtil {

    public static final String ID = "_id";

    public static final String SYSTEM_JS = "system.js";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * 获取值类型
     *
     * @param val 值
     * @return 类型
     */
    public static String getType(Object val) {
        if (val instanceof Integer
                || val instanceof Short
                || val instanceof Byte) {
            return "int";
        }
        if (val instanceof Long) {
            return "long";
        }
        if (val instanceof Number) {
            return "double";
        }
        if (val instanceof Character || val instanceof CharSequence) {
            return "string";
        }
        if (val instanceof List<?>) {
            return "list";
        }
        if (val instanceof Boolean) {
            return "boolean";
        }
        if (val instanceof java.util.Date) {
            return "date";
        }
        if (val instanceof byte[] || val instanceof Binary) {
            return "binary";
        }
        if (val instanceof ObjectId) {
            return "obejectid";
        }
        if (val instanceof Document) {
            return "object";
        }
        if (val instanceof Code) {
            return "code";
        }
        if (val instanceof BsonValue bsonValue) {
            if (bsonValue.isInt32() || bsonValue.isInt64()) {
                return "int";
            }
            if (bsonValue.isDouble() || bsonValue.isDecimal128() || bsonValue.isNumber()) {
                return "double";
            }
            if (bsonValue.isDateTime() || bsonValue.isTimestamp()) {
                return "date";
            }
            if (bsonValue.isArray()) {
                return "list";
            }
            if (bsonValue.isBinary()) {
                return "binary";
            }
            if (bsonValue.isBoolean()) {
                return "boolean";
            }
            if (bsonValue.isString()) {
                return "string";
            }
            if (bsonValue.isDocument()) {
                return "object";
            }
            if (bsonValue.isObjectId()) {
                return "obejectid";
            }
        }
        return "object";
    }

    /**
     * 是否原始类型
     *
     * @param val 值
     * @return 类型
     */
    public static boolean isPrimaryType(Object val) {
        return StringUtil.equalsAnyIgnoreCase(getType(val), "int", "long", "double", "list", "object", "boolean");
    }

    /**
     * 移除注释
     *
     * @param sql sql
     * @return 结果
     */
    public static String removeComment(String sql) {
        StringBuilder builder = new StringBuilder();
        AtomicBoolean commentFlag = new AtomicBoolean(false);
        sql.lines().forEach(line -> {
            // 单行注释1
            if (line.stripLeading().startsWith("-- ")) {
                return;
            }
            // 单行注释2
            if (line.stripLeading().startsWith("#")) {
                return;
            }
            // 单行注释3
            if (line.stripLeading().startsWith("//")) {
                return;
            }
            // 多行注释开始
            if (line.stripLeading().startsWith("/*")) {
                commentFlag.set(true);
            }
            // 多行注释结束
            if (line.stripTrailing().endsWith("*/")) {
                commentFlag.set(false);
                return;
            }
            // 正常行
            if (!commentFlag.get() && StringUtil.isNotBlank(line)) {
                builder.append(line).append("\n");
            }
        });
        return builder.toString();
    }

    /**
     * 生成克隆名称
     *
     * @return 复制名称
     */
    public static String genCloneName() {
        return "_clone_" + UUIDUtil.uuidSimple().substring(0, 5);
    }
}
