package cn.oyzh.easyshell.util.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.view.MongoView;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.util.List;

/**
 * MongoDB 视图设计工具。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewDesignUtil {

    private static final JsonWriterSettings JSON_SETTINGS = JsonWriterSettings.builder()
            .indent(true)
            .build();

    /**
     * 解析聚合管道 JSON 数组。
     *
     * @param text JSON 文本
     * @return 聚合管道
     */
    public static List<Document> parsePipeline(String text) {
        if (StringUtil.isBlank(text)) {
            return List.of();
        }
        Document document = Document.parse("{\"pipeline\":" + text.trim() + "}");
        return MongoView.toDocumentList(document.get("pipeline"));
    }

    /**
     * 将聚合管道转换为 JSON 文本。
     *
     * @param pipeline 聚合管道
     * @return JSON 文本
     */
    public static String pipelineText(List<Document> pipeline) {
        if (pipeline == null || pipeline.isEmpty()) {
            return "[]";
        }
        String json = new Document("pipeline", pipeline).toJson(JSON_SETTINGS);
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');
        return start >= 0 && end > start ? json.substring(start, end + 1) : "[]";
    }

    /**
     * 生成视图设计预览脚本。
     *
     * @param view   视图
     * @param isNew 是否新建
     * @return 脚本
     */
    public static String toScript(MongoView view, boolean isNew) {
        String name = StringUtil.isBlank(view.getName()) ? "unnamed_view" : view.getName();
        List<Document> pipeline = view.getPipeline() == null ? List.of() : view.getPipeline();
        if (isNew) {
            return "db.createView(" + quote(name) + ", " + quote(view.getViewOn()) + ", "
                    + pipelineText(pipeline) + ");";
        }
        Document command = new Document("collMod", name)
                .append("viewOn", view.getViewOn())
                .append("pipeline", pipeline);
        return "db.runCommand(" + command.toJson(JSON_SETTINGS) + ");";
    }

    private static String quote(String value) {
        String text = value == null ? "" : value;
        return "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
