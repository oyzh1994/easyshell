package cn.oyzh.easyshell.event.mongo;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mongo.bucket.MongoBucketDroppedEvent;
import cn.oyzh.easyshell.event.mongo.bucket.MongoBucketOpenEvent;
import cn.oyzh.easyshell.event.mongo.collection.MongoCollectionDroppedEvent;
import cn.oyzh.easyshell.event.mongo.collection.MongoCollectionOpenEvent;
import cn.oyzh.easyshell.event.mongo.collection.MongoCollectionRenamedEvent;
import cn.oyzh.easyshell.event.mongo.database.MongoDatabaseAddedEvent;
import cn.oyzh.easyshell.event.mongo.database.MongoDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mongo.database.MongoDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mongo.database.MongoDatabaseUpdatedEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionDesignEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionRenamedEvent;
import cn.oyzh.easyshell.event.mongo.query.MongoQueryAddEvent;
import cn.oyzh.easyshell.event.mongo.query.MongoQueryAddedEvent;
import cn.oyzh.easyshell.event.mongo.query.MongoQueryDeletedEvent;
import cn.oyzh.easyshell.event.mongo.query.MongoQueryOpenEvent;
import cn.oyzh.easyshell.event.mongo.query.MongoQueryRenamedEvent;
import cn.oyzh.easyshell.event.mongo.terminal.MongoTerminalOpenEvent;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.MongoDatabase;
import cn.oyzh.easyshell.mongo.MongoFunction;
import cn.oyzh.easyshell.trees.mongo.bucket.MongoBucketTreeItem;
import cn.oyzh.easyshell.trees.mongo.collection.MongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.function.ShellMongoFunctionTreeItem;
import cn.oyzh.easyshell.trees.mongo.query.MongoQueryTreeItem;
import cn.oyzh.easyshell.trees.mongo.root.ShellMongoRootTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * redis事件工具
 *
 * @author oyzh
 * @since 2023/11/20
 */
public class MongoEventUtil {

    public static void databaseClosed(MongoDatabaseTreeItem dbItem) {
        MongoDatabaseClosedEvent event = new MongoDatabaseClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void databaseAdded(ShellMongoRootTreeItem connectItem, MongoDatabase database) {
        MongoDatabaseAddedEvent event = new MongoDatabaseAddedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void databaseUpdated(ShellMongoRootTreeItem connectItem, MongoDatabase database) {
        MongoDatabaseUpdatedEvent event = new MongoDatabaseUpdatedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void databaseDropped(MongoDatabaseTreeItem dbItem) {
        MongoDatabaseDroppedEvent event = new MongoDatabaseDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void queryAdd(MongoDatabaseTreeItem item) {
        MongoQueryAddEvent event = new MongoQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryAdded(ShellQuery query, MongoDatabaseTreeItem item) {
        MongoQueryAddedEvent event = new MongoQueryAddedEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryDeleted(MongoQueryTreeItem item) {
        MongoQueryDeletedEvent event = new MongoQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryOpen(ShellQuery query, MongoDatabaseTreeItem item) {
        MongoQueryOpenEvent event = new MongoQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryRenamed(String queryId, String queryName, String newQueryName, MongoDatabaseTreeItem item) {
        MongoQueryRenamedEvent event = new MongoQueryRenamedEvent();
        event.data(queryId);
        event.setQueryName(queryName);
        event.setNewQueryName(newQueryName);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void collectionDropped(MongoCollectionTreeItem collectionItem, MongoDatabaseTreeItem dbItem) {
        MongoCollectionDroppedEvent event = new MongoCollectionDroppedEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void collectionOpen(MongoCollectionTreeItem collectionItem, MongoDatabaseTreeItem dbItem) {
        MongoCollectionOpenEvent event = new MongoCollectionOpenEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void collectionRenamed(String collectionName, String newCollectionName, MongoDatabaseTreeItem dbItem) {
        MongoCollectionRenamedEvent event = new MongoCollectionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(collectionName);
        event.setNewCollectionName(newCollectionName);
        EventUtil.post(event);
    }

    public static void bucketDropped(MongoBucketTreeItem collectionItem, MongoDatabaseTreeItem dbItem) {
        MongoBucketDroppedEvent event = new MongoBucketDroppedEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void bucketOpen(MongoBucketTreeItem collectionItem, MongoDatabaseTreeItem dbItem) {
        MongoBucketOpenEvent event = new MongoBucketOpenEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 终端打开事件
     *
     * @param client zk客户端
     */
    public static void terminalOpen(ShellMongoClient client, String dbName) {
        MongoTerminalOpenEvent event = new MongoTerminalOpenEvent();
        event.data(client);
        event.setDbName(dbName);
        EventUtil.post(event);
    }

    public static void dropFunction(ShellMongoFunctionTreeItem treeItem) {
        ShellMongoFunctionDroppedEvent event = new ShellMongoFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void designFunction(MongoFunction function, MongoDatabaseTreeItem dbItem) {
        ShellMongoFunctionDesignEvent event = new ShellMongoFunctionDesignEvent();
        event.data(function);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void functionRenamed(String functionName, String newFunctionName, MongoDatabaseTreeItem dbItem) {
        ShellMongoFunctionRenamedEvent event = new ShellMongoFunctionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(functionName);
        event.setNewFunctionName(newFunctionName);
        EventUtil.post(event);
    }
}
