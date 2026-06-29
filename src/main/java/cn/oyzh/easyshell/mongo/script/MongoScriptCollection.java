package cn.oyzh.easyshell.mongo.script;

import cn.oyzh.common.json.JSONUtil;
import com.mongodb.MongoNamespace;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MongoScriptCollection {

    private final String dbName;

    private final String collectionName;

    private final MongoCollection<Document> collection;

    public MongoScriptCollection(MongoCollection<Document> collection) {
        this.dbName = collection.getNamespace().getDatabaseName();
        this.collectionName = collection.getNamespace().getCollectionName();
        this.collection = collection;
    }

    public MongoScriptFindCursor find() {
        return this.find(null);
    }

    public MongoScriptFindCursor find(Object doc) {
        Document filter = MongoScriptUtil.toDocumentOrDefault(doc);
        FindIterable<Document> iter = this.collection.find(filter);
        return new MongoScriptFindCursor(this.dbName, this.collectionName, iter);
    }

    public Object insert(Object doc) {
        if (doc instanceof ScriptObjectMirror mirror && mirror.isArray()) {
            return this.insertMany(mirror.values());
        }
        if (doc instanceof Collection<?> c) {
            return this.insertMany(c);
        }
        return this.insertOne(doc);
    }

    public InsertOneResult insertOne(Object doc) {
        Document document = MongoScriptUtil.toDocument(doc);
        return document != null ? this.collection.insertOne(document) : null;
    }

    public InsertManyResult insertMany(Object doc) {
        if (doc instanceof ScriptObjectMirror mirror) {
            return this.insertMany(mirror.values());
        }
        if (doc instanceof Collection<?> collection) {
            List<Document> list = new ArrayList<>();
            for (Object o : collection) {
                Document d = MongoScriptUtil.toDocument(o);
                if (d != null) {
                    list.add(d);
                }
            }
            return this.collection.insertMany(list);
        }
        return null;
    }

    public DeleteResult delete(Object doc) {
        return this.deleteOne(doc);
    }

    public DeleteResult deleteOne(Object doc) {
        Document filter = MongoScriptUtil.toDocument(doc);
        return filter != null ? this.collection.deleteOne(filter) : null;
    }

    public DeleteResult deleteMany() {
        return this.deleteMany(null);
    }

    public DeleteResult deleteMany(Object doc) {
        Document filter = MongoScriptUtil.toDocumentOrDefault(doc);
        return this.collection.deleteMany(filter);
    }

    public UpdateResult update(Object filter, Object doc) {
        return this.updateOne(filter, doc);
    }

    public UpdateResult updateOne(Object filter, Object doc) {
        Document filterDoc = MongoScriptUtil.toDocument(filter);
        Document updateDoc = MongoScriptUtil.toDocument(doc);
        if (filterDoc != null && updateDoc != null) {
            return this.collection.updateOne(filterDoc, updateDoc);
        }
        return null;
    }

    public void drop() {
        this.collection.drop();
    }

    // --- findOne ---

    public Document findOne() {
        return this.collection.find().first();
    }

    public Document findOne(Object filter) {
        Document f = MongoScriptUtil.toDocument(filter);
        return f != null ? this.collection.find(f).first() : this.collection.find().first();
    }

    // --- findOneAndDelete ---

    public Document findOneAndDelete(Object filter) {
        Document f = MongoScriptUtil.toDocument(filter);
        return f != null ? this.collection.findOneAndDelete(f) : null;
    }

    // --- findOneAndReplace ---

    public Document findOneAndReplace(Object filter, Object replacement) {
        Document f = MongoScriptUtil.toDocument(filter);
        Document r = MongoScriptUtil.toDocument(replacement);
        if (f != null && r != null) {
            return this.collection.findOneAndReplace(f, r);
        }
        return null;
    }

    // --- findOneAndUpdate ---

    public Document findOneAndUpdate(Object filter, Object update) {
        Document f = MongoScriptUtil.toDocument(filter);
        Document u = MongoScriptUtil.toDocument(update);
        if (f != null && u != null) {
            return this.collection.findOneAndUpdate(f, u);
        }
        return null;
    }

    // --- updateMany ---

    public UpdateResult updateMany(Object filter, Object update) {
        Document f = MongoScriptUtil.toDocument(filter);
        Document u = MongoScriptUtil.toDocument(update);
        if (f != null && u != null) {
            return this.collection.updateMany(f, u);
        }
        return null;
    }

    // --- replaceOne ---

    public UpdateResult replaceOne(Object filter, Object replacement) {
        Document f = MongoScriptUtil.toDocument(filter);
        Document r = MongoScriptUtil.toDocument(replacement);
        if (f != null && r != null) {
            return this.collection.replaceOne(f, r);
        }
        return null;
    }

    public UpdateResult replaceOne(Object filter, Object replacement, Object option) {
        Document f = MongoScriptUtil.toDocument(filter);
        Document r = MongoScriptUtil.toDocument(replacement);
        if (f != null && r != null && option instanceof Map<?, ?> o) {
            ReplaceOptions options = JSONUtil.toBean(o, ReplaceOptions.class);
            return this.collection.replaceOne(f, r, options);
        }
        return null;
    }

    // --- countDocuments ---

    public long countDocuments() {
        return this.collection.countDocuments();
    }

    public long countDocuments(Object filter) {
        Document f = MongoScriptUtil.toDocument(filter);
        return f != null ? this.collection.countDocuments(f) : this.collection.countDocuments();
    }

    // --- estimatedDocumentCount ---

    public long estimatedDocumentCount() {
        return this.collection.estimatedDocumentCount();
    }

    // --- distinct ---

    public MongoScriptCursor distinct(String fieldName) {
        return new MongoScriptCursor(this.collection.distinct(fieldName, String.class));
    }

    public MongoScriptCursor distinct(String fieldName, Object filter) {
        Document f = MongoScriptUtil.toDocument(filter);
        if (f != null) {
            return new MongoScriptCursor(this.collection.distinct(fieldName, f, String.class));
        }
        return new MongoScriptCursor(this.collection.distinct(fieldName, String.class));
    }

    // --- aggregate ---

    public MongoScriptCursor aggregate(Object pipeline) {
        List<Document> stages = MongoScriptUtil.toDocumentList(pipeline);
        AggregateIterable<Document> iter = this.collection.aggregate(stages);
        iter.allowDiskUse(true);
        return new MongoScriptCursor(iter);
    }

    // --- indexes ---

    public String createIndex(Object keys) {
        return this.createIndex(keys, null);
    }

    public String createIndex(Object keys, Object options) {
        Document k = MongoScriptUtil.toDocument(keys);
        if (k == null) {
            return null;
        }
        IndexOptions opts = new IndexOptions();
        if (options instanceof Map optMap) {
            if (optMap.containsKey("name")) {
                opts.name(optMap.get("name").toString());
            }
            if (optMap.containsKey("unique")) {
                opts.unique(Boolean.parseBoolean(optMap.get("unique").toString()));
            }
            if (optMap.containsKey("background")) {
                opts.background(Boolean.parseBoolean(optMap.get("background").toString()));
            }
            if (optMap.containsKey("sparse")) {
                opts.sparse(Boolean.parseBoolean(optMap.get("sparse").toString()));
            }
            if (optMap.containsKey("expireAfterSeconds")) {
                opts.expireAfter(Long.parseLong(optMap.get("expireAfterSeconds").toString()), TimeUnit.SECONDS);
            }
        }
        return this.collection.createIndex(k, opts);
    }

    public MongoScriptCursor listIndexes() {
        return new MongoScriptCursor(this.collection.listIndexes());
    }

    public void dropIndex(Object keys) {
        Document k = MongoScriptUtil.toDocument(keys);
        if (k != null) {
            this.collection.dropIndex(k);
        }
    }

    public void dropIndexByName(String name) {
        this.collection.dropIndex(name);
    }

    public void dropIndexes() {
        this.collection.dropIndexes();
    }

    // --- rename ---

    public void rename(String newName) {
        this.collection.renameCollection(new MongoNamespace(this.dbName, newName));
    }
}
