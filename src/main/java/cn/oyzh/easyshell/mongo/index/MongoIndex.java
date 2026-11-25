package cn.oyzh.easyshell.mongo.index;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;
import org.bson.Document;

import java.util.Map;

/**
 * MongoDB 索引。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoIndex extends DBObject implements ObjectCopier<MongoIndex> {

    /**
     * 索引名称
     */
    private String name;

    /**
     * 索引字段
     */
    private Document keys;

    /**
     * 是否唯一索引
     */
    private boolean unique;

    /**
     * 是否稀疏索引
     */
    private boolean sparse;

    /**
     * 是否后台构建
     */
    private boolean background;

    /**
     * 是否隐藏索引
     */
    private boolean hidden;

    /**
     * TTL 过期时间，单位秒
     */
    private Long expireAfterSeconds;

    /**
     * 部分过滤表达式
     */
    private Document partialFilterExpression;

    /**
     * 从 MongoDB 索引文档构建索引对象。
     *
     * @param document 索引文档
     * @return 索引对象
     */
    public static MongoIndex from(Document document) {
        MongoIndex index = new MongoIndex();
        if (document == null) {
            return index;
        }
        index.setName(document.getString("name"));
        index.setKeys(asDocument(document.get("key")));
        index.setUnique(getBoolean(document, "unique"));
        index.setSparse(getBoolean(document, "sparse"));
        index.setBackground(getBoolean(document, "background"));
        index.setHidden(getBoolean(document, "hidden"));
        Object expire = document.get("expireAfterSeconds");
        if (expire instanceof Number number) {
            index.setExpireAfterSeconds(number.longValue());
        }
        index.setPartialFilterExpression(asDocument(document.get("partialFilterExpression")));
        return index;
    }

    /**
     * 将 BSON 可序列化值转为文档。
     *
     * @param value 值
     * @return 文档
     */
    public static Document asDocument(Object value) {
        if (value instanceof Document document) {
            return document;
        }
        if (value instanceof Map<?, ?> map) {
            Document document = new Document();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                document.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return document;
        }
        if (value instanceof org.bson.BsonDocument bsonDocument) {
            return Document.parse(bsonDocument.toJson());
        }
        return null;
    }

    private static boolean getBoolean(Document document, String key) {
        Object value = document.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    /**
     * 解析索引字段文本。
     * 支持 JSON（{"name": 1}）以及简写（name:1, other:-1）。
     *
     * @param text 字段文本
     * @return 字段文档
     */
    public static Document parseKeys(String text) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        String value = text.trim();
        if (value.startsWith("{")) {
            return Document.parse(value);
        }
        Document keys = new Document();
        for (String item : value.split(",")) {
            String[] pair = item.split(":", 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException("Invalid index key: " + item);
            }
            String field = unquote(pair[0].trim());
            String keyValue = pair[1].trim();
            if (StringUtil.isBlank(field) || StringUtil.isBlank(keyValue)) {
                throw new IllegalArgumentException("Invalid index key: " + item);
            }
            keys.put(field, parseKeyValue(unquote(keyValue)));
        }
        return keys;
    }

    /**
     * 解析 JSON 文档。
     *
     * @param text JSON 文本
     * @return 文档
     */
    public static Document parseDocument(String text) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        return Document.parse(text.trim());
    }

    /**
     * 判断索引字段是否有效。
     *
     * @param keys 索引字段
     * @return 结果
     */
    public static boolean isEmptyKeys(Document keys) {
        return keys == null || keys.isEmpty();
    }

    private static Object parseKeyValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    /**
     * 获取索引字段文本。
     *
     * @return 索引字段文本
     */
    public String keyText() {
        if (this.getKeys() == null || this.getKeys().isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : this.getKeys().entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey()).append(": ").append(formatKeyValue(entry.getValue()));
        }
        return builder.toString();
    }

    private static String formatKeyValue(Object value) {
        if (value instanceof String string) {
            return '"' + string + '"';
        }
        return String.valueOf(value);
    }

    /**
     * 获取部分过滤表达式文本。
     *
     * @return 部分过滤表达式文本
     */
    public String partialFilterText() {
        return this.getPartialFilterExpression() == null ? "" : this.getPartialFilterExpression().toJson();
    }

    /**
     * 是否为 _id 索引。
     *
     * @return 结果
     */
    public boolean isIdIndex() {
        return StringUtil.equals("_id_", this.getName());
    }

    /**
     * 是否有效。
     *
     * @return 结果
     */
    public boolean isInvalid() {
        return StringUtil.isBlank(this.getName()) || isEmptyKeys(this.getKeys());
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public Document getKeys() {
        return keys;
    }

    public void setKeys(Document keys) {
        this.keys = keys;
        super.putOriginalData("keys", keys);
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
        super.putOriginalData("unique", unique);
    }

    public boolean isSparse() {
        return sparse;
    }

    public void setSparse(boolean sparse) {
        this.sparse = sparse;
        super.putOriginalData("sparse", sparse);
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
        super.putOriginalData("background", background);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        super.putOriginalData("hidden", hidden);
    }

    public Long getExpireAfterSeconds() {
        return expireAfterSeconds;
    }

    public void setExpireAfterSeconds(Long expireAfterSeconds) {
        this.expireAfterSeconds = expireAfterSeconds;
        super.putOriginalData("expireAfterSeconds", expireAfterSeconds);
    }

    public Document getPartialFilterExpression() {
        return partialFilterExpression;
    }

    public void setPartialFilterExpression(Document partialFilterExpression) {
        this.partialFilterExpression = partialFilterExpression;
        super.putOriginalData("partialFilterExpression", partialFilterExpression);
    }

    @Override
    public void copy(MongoIndex index) {
        if (index != null) {
            this.setName(index.name);
            this.setKeys(index.keys);
            this.setUnique(index.unique);
            this.setSparse(index.sparse);
            this.setBackground(index.background);
            this.setHidden(index.hidden);
            this.setExpireAfterSeconds(index.expireAfterSeconds);
            this.setPartialFilterExpression(index.partialFilterExpression);
        }
    }
}
