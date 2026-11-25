package cn.oyzh.easyshell.util.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import org.bson.BsonDecimal128;
import org.bson.BsonMaxKey;
import org.bson.BsonMinKey;
import org.bson.BsonRegularExpression;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.Decimal128;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * mongodb工具类
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoUtil {

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
        if (val instanceof Decimal128) {
            return "decimal";
        }
        if (val instanceof Number) {
            return "double";
        }
        if (val instanceof Character || val instanceof CharSequence) {
            return "string";
        }
        if (val instanceof List) {
            return "list";
        }
        if (val instanceof Boolean) {
            return "boolean";
        }
        if (val instanceof java.util.Date) {
            return "date";
        }
        if (val instanceof byte[] || val instanceof Byte[] || val instanceof Binary) {
            return "binary";
        }
        if (val instanceof ObjectId) {
            return "objectid";
        }
        if (val instanceof Document) {
            return "object";
        }
        if (val instanceof Code) {
            return "code";
        }
        if (val instanceof Pattern || val instanceof BsonRegularExpression) {
            return "regex";
        }
        if (val instanceof MinKey || val instanceof BsonMinKey) {
            return "minKey";
        }
        if (val instanceof MaxKey || val instanceof BsonMaxKey) {
            return "maxKey";
        }
        if (val instanceof Symbol) {
            return "string";
        }
        if (val instanceof BsonValue bsonValue) {
            if (bsonValue.isInt32()) {
                return "int";
            }
            if (bsonValue.isInt64()) {
                return "long";
            }
            if (bsonValue.isDecimal128() || bsonValue instanceof BsonDecimal128) {
                return "decimal";
            }
            if (bsonValue.isDouble() || bsonValue.isNumber()) {
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
                return "objectid";
            }
            if (bsonValue.isRegularExpression()) {
                return "regex";
            }
            if (bsonValue.isSymbol()) {
                return "string";
            }
            if (bsonValue.isJavaScript() || bsonValue.isJavaScriptWithScope()) {
                return "code";
            }
        }
        return "string";
    }

    /**
     * 是否原始类型
     *
     * @param val 值
     * @return 类型
     */
    public static boolean isPrimaryType(Object val) {
        return StringUtil.equalsAnyIgnoreCase(getType(val), "int", "long", "double", "decimal",
                "list", "object", "boolean");
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
