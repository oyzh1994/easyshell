package cn.oyzh.easyshell.event.mongo;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mongo.bucket.ShellMongoBucketDroppedEvent;
import cn.oyzh.easyshell.event.mongo.bucket.ShellMongoBucketOpenEvent;
import cn.oyzh.easyshell.event.mongo.collection.ShellMongoCollectionDroppedEvent;
import cn.oyzh.easyshell.event.mongo.collection.ShellMongoCollectionOpenEvent;
import cn.oyzh.easyshell.event.mongo.collection.ShellMongoCollectionRenamedEvent;
import cn.oyzh.easyshell.event.mongo.database.ShellMongoDatabaseAddedEvent;
import cn.oyzh.easyshell.event.mongo.database.ShellMongoDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mongo.database.ShellMongoDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mongo.database.ShellMongoDatabaseUpdatedEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionDesignEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionRenamedEvent;
import cn.oyzh.easyshell.event.mongo.query.ShellMongoQueryAddEvent;
import cn.oyzh.easyshell.event.mongo.query.ShellMongoQueryAddedEvent;
import cn.oyzh.easyshell.event.mongo.query.ShellMongoQueryDeletedEvent;
import cn.oyzh.easyshell.event.mongo.query.ShellMongoQueryOpenEvent;
import cn.oyzh.easyshell.event.mongo.query.ShellMongoQueryRenamedEvent;
import cn.oyzh.easyshell.event.mongo.terminal.ShellMongoTerminalOpenEvent;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.database.MongoDatabase;
import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.trees.mongo.bucket.ShellMongoBucketTreeItem;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.function.ShellMongoFunctionTreeItem;
import cn.oyzh.easyshell.trees.mongo.query.ShellMongoQueryTreeItem;
import cn.oyzh.easyshell.trees.mongo.root.ShellMongoRootTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * mongodb事件工具
 *
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellMongoEventUtil {

    public static void databaseClosed(ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoDatabaseClosedEvent event = new ShellMongoDatabaseClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void databaseAdded(ShellMongoRootTreeItem connectItem, MongoDatabase database) {
        ShellMongoDatabaseAddedEvent event = new ShellMongoDatabaseAddedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void databaseUpdated(ShellMongoRootTreeItem connectItem, MongoDatabase database) {
        ShellMongoDatabaseUpdatedEvent event = new ShellMongoDatabaseUpdatedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void databaseDropped(ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoDatabaseDroppedEvent event = new ShellMongoDatabaseDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void queryAdd(ShellMongoDatabaseTreeItem item) {
        ShellMongoQueryAddEvent event = new ShellMongoQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryAdded(ShellQuery query, ShellMongoDatabaseTreeItem item) {
        ShellMongoQueryAddedEvent event = new ShellMongoQueryAddedEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryDeleted(ShellMongoQueryTreeItem item) {
        ShellMongoQueryDeletedEvent event = new ShellMongoQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryOpen(ShellQuery query, ShellMongoDatabaseTreeItem item) {
        ShellMongoQueryOpenEvent event = new ShellMongoQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryRenamed(String queryId, String queryName, String newQueryName, ShellMongoDatabaseTreeItem item) {
        ShellMongoQueryRenamedEvent event = new ShellMongoQueryRenamedEvent();
        event.data(queryId);
        event.setQueryName(queryName);
        event.setNewQueryName(newQueryName);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void collectionDropped(ShellMongoCollectionTreeItem collectionItem, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoCollectionDroppedEvent event = new ShellMongoCollectionDroppedEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void collectionOpen(ShellMongoCollectionTreeItem collectionItem, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoCollectionOpenEvent event = new ShellMongoCollectionOpenEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void collectionRenamed(String collectionName, String newCollectionName, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoCollectionRenamedEvent event = new ShellMongoCollectionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(collectionName);
        event.setNewCollectionName(newCollectionName);
        EventUtil.post(event);
    }

    public static void bucketDropped(ShellMongoBucketTreeItem collectionItem, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoBucketDroppedEvent event = new ShellMongoBucketDroppedEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void bucketOpen(ShellMongoBucketTreeItem collectionItem, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoBucketOpenEvent event = new ShellMongoBucketOpenEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 终端打开事件
     *
     * @param client mongodb客户端
     */
    public static void terminalOpen(ShellMongoClient client, String dbName) {
        ShellMongoTerminalOpenEvent event = new ShellMongoTerminalOpenEvent();
        event.data(client);
        event.setDbName(dbName);
        EventUtil.post(event);
    }

    public static void dropFunction(ShellMongoFunctionTreeItem treeItem) {
        ShellMongoFunctionDroppedEvent event = new ShellMongoFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void designFunction(MongoFunction function, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoFunctionDesignEvent event = new ShellMongoFunctionDesignEvent();
        event.data(function);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void functionRenamed(String functionName, String newFunctionName, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoFunctionRenamedEvent event = new ShellMongoFunctionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(functionName);
        event.setNewFunctionName(newFunctionName);
        EventUtil.post(event);
    }
}
