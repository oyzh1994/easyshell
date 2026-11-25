package cn.oyzh.easyshell.mongo.view;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MongoDB 视图。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class MongoView implements ObjectComparator<MongoView>, ObjectCopier<MongoView> {

    /**
     * 视图名称
     */
    private String name;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 源集合或视图名称
     */
    private String viewOn;

    /**
     * 聚合管道
     */
    private List<Document> pipeline;

    /**
     * 从 MongoDB listCollections 文档构建视图对象。
     *
     * @param info listCollections 文档
     * @return 视图对象
     */
    public static MongoView from(Document info) {
        MongoView view = new MongoView();
        if (info == null) {
            return view;
        }
        view.setName(info.getString("name"));
        Document options = info.get("options", Document.class);
        if (options != null) {
            view.setViewOn(options.getString("viewOn"));
            view.setPipeline(toDocumentList(options.get("pipeline")));
        }
        return view;
    }

    /**
     * 转换为聚合管道文档列表。
     *
     * @param value 值
     * @return 聚合管道
     */
    public static List<Document> toDocumentList(Object value) {
        List<Document> pipeline = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                Document document = toDocument(item);
                if (document != null) {
                    pipeline.add(document);
                }
            }
        }
        return pipeline;
    }

    private static Document toDocument(Object value) {
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
        if (value instanceof org.bson.BsonDocument document) {
            return Document.parse(document.toJson());
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getViewOn() {
        return viewOn;
    }

    public void setViewOn(String viewOn) {
        this.viewOn = viewOn;
    }

    public List<Document> getPipeline() {
        return pipeline;
    }

    public void setPipeline(List<Document> pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * 是否为新视图。
     *
     * @return 是否为新视图
     */
    public boolean isNew() {
        return StringUtil.isBlank(this.name);
    }

    @Override
    public boolean compare(MongoView view) {
        if (view == null) {
            return false;
        }
        if (view == this) {
            return true;
        }
        return StringUtil.equals(this.name, view.name)
                && StringUtil.equals(this.dbName, view.dbName);
    }

    @Override
    public void copy(MongoView view) {
        if (view != null) {
            this.setName(view.name);
            this.setDbName(view.dbName);
            this.setViewOn(view.viewOn);
            this.setPipeline(view.pipeline);
        }
    }
}
