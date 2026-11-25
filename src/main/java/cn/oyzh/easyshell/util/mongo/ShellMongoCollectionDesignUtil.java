package cn.oyzh.easyshell.util.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.collection.MongoCollectionOptions;
import cn.oyzh.easyshell.mongo.index.MongoIndex;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.util.List;

/**
 * MongoDB 集合设计工具。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoCollectionDesignUtil {

    /**
     * JSON 输出配置
     */
    private static final JsonWriterSettings JSON_SETTINGS = JsonWriterSettings.builder()
            .indent(true)
            .build();

    /**
     * 解析校验器 JSON。
     *
     * @param text JSON 文本
     * @return 校验器
     */
    public static Document parseValidator(String text) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        return Document.parse(text.trim());
    }

    /**
     * 将校验器转换为 JSON 文本。
     *
     * @param validator 校验器
     * @return JSON 文本
     */
    public static String validatorText(Document validator) {
        return validator == null ? "" : validator.toJson(JSON_SETTINGS);
    }

    /**
     * 生成创建集合脚本。
     *
     * @param options 集合参数
     * @param indexes 索引列表
     * @return 脚本
     */
    public static String toCreateCollectionScript(MongoCollectionOptions options, List<MongoIndex> indexes) {
        String collectionName = StringUtil.isBlank(options.getName()) ? "unnamed_collection" : options.getName();
        StringBuilder builder = new StringBuilder();
        Document createOptions = toCreateCollectionOptions(options);
        builder.append("db.createCollection(")
                .append(quote(collectionName));
        if (!createOptions.isEmpty()) {
            builder.append(", ").append(createOptions.toJson(JSON_SETTINGS));
        }
        builder.append(");\n");
        if (indexes != null) {
            for (MongoIndex index : indexes) {
                if (index == null || index.isInvalid() || index.isIdIndex()) {
                    continue;
                }
                builder.append(toCreateIndexScript(collectionName, index)).append('\n');
            }
        }
        return builder.toString();
    }

    /**
     * 生成修改集合校验器脚本。
     *
     * @param options 集合参数
     * @return 脚本
     */
    public static String toCollModScript(MongoCollectionOptions options) {
        Document command = new Document("collMod", options.getName());
        command.append("validator", options.getValidator() == null
                ? new Document() : options.getValidator());
        if (StringUtil.isNotBlank(options.getValidationLevel())) {
            command.append("validationLevel", options.getValidationLevel());
        }
        if (StringUtil.isNotBlank(options.getValidationAction())) {
            command.append("validationAction", options.getValidationAction());
        }
        return "db.runCommand(" + command.toJson(JSON_SETTINGS) + ");\n";
    }

    /**
     * 生成创建索引脚本。
     *
     * @param collectionName 集合名称
     * @param index          索引
     * @return 脚本
     */
    public static String toCreateIndexScript(String collectionName, MongoIndex index) {
        Document options = toIndexOptions(index);
        StringBuilder builder = new StringBuilder();
        builder.append("db.getCollection(").append(quote(collectionName)).append(").createIndex(")
                .append(index.getKeys().toJson(JSON_SETTINGS));
        if (!options.isEmpty()) {
            builder.append(", ").append(options.toJson(JSON_SETTINGS));
        }
        builder.append(");");
        return builder.toString();
    }

    /**
     * 生成删除索引脚本。
     *
     * @param collectionName 集合名称
     * @param indexName      索引名称
     * @return 脚本
     */
    public static String toDropIndexScript(String collectionName, String indexName) {
        return "db.getCollection(" + quote(collectionName) + ").dropIndex(" + quote(indexName) + ");";
    }

    /**
     * 构建创建集合选项。
     *
     * @param options 集合参数
     * @return 集合选项
     */
    public static Document toCreateCollectionOptions(MongoCollectionOptions options) {
        Document document = new Document();
        if (options.isCapped()) {
            document.append("capped", true);
        }
        if (options.getSizeInBytes() != null) {
            document.append("size", options.getSizeInBytes());
        }
        if (options.getMaxDocuments() != null) {
            document.append("max", options.getMaxDocuments());
        }
        if (options.getValidator() != null) {
            document.append("validator", options.getValidator());
            if (StringUtil.isNotBlank(options.getValidationLevel())) {
                document.append("validationLevel", options.getValidationLevel());
            }
            if (StringUtil.isNotBlank(options.getValidationAction())) {
                document.append("validationAction", options.getValidationAction());
            }
        }
        return document;
    }

    /**
     * 构建索引选项。
     *
     * @param index 索引
     * @return 索引选项
     */
    public static Document toIndexOptions(MongoIndex index) {
        Document document = new Document();
        if (StringUtil.isNotBlank(index.getName())) {
            document.append("name", index.getName());
        }
        if (index.isUnique()) {
            document.append("unique", true);
        }
        if (index.isSparse()) {
            document.append("sparse", true);
        }
        if (index.isBackground()) {
            document.append("background", true);
        }
        if (index.isHidden()) {
            document.append("hidden", true);
        }
        if (index.getExpireAfterSeconds() != null) {
            document.append("expireAfterSeconds", index.getExpireAfterSeconds());
        }
        if (index.getPartialFilterExpression() != null
                && !index.getPartialFilterExpression().isEmpty()) {
            document.append("partialFilterExpression", index.getPartialFilterExpression());
        }
        return document;
    }

    private static String quote(String value) {
        String text = value == null ? "" : value;
        return "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
