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
import cn.oyzh.easyshell.event.mongo.user.ShellMongoUserDeletedEvent;
import cn.oyzh.easyshell.event.mongo.user.ShellMongoUserViewEvent;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.database.MongoDatabase;
import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.trees.mongo.bucket.ShellMongoBucketTreeItem;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.function.ShellMongoFunctionTreeItem;
import cn.oyzh.easyshell.trees.mongo.query.ShellMongoQueryTreeItem;
import cn.oyzh.easyshell.trees.mongo.root.ShellMongoRootTreeItem;
import cn.oyzh.easyshell.trees.mongo.user.ShellMongoUserTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * mongodb事件工具
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoEventUtil {

    /**
     * 数据库关闭事件
     *
     * @param dbItem 数据库节点
     */
    public static void databaseClosed(ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoDatabaseClosedEvent event = new ShellMongoDatabaseClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 数据库新增事件
     *
     * @param connectItem 连接节点
     * @param database    数据库
     */
    public static void databaseAdded(ShellMongoRootTreeItem connectItem, MongoDatabase database) {
        ShellMongoDatabaseAddedEvent event = new ShellMongoDatabaseAddedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 数据库更新事件
     *
     * @param connectItem 连接节点
     * @param database    数据库
     */
    public static void databaseUpdated(ShellMongoRootTreeItem connectItem, MongoDatabase database) {
        ShellMongoDatabaseUpdatedEvent event = new ShellMongoDatabaseUpdatedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 数据库删除事件
     *
     * @param dbItem 数据库节点
     */
    public static void databaseDropped(ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoDatabaseDroppedEvent event = new ShellMongoDatabaseDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 查询新增事件
     *
     * @param item 数据库节点
     */
    public static void queryAdd(ShellMongoDatabaseTreeItem item) {
        ShellMongoQueryAddEvent event = new ShellMongoQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询已新增事件
     *
     * @param query 查询
     * @param item  数据库节点
     */
    public static void queryAdded(ShellQuery query, ShellMongoDatabaseTreeItem item) {
        ShellMongoQueryAddedEvent event = new ShellMongoQueryAddedEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    /**
     * 查询删除事件
     *
     * @param item 查询节点
     */
    public static void queryDeleted(ShellMongoQueryTreeItem item) {
        ShellMongoQueryDeletedEvent event = new ShellMongoQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询打开事件
     *
     * @param query 查询
     * @param item  数据库节点
     */
    public static void queryOpen(ShellQuery query, ShellMongoDatabaseTreeItem item) {
        ShellMongoQueryOpenEvent event = new ShellMongoQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    /**
     * 查询重命名事件
     *
     * @param item      查询节点
     * @param queryName    查询名称
     */
    public static void queryRenamed(ShellMongoQueryTreeItem item, String queryName) {
        ShellMongoQueryRenamedEvent event = new ShellMongoQueryRenamedEvent();
        event.data(item);
        event.setQueryName(queryName);
        EventUtil.post(event);
    }

    /**
     * 集合删除事件
     *
     * @param collectionItem 集合节点
     * @param dbItem         数据库节点
     */
    public static void collectionDropped(ShellMongoCollectionTreeItem collectionItem, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoCollectionDroppedEvent event = new ShellMongoCollectionDroppedEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 集合打开事件
     *
     * @param collectionItem 集合节点
     * @param dbItem         数据库节点
     */
    public static void collectionOpen(ShellMongoCollectionTreeItem collectionItem, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoCollectionOpenEvent event = new ShellMongoCollectionOpenEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 集合重命名事件
     *
     * @param collectionName    集合名称
     * @param newCollectionName 新集合名称
     * @param dbItem            数据库节点
     */
    public static void collectionRenamed(String collectionName, String newCollectionName, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoCollectionRenamedEvent event = new ShellMongoCollectionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(collectionName);
        event.setNewCollectionName(newCollectionName);
        EventUtil.post(event);
    }

    /**
     * 桶删除事件
     *
     * @param collectionItem 桶节点
     * @param dbItem         数据库节点
     */
    public static void bucketDropped(ShellMongoBucketTreeItem collectionItem, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoBucketDroppedEvent event = new ShellMongoBucketDroppedEvent();
        event.data(collectionItem);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 桶打开事件
     *
     * @param collectionItem 桶节点
     * @param dbItem         数据库节点
     */
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
     * @param dbName 数据库名称
     */
    public static void terminalOpen(ShellMongoClient client, String dbName) {
        ShellMongoTerminalOpenEvent event = new ShellMongoTerminalOpenEvent();
        event.data(client);
        event.setDbName(dbName);
        EventUtil.post(event);
    }

    /**
     * 函数删除事件
     *
     * @param treeItem 函数节点
     */
    public static void dropFunction(ShellMongoFunctionTreeItem treeItem) {
        ShellMongoFunctionDroppedEvent event = new ShellMongoFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 函数设计事件
     *
     * @param function 函数
     * @param dbItem   数据库节点
     */
    public static void designFunction(MongoFunction function, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoFunctionDesignEvent event = new ShellMongoFunctionDesignEvent();
        event.data(function);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 函数重命名事件
     *
     * @param functionName    函数名称
     * @param newFunctionName 新函数名称
     * @param dbItem          数据库节点
     */
    public static void functionRenamed(String functionName, String newFunctionName, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoFunctionRenamedEvent event = new ShellMongoFunctionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(functionName);
        event.setNewFunctionName(newFunctionName);
        EventUtil.post(event);
    }

    /**
     * 用户查看事件
     *
     * @param mongoUser 用户
     * @param dbItem    数据库节点
     */
    public static void userView(MongoUser mongoUser,ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoUserViewEvent event = new ShellMongoUserViewEvent();
        event.data(mongoUser);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 用户删除事件
     *
     * @param userTreeItem 用户节点
     */
    public static void userDeleted(ShellMongoUserTreeItem userTreeItem) {
        ShellMongoUserDeletedEvent event = new ShellMongoUserDeletedEvent();
        event.data(userTreeItem);
        EventUtil.post(event);
    }
}
