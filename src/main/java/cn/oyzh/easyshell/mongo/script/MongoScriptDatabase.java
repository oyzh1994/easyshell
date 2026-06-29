package cn.oyzh.easyshell.mongo.script;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateViewOptions;
import org.bson.Document;

import java.util.List;
import java.util.Map;

public class MongoScriptDatabase {

    private final MongoDatabase database;

    public MongoScriptDatabase(MongoDatabase database) {
        this.database = database;
    }

    public String getName() {
        return this.database.getName();
    }

    public MongoScriptCollection getCollection(String name) {
        MongoCollection<Document> collection = this.database.getCollection(name);
        return new MongoScriptCollection(collection);
    }

    public MongoScriptDatabase createCollection(String name) {
        this.database.createCollection(name);
        return this;
    }

    public MongoScriptDatabase createCollection(String name, Object options) {
        CreateCollectionOptions opts = new CreateCollectionOptions();
        if (options instanceof Map optMap) {
            if (optMap.containsKey("capped")) {
                opts.capped(Boolean.parseBoolean(optMap.get("capped").toString()));
            }
            if (optMap.containsKey("sizeInBytes")) {
                opts.sizeInBytes(Long.parseLong(optMap.get("sizeInBytes").toString()));
            }
            if (optMap.containsKey("maxDocuments")) {
                opts.maxDocuments(Long.parseLong(optMap.get("maxDocuments").toString()));
            }
        }
        this.database.createCollection(name, opts);
        return this;
    }

    public void drop() {
        this.database.drop();
    }

    public Document runCommand(Object command) {
        if (command instanceof Map map) {
            return this.database.runCommand(new Document(map));
        }
        return null;
    }

    public void createView(String name, String viewOn, Object pipeline) {
        List<Document> stages = MongoScriptUtil.toDocumentList(pipeline);
        this.database.createView(name, viewOn, stages);
    }

    public void createView(String name, String viewOn, Object pipeline, Object options) {
        List<Document> stages = MongoScriptUtil.toDocumentList(pipeline);
        CreateViewOptions opts = new CreateViewOptions();
        this.database.createView(name, viewOn, stages, opts);
    }

    public MongoScriptCursor aggregate(Object pipeline) {
        List<Document> stages = MongoScriptUtil.toDocumentList(pipeline);
        AggregateIterable<Document> iter = this.database.aggregate(stages);
        iter.allowDiskUse(true);
        return new MongoScriptCursor(iter);
    }

    public MongoScriptCursor watch() {
        ChangeStreamIterable<Document> iter = this.database.watch();
        return new MongoScriptCursor(iter);
    }

    public MongoScriptCursor watch(Object pipeline) {
        List<Document> stages = MongoScriptUtil.toDocumentList(pipeline);
        ChangeStreamIterable<Document> iter = this.database.watch(stages);
        return new MongoScriptCursor(iter);
    }

    public MongoScriptCursor listCollectionNames() {
        MongoIterable<String> iter = this.database.listCollectionNames();
        return new MongoScriptCursor(iter);
    }

    public MongoScriptCursor listCollections() {
        ListCollectionsIterable<Document> iter = this.database.listCollections();
        return new MongoScriptCursor(iter);
    }
}
