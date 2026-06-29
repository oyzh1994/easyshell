package cn.oyzh.easyshell.mongo.script;

import com.mongodb.client.FindIterable;
import org.bson.Document;

/**
 *
 * @author oyzh
 * @since 2026-06-08
 */
public class MongoScriptFindCursor extends MongoScriptCursor {

    private final String dbName;

    private final String collectionName;

    private final FindIterable<Document> cursor;

    public MongoScriptFindCursor(String dbName, String collectionName, FindIterable<Document> cursor) {
        super(cursor);
        this.dbName = dbName;
        this.collectionName = collectionName;
        this.cursor = cursor;
    }

    public String getDbName() {
        return dbName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public MongoScriptFindCursor limit(int n) {
        return new MongoScriptFindCursor(this.dbName, this.collectionName, this.cursor.limit(n));
    }

    public MongoScriptFindCursor skip(int n) {
        return new MongoScriptFindCursor(this.dbName, this.collectionName, this.cursor.skip(n));
    }

    public Document explain() {
       return this.cursor.explain();
    }
}