package cn.oyzh.easyshell.mongo;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.mongo.MongoException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.mongo.condition.MongoConditionUtil;
import cn.oyzh.easyshell.mongo.script.MongoScriptCursor;
import cn.oyzh.easyshell.mongo.script.MongoScriptEngine;
import cn.oyzh.easyshell.mongo.script.MongoScriptFindCursor;
import cn.oyzh.easyshell.mongo.script.MongoScriptParser;
import cn.oyzh.easyshell.query.mongo.MongoExecuteResult;
import cn.oyzh.easyshell.query.mongo.MongoQueryResults;
import cn.oyzh.easyshell.util.mongo.MongoRecordUtil;
import cn.oyzh.easyshell.util.mongo.MongoUtil;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Code;
import org.bson.types.ObjectId;

import javax.script.ScriptException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * db客户端封装
 *
 * @author oyzh
 * @since 2023/11/06
 */
public class ShellMongoClient implements ShellBaseClient {

    /**
     * db信息
     */
    protected ShellConnect shellConnect;

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

    public ShellMongoClient(ShellConnect value) {
        this.shellConnect = value;
        this.addStateListener(this.stateListener);
    }

    public boolean isConnected() {
        return this.state.get() != null && this.state.get().isConnected();
    }

    public boolean isConnecting() {
        return this.state.get() == ShellConnState.CONNECTING;
    }

    @Override
    public void close() {
        try {
            if (this.mongoClient != null) {
                this.mongoClient.close();
            }
            this.removeStateListener(this.stateListener);
            this.state.set(ShellConnState.CLOSED);
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("Zookeeper client close error.", ex);
        }
    }

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    /**
     * 初始化连接
     *
     * @return 连接
     */
    private String initHost() {
        // 连接地址
        String host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
        return host;
    }

    /**
     * mongo客户端
     */
    private com.mongodb.client.MongoClient mongoClient;

    /**
     * 脚本引擎
     */
    private MongoScriptEngine engine;

    /**
     * 创建shell引擎
     *
     * @return 结果
     */
    public MongoScriptEngine shellEngine() {
        if (this.engine == null) {
            this.engine = new MongoScriptEngine(this.mongoClient);
        }
        return this.engine;
    }

    /**
     * 初始化客户端
     *
     * @param timeoutMs 超时时间
     */
    private void initClient(int timeoutMs) {
        // 连接信息
        String host = this.initHost();
        String hostIp = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applyToClusterSettings(b -> b
                        .hosts(Collections.singletonList(new ServerAddress(hostIp, port)))
                        .serverSelectionTimeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS))
                .applyToSocketSettings(b -> b
                        .connectTimeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS));
        // 密码认证
        if (this.shellConnect.isMongoPasswordAuth()) {
            String user = this.shellConnect.getUser();
            String database = this.shellConnect.getMongoAuthDatabase();
            String password = this.shellConnect.getPassword();
            MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
            builder.credential(credential);
        }
        this.mongoClient = MongoClients.create(builder.build());
    }

    @Override
    public void start(int timeout) throws Throwable {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        // 初始化客户端
        this.initClient(timeout);
        try {
            // 开始连接时间
            final AtomicLong starTime = new AtomicLong(System.currentTimeMillis());
            // 更新连接状态
            this.state.set(ShellConnState.CONNECTING);
            // 检查连接（需迭代才能触发实际网络请求和认证）
            if (this.shellConnect.getMongoAuthDatabase() != null) {
                this.mongoClient.getDatabase(this.shellConnect.getMongoAuthDatabase()).listCollections().first();
            } else {
                this.mongoClient.listDatabases().first();
            }
            // 更新连接状态
            this.state.set(ShellConnState.CONNECTED);
            // 开始连接时间
            starTime.set(System.currentTimeMillis());
        } catch (Exception ex) {
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("Mongo client start error", ex);
            throw new MongoException(ex);
        }
    }

    public MongoDatabase database(String dbName) {
        MongoDatabase database1 = new MongoDatabase();
        database1.setName(dbName);
        return database1;
    }

    /**
     * 列举数据库
     *
     * @return 结果
     */
    public List<MongoDatabase> listDatabases() {
        List<MongoDatabase> databases = new ArrayList<>();
        try {
            ListDatabasesIterable<Document> documents = this.mongoClient.listDatabases();
            for (Document document : documents) {
                MongoDatabase database = new MongoDatabase();
                String name = document.getString("name");
                database.setName(name);
                Object sizeOnDisk = document.get("sizeOnDisk");
                if (sizeOnDisk instanceof Number number) {
                    database.setSizeOnDisk(number.doubleValue());
                }
                databases.add(database);
            }
        } catch (Exception ex) {
            if (ExceptionUtil.hasMessage(ex, "not authorized") && this.shellConnect.getMongoAuthDatabase() != null) {
                MongoDatabase database = new MongoDatabase();
                database.setName(this.shellConnect.getMongoAuthDatabase());
                databases.add(database);
            }
        }
        if (databases.isEmpty() && this.shellConnect.getMongoAuthDatabase() != null) {
            MongoDatabase database = new MongoDatabase();
            database.setName(this.shellConnect.getMongoAuthDatabase());
            databases.add(database);
        }
        return databases;
    }

    /**
     * 列举数据库名称
     *
     * @return 结果
     */
    public List<String> listDatabaseNames() {
        List<String> list = new ArrayList<>();
        try {
            MongoIterable<String> iterable = this.mongoClient.listDatabaseNames();
            for (String s : iterable) {
                list.add(s);
            }
        } catch (Exception ex) {
            if (ExceptionUtil.hasMessage(ex, "not authorized") && this.shellConnect.getMongoAuthDatabase() != null) {
                list.add(this.shellConnect.getMongoAuthDatabase());
            }
        }
        if (list.isEmpty() && this.shellConnect.getMongoAuthDatabase() != null) {
            list.add(this.shellConnect.getMongoAuthDatabase());
        }
        return list;
    }

    private com.mongodb.client.MongoCollection<Document> collection(String dbName, String collectionName) {
        return this.mongoClient.getDatabase(dbName).getCollection(collectionName);
    }

    public void createDatabase(String dbName) {
        com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
        database.createCollection("_empty_");
    }

    public boolean existDatabase(String dbName) {
        try {
            com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
            MongoIterable<String> iterable = database.listCollectionNames();
            return iterable.first() != null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean alterDatabase(MongoDatabase database) {
        return false;
    }

    public boolean dropDatabase(String dbName) {
        try {
            com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
            database.drop();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 删除集合
     *
     * @param dbName         数据库名称
     * @param collectionName 集合名称
     */
    public void dropCollection(String dbName, String collectionName) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, collectionName);
        collection.drop();
    }

    /**
     * 列举集合
     *
     * @param dbName 数据库名称
     * @return 结果
     */
    public List<MongoCollection> listCollections(String dbName) {
        com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
        List<MongoCollection> collections = new ArrayList<>();
        for (String name : database.listCollectionNames()) {
            if (!MongoRecordUtil.isCollection(name)) {
                continue;
            }
            MongoCollection collection = new MongoCollection();
            collection.setDbName(dbName);
            collection.setName(name);
            collections.add(collection);
        }
        collections = collections.stream().sorted(Comparator.comparing(MongoCollection::getName)).toList();
        return collections;
    }

    /**
     * 列举集合名称
     *
     * @param dbName 数据库名称
     * @return 结果
     */
    public List<String> listCollectionNames(String dbName) {
        com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
        MongoIterable<String> iterable = database.listCollectionNames();
        List<String> list = new ArrayList<>();
        for (String name : iterable) {
            if (MongoRecordUtil.isCollection(name)) {
                list.add(name);
            }
        }
        return list;
    }

    /**
     * 查询集合记录
     *
     * @param param 参数
     * @return 结果
     */
    public List<MongoRecord> selectCollectionRecords(MongoSelectRecordParam param) {
        String dbName = param.getDbName();
        String collectionName = param.getCollectionName();
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, collectionName);
        Bson filters = MongoConditionUtil.buildCondition(param.getFilters());
        FindIterable<Document> iterable = collection.find(filters);
        if (param.getStart() != null) {
            int skip = Math.toIntExact(param.getStart());
            iterable = iterable.skip(skip);
        }
        if (param.getLimit() != null) {
            int limit = Math.toIntExact(param.getLimit());
            iterable = iterable.limit(limit);
        }
        return MongoRecordUtil.docToRecord(dbName, collectionName, iterable);
    }

    /**
     * 查询集合记录
     *
     * @param dbName         数据库
     * @param collectionName 集合
     * @param id             对象id
     * @return 结果
     */
    public MongoRecord selectCollectionRecord(String dbName, String collectionName, Object id) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, collectionName);
        Bson filters = Filters.eq(MongoUtil.ID, id);
        FindIterable<Document> iterable = collection.find(filters);
        Document document = iterable.first();
        if (document == null) {
            return null;
        }
        return MongoRecordUtil.docToRecord(dbName, collectionName, document);

    }

    /**
     * 查询集合记录数量
     *
     * @param param 参数
     * @return 结果
     */
    public long selectCollectionRecordCount(MongoSelectRecordParam param) {
        String dbName = param.getDbName();
        String collectionName = param.getCollectionName();
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, collectionName);
        Bson filters = MongoConditionUtil.buildCondition(param.getFilters());
        return collection.countDocuments(filters);
    }

    /**
     * 新增集合记录
     *
     * @param record 数据
     * @return 结果
     */
    public BsonValue insertCollectionRecord(MongoRecord record) {
        String dbName = record.getColumns().getFirst().getDbName();
        String collectionName = record.getColumns().getFirst().getCollectionName();
        Document document = new Document();
        for (MongoColumn column : record.getColumns()) {
            Object value = record.getValue(column.getName());
            if (column.is_id() && value == null) {
                continue;
            }
            document.append(column.getName(), value);
        }
        if (dbName != null) {
            com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
            com.mongodb.client.MongoCollection<Document> collection = database.getCollection(collectionName);
            InsertOneResult result = collection.insertOne(document);
            return result.getInsertedId();
        }
        return null;
    }

    /**
     * 新增多条集合记录
     *
     * @param records 数据列表
     * @return 结果
     */
    public List<BsonValue> insertCollectionRecord(List<MongoRecord> records) {
        if (CollectionUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        String dbName = records.getFirst()._idColumn().getDbName();
        String collectionName = records.getFirst()._idColumn().getCollectionName();
        List<Document> documents = new ArrayList<>();
        for (MongoRecord record : records) {
            Document document = new Document();
            for (String col : record.columns()) {
                document.append(col, record.getValue(col));
            }
            documents.add(document);
        }
        com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
        com.mongodb.client.MongoCollection<Document> collection = database.getCollection(collectionName);
        InsertManyResult result = collection.insertMany(documents);
        Map<Integer, BsonValue> map = result.getInsertedIds();
        return new ArrayList<>(map.values());
    }

    /**
     * 删除集合记录
     *
     * @param record 数据
     * @return 结果
     */
    public long deleteCollectionRecord(MongoRecord record) {
        MongoColumn column = record._idColumn();
        if (column == null) {
            throw new IllegalArgumentException("_id");
        }
        String dbName = column.getDbName();
        String collectionName = column.getCollectionName();
        Object _id = record._idValue();
        Bson filter = Filters.eq(MongoUtil.ID, _id);
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, collectionName);
        DeleteResult result = collection.deleteOne(filter);
        return result.getDeletedCount();
    }

    /**
     * 创建集合
     *
     * @param collection 集合
     */
    public void createCollection(MongoCollection collection) {
        com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(collection.getDbName());
        database.createCollection(collection.getName());
    }

    /**
     * 清空集合
     *
     * @param dbName         数据库名称
     * @param collectionName 集合名称
     * @return 结果
     */
    public long clearCollection(String dbName, String collectionName) {
        com.mongodb.client.MongoCollection<Document> collection1 = this.collection(dbName, collectionName);
        DeleteResult result = collection1.deleteMany(new Document());
        return result.getDeletedCount();
    }

    /**
     * 重命名集合
     *
     * @param dbName  数据库名称
     * @param oldName 旧名称
     * @param newName 新名称
     */
    public void renameCollection(String dbName, String oldName, String newName) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, oldName);
        MongoNamespace namespace = new MongoNamespace(dbName, newName);
        collection.renameCollection(namespace);
    }

    /**
     * 更新集合记录
     *
     * @param record 数据
     * @return 结果
     */
    public long updateCollectionRecord(MongoRecord record) {
        MongoColumn column = record._idColumn();
        if (column == null) {
            throw new IllegalArgumentException("_id");
        }
        String dbName = column.getDbName();
        String collectionName = column.getCollectionName();
        com.mongodb.client.MongoCollection<Document> collection1 = this.collection(dbName, collectionName);
        Object _id = record._idValue();
        Bson filter = Filters.eq(MongoUtil.ID, _id);
        FindIterable<Document> iterable = collection1.find(filter);
        Document document = iterable.first();
        if (document == null) {
            return 0;
        }
        Bson update = null;
        for (MongoColumn mongoColumn : record.getColumns()) {
            if (mongoColumn.is_id()) {
                continue;
            }
            String colName = mongoColumn.getName();
            Bson bson = Updates.set(colName, record.getValue(colName));
            if (update == null) {
                update = bson;
            } else {
                update = Updates.combine(update, bson);
            }
        }

        for (String colName : document.keySet()) {
            if (record.column(colName) == null) {
                Bson bson = Updates.unset(colName);
                update = Updates.combine(update, bson);
            }
        }

        if (update != null) {
            UpdateResult result = collection1.updateOne(filter, update);
            return result.getMatchedCount();
        }
        return 0;
    }

    /**
     * 列举存储桶
     *
     * @param dbName 数据库名称
     * @return 结果
     */
    public List<MongoBucket> listBuckets(String dbName) {
        com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
        MongoIterable<String> collectionNames = database.listCollectionNames();
        List<MongoBucket> buckets = new ArrayList<>();
        for (String collectionName : collectionNames) {
            if (!MongoRecordUtil.isBucket(collectionName)) {
                continue;
            }
            MongoBucket gridFS = new MongoBucket();
            gridFS.setDbName(dbName);
            gridFS.setName(collectionName.substring(0, collectionName.lastIndexOf(".")));
            buckets.add(gridFS);
        }
        buckets = buckets.stream().sorted(Comparator.comparing(MongoBucket::getName)).toList();
        return buckets;
    }

    /**
     * 列举存储桶名称
     *
     * @param dbName 数据库名称
     * @return 结果
     */
    public List<String> listBucketNames(String dbName) {
        com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
        MongoIterable<String> collectionNames = database.listCollectionNames();
        List<String> buckets = new ArrayList<>();
        for (String collectionName : collectionNames) {
            if (!MongoRecordUtil.isBucket(collectionName)) {
                continue;
            }
            buckets.add(collectionName);
        }
        return buckets;
    }

    /**
     * 获取存储桶
     *
     * @param dbName     数据库名称
     * @param bucketName 桶名称
     */
    public GridFSBucket bucket(String dbName, String bucketName) {
        com.mongodb.client.MongoDatabase database = this.mongoClient.getDatabase(dbName);
        return GridFSBuckets.create(database, bucketName);
    }

    /**
     * 创建存储桶
     *
     * @param dbName     数据库名称
     * @param bucketName 桶名称
     */
    public void createBucket(String dbName, String bucketName) {
        GridFSBucket bucket = this.bucket(dbName, bucketName);
        // 需要上传一次数据，不然桶不会出现
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[]{});
        ObjectId _id = bucket.uploadFromStream("_empty_", bis);
        // 删除此数据
        this.deleteBucketRecord(dbName, bucketName, new BsonObjectId(_id));
    }

    /**
     * 删除存储桶
     *
     * @param dbName     数据库名称
     * @param bucketName 桶名称
     */
    public void dropBucket(String dbName, String bucketName) {
        this.dropCollection(dbName, bucketName + ".files");
    }

    /**
     * 清除存储桶
     *
     * @param dbName     数据库名称
     * @param bucketName 桶名称
     */
    public void clearBucket(String dbName, String bucketName) {
        GridFSBucket bucket = this.bucket(dbName, bucketName);
        bucket.find().forEach(file -> bucket.delete(file.getObjectId()));
    }

    /**
     * 转换为存储桶记录
     *
     * @param columns 列
     * @param file    文件
     * @return 结果
     */
    private MongoRecord toBucketRecord(MongoColumns columns, GridFSFile file) {
        MongoRecord record = new MongoRecord(columns, true);
        BsonValue id = file.getId();
        long length = file.getLength();
        int chunkSize = file.getChunkSize();
        String filename = file.getFilename();
        Document metadata = file.getMetadata();
        Date uploadDate = file.getUploadDate();
        record.putValue(columns.column(MongoUtil.ID), id);
        record.putValue(columns.column("filename"), filename);
        record.putValue(columns.column("length"), NumberUtil.formatSize(length, 2));
        record.putValue(columns.column("chunkSize"), NumberUtil.formatSize(chunkSize, 2));
        record.putValue(columns.column("uploadDate"), MongoUtil.DATE_FORMAT.format(uploadDate));
        record.putValue(columns.column("metadata"), metadata == null ? "" : JSONUtil.toJson(metadata));
        record.getProperty("metadata").setOriginal(metadata);
        return record;
    }

    /**
     * 查询存储桶记录
     *
     * @param param 参数
     * @return 结果
     */
    public List<MongoRecord> selectBucketRecords(MongoSelectRecordParam param) {
        String dbName = param.getDbName();
        String bucketName = param.getCollectionName();
        GridFSBucket bucket = this.bucket(dbName, bucketName);
        int skip = Math.toIntExact(param.getStart());
        int limit = Math.toIntExact(param.getLimit());
        Bson filters = MongoConditionUtil.buildCondition(param.getFilters());
        GridFSFindIterable iterable = bucket.find(filters).limit(limit).skip(skip);
        List<MongoRecord> records = new ArrayList<>();
        MongoColumns columns = this.bucketColumns();
        for (MongoColumn column : columns) {
            column.setDbName(dbName);
            column.setCollectionName(bucketName);
        }
        for (GridFSFile file : iterable) {
            MongoRecord record = this.toBucketRecord(columns, file);
            records.add(record);
        }
        return records;
    }

    /**
     * 查询单个记录
     *
     * @param dbName     数据库名称
     * @param bucketName 存储桶名称
     * @param _id        数据id
     * @return 结果
     */
    public MongoRecord selectBucketRecord(String dbName, String bucketName, Object _id) {
        if (_id == null) {
            throw new IllegalArgumentException("_id");
        }
        GridFSBucket bucket = this.bucket(dbName, bucketName);
        Bson filters = Filters.eq(MongoUtil.ID, _id);
        GridFSFindIterable iterable = bucket.find(filters).limit(1);
        MongoColumns columns = this.bucketColumns();
        for (MongoColumn column : columns) {
            column.setDbName(dbName);
            column.setCollectionName(bucketName);
        }
        GridFSFile file = iterable.first();
        if (file != null) {
            return this.toBucketRecord(columns, file);
        }
        return null;
    }

    /**
     * 查询存储桶记录数量
     *
     * @param param 参数
     * @return 结果
     */
    public long selectBucketRecordCount(MongoSelectRecordParam param) {
        String dbName = param.getDbName();
        String collectionName = param.getCollectionName();
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, collectionName + ".files");
        Bson filters = MongoConditionUtil.buildCondition(param.getFilters());
        return collection.countDocuments(filters);
    }

    /**
     * 存储桶字段列表
     *
     * @return 结果
     */
    public MongoColumns bucketColumns() {
        MongoColumns columns = new MongoColumns();
        MongoColumn idColumn = new MongoColumn("_id", I18nHelper.id());
        columns.add(idColumn);
        MongoColumn fileNameColumn = new MongoColumn("filename", I18nHelper.fileName());
        columns.add(fileNameColumn);
        MongoColumn lengthColumn = new MongoColumn("length", I18nHelper.length());
        columns.add(lengthColumn);
        MongoColumn chunkSizeColumn = new MongoColumn("chunkSize", I18nHelper.chunkSize());
        columns.add(chunkSizeColumn);
        MongoColumn uploadDateColumn = new MongoColumn("uploadDate", I18nHelper.uploadDate());
        columns.add(uploadDateColumn);
        //        MongoColumn contentTypeColumn = new MongoColumn("contentType", I18nHelper.contentType());
        //        columns.add(contentTypeColumn);
        //        MongoColumn md5Column = new MongoColumn("md5", "MD5");
        //        columns.add(md5Column);
        MongoColumn metadataColumn = new MongoColumn("metadata", I18nHelper.metadata());
        columns.add(metadataColumn);
        return columns;
    }

    /**
     * 上传存储桶记录
     *
     * @param dbName     数据库名称
     * @param bucketName 桶名称
     * @param file       文件
     * @return 结果
     */
    public ObjectId uploadBucketRecord(String dbName, String bucketName, File file) throws Exception {
        if (file == null) {
            throw new IllegalArgumentException("file");
        }
        GridFSBucket bucket = this.bucket(dbName, bucketName);
        FileInputStream fis = new FileInputStream(file);
        ObjectId objectId;
        try (fis) {
            objectId = bucket.uploadFromStream(file.getName(), fis);
        }
        return objectId;
    }

    /**
     * 重新上传存储桶记录
     *
     * @param dbName     数据库名称
     * @param bucketName 桶名称
     * @param id         文件id
     * @param filename   文件名
     * @param file       文件
     */
    public void reuploadBucketRecord(String dbName, String bucketName, Object id, String filename, File file) throws Exception {
        if (file == null) {
            throw new IllegalArgumentException("file");
        }
        // 删除旧文件
        this.deleteBucketRecord(dbName, bucketName, id);
        // 上传记录
        if (id instanceof BsonValue bsonValue) {
            GridFSBucket bucket = this.bucket(dbName, bucketName);
            FileInputStream fis = new FileInputStream(file);
            try (fis) {
                bucket.uploadFromStream(bsonValue, filename, fis);
            }
        }
    }

    /**
     * 下载存储桶记录
     *
     * @param dbName     数据库名称
     * @param bucketName 桶名称
     * @param _id        数据id
     * @param file       文件
     */
    public void downloadBucketRecord(String dbName, String bucketName, Object _id, String file) throws FileNotFoundException {
        if (_id == null) {
            throw new IllegalArgumentException("_id");
        }
        if (file == null) {
            throw new IllegalArgumentException("file");
        }
        GridFSBucket bucket = this.bucket(dbName, bucketName);
        FileOutputStream fos = new FileOutputStream(file);
        if (_id instanceof BsonValue bsonValue) {
            bucket.downloadToStream(bsonValue, fos);
        } else if (_id instanceof ObjectId objectId) {
            bucket.downloadToStream(objectId, fos);
        }
        IOUtil.close(fos);
    }

    /**
     * 删除存储桶记录
     *
     * @param dbName     数据库名称
     * @param bucketName 桶名称
     * @param _id        数据id
     * @return 结果
     */
    public long deleteBucketRecord(String dbName, String bucketName, Object _id) {
        if (_id == null) {
            throw new IllegalArgumentException("_id");
        }
        GridFSBucket bucket = this.bucket(dbName, bucketName);
        if (_id instanceof BsonValue bsonValue) {
            bucket.delete(bsonValue);
        } else if (_id instanceof ObjectId objectId) {
            bucket.delete(objectId);
        }
        return 1;
    }

    /**
     * 修改存储桶记录
     *
     * @param record 记录
     * @return 结果
     */
    public long updateBucketRecord(MongoRecord record) {
        MongoColumn column = record._idColumn();
        if (column == null) {
            throw new IllegalArgumentException("_id");
        }
        String dbName = column.getDbName();
        String bucketName = column.getCollectionName();
        com.mongodb.client.MongoCollection<Document> collection1 = this.collection(dbName, bucketName + ".files");
        Object _id = record._idValue();
        Bson filter = Filters.eq(MongoUtil.ID, _id);
        FindIterable<Document> iterable = collection1.find(filter);
        Document document = iterable.first();
        if (document == null) {
            return 0;
        }
        Bson update = Updates.combine(
                Updates.set("filename", record.getValue("filename")),
                Updates.set("metadata", record.getValue("metadata"))
                //                Updates.set("contentType", record.getValue("contentType"))
        );
        UpdateResult result = collection1.updateOne(filter, update);
        return result.getMatchedCount();
    }

    public List<? extends MongoColumn> selectColumns(MongoSelectRecordParam param) {
        List<MongoRecord> records = this.selectCollectionRecords(param);
        return MongoRecordUtil.columns(records);
    }

    private String version;

    /**
     * 查询版本信息
     *
     * @return 结果
     */
    public String selectVersion() {
        if (this.version == null) {
            Document buildInfo = this.mongoClient.getDatabase("admin")
                    .runCommand(new Document("buildInfo", 1));
            this.version = buildInfo.getString("version");
        }
        return this.version;
    }

    /**
     * 查询服务信息信息
     *
     * @return 结果
     */
    public Map<?, ?> selectHostInfo() {
        Document hostInfo = this.mongoClient.getDatabase("admin")
                .runCommand(new Document("hostInfo", 1));
        return new HashMap<>(hostInfo);
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 执行单段脚本
     *
     * @param dbName 数据库名称
     * @param script 脚本
     * @return 结果
     */
    public MongoExecuteResult executeSingleScript(String dbName, String script) {
        this.shellEngine().db(dbName);
        MongoExecuteResult result = new MongoExecuteResult();
        result.setScript(script);
        long start = System.currentTimeMillis();
        try {
            Object obj = this.shellEngine().eval(script);
            this.parseResult(result, obj, dbName, null);
        } catch (ScriptException ex) {
            ex.printStackTrace();
            result.setSuccess(false);
            result.setMsg(ex.getMessage());
        }
        result.setMsg("ok");
        long end = System.currentTimeMillis();
        result.setUsed(end - start);
        return result;
    }

    /**
     * 解析结果
     *
     * @param result 结果
     * @param obj    对象
     * @param dbName 数据库名称
     */
    private void parseResult(MongoExecuteResult result, Object obj, String dbName, String collectionName) {
        if (obj instanceof MongoScriptFindCursor cursor) {
            parseResult(result, cursor.toArray(), cursor.getDbName(), cursor.getCollectionName());
        } else if (obj instanceof MongoScriptCursor cursor) {
            parseResult(result, cursor.toArray(), dbName, collectionName);
        } else if (obj instanceof List<?> list) {
            result.setSuccess(true);
            List<MongoRecord> records = new ArrayList<>();
            for (Object o : list) {
                MongoRecord record = toMongoRecord(o, dbName, collectionName);
                if (record != null) {
                    records.add(record);
                }
            }
            result.parseResult(records);
        } else if (obj instanceof DeleteResult result1) {
            result.setSuccess(true);
            result.setUpdateCount(result1.getDeletedCount());
        } else if (obj instanceof InsertOneResult result1) {
            result.setSuccess(true);
            if (result1.getInsertedId() != null) {
                result.setUpdateCount(1);
            }
        } else if (obj instanceof InsertManyResult result1) {
            result.setSuccess(true);
            result.setUpdateCount(result1.getInsertedIds().size());
        } else if (obj instanceof UpdateResult result1) {
            result.setSuccess(true);
            result.setUpdateCount(result1.getModifiedCount());
        } else {
            result.setSuccess(true);
            MongoRecord record = toMongoRecord(obj, dbName, collectionName);
            if (record != null) {
                result.parseResult(List.of(record));
            }
        }
    }

    /**
     * 转换为mongo记录
     *
     * @param obj            对象
     * @param dbName         数据库名称
     * @param collectionName 集合名称
     */
    private MongoRecord toMongoRecord(Object obj, String dbName, String collectionName) {
        if (obj instanceof Document document) {
            return MongoRecordUtil.docToRecord(dbName, collectionName, document);
        }
        if (obj != null) {
            MongoColumns columns = new MongoColumns();
            MongoColumn column = new MongoColumn(I18nHelper.result());
            column.setDbName(dbName);
            column.setCollectionName(collectionName);
            columns.add(column);
            MongoRecord record = new MongoRecord(columns);
            record.putValue(column, JSONUtil.toJson(obj));
            return record;
        }
        return null;
    }

    /**
     * 执行脚本
     *
     * @param dbName 数据库名称
     * @param script 脚本
     * @return 结果
     */
    public MongoQueryResults<MongoExecuteResult> executeScript(String dbName, String script) {
        this.shellEngine().db(dbName);
        MongoQueryResults<MongoExecuteResult> results = new MongoQueryResults<>();
        try {
            MongoScriptParser parser = MongoScriptParser.getParser(script);
            List<String> sqlList = parser.parseScript();
            for (String sql1 : sqlList) {
                MongoExecuteResult result = this.executeSingleScript(dbName, sql1);
                results.addResult(result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            results.parseError(ex);
        }
        return results;
    }

    /**
     * 执行脚本
     *
     * @param dbName 数据库名称
     * @return 结果
     */
    public List<MongoFunction> listFunctions(String dbName) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, MongoUtil.SYSTEM_JS);
        List<MongoFunction> functions = new ArrayList<>();
        FindIterable<Document> iter = collection.find();
        for (Document document : iter) {
            MongoFunction function = new MongoFunction();
            Object value = document.get("value");
            if (value instanceof Code code) {
                function.setName(document.getString("_id"));
                function.setCode(code.getCode());
                function.setDbName(dbName);
                functions.add(function);
            }
        }
        return functions;
    }

    public BsonValue createFunction(String dbName, String functionName, String code) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, MongoUtil.SYSTEM_JS);
        Document funcDoc = new Document()
                .append("_id", functionName)
                .append("value", new Code(code));
        InsertOneResult result = collection.insertOne(funcDoc);
        return result.getInsertedId();
    }

    public boolean alertFunction(String dbName, String functionName, String code) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, MongoUtil.SYSTEM_JS);
        Bson filter = Filters.eq("_id", functionName);
        Bson update = Updates.set("value", new Code(code));
        UpdateResult result = collection.updateOne(filter, update);
        return result.getMatchedCount() == 1;
    }

    public MongoFunction selectFunction(String dbName, String functionName) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, MongoUtil.SYSTEM_JS);
        Bson filter = Filters.eq("_id", functionName);
        FindIterable<Document> iter = collection.find(filter);
        Document document = iter.first();
        if (document != null) {
            MongoFunction function = new MongoFunction();
            Code code = (Code) document.get("value");
            function.setName(document.getString("_id"));
            function.setCode(code.getCode());
            function.setDbName(dbName);
            return function;
        }
        return null;
    }

    public boolean dropFunction(String dbName, String functionName) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, MongoUtil.SYSTEM_JS);
        Bson filter = Filters.eq("_id", functionName);
        Document document = collection.findOneAndDelete(filter);
        return document != null;
    }

    public boolean renameFunction(String dbName, String oldName, String newName) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, MongoUtil.SYSTEM_JS);
        Bson filter = Filters.eq("_id", oldName);
        FindIterable<Document> iter = collection.find(filter);
        Document document = iter.first();
        if (document != null) {
            Document newFunc = new Document()
                    .append("_id", newName)
                    .append("value", document.get("value"));
            collection.insertOne(newFunc);
            collection.deleteOne(filter);

            return true;
        }
        return false;
    }

    public long functionSize(String dbName) {
        com.mongodb.client.MongoCollection<Document> collection = this.collection(dbName, MongoUtil.SYSTEM_JS);
        return collection.countDocuments();
    }

    /**
     * 执行脚本
     *
     * @param dbName 数据库名称
     * @param script 脚本
     * @return 结果
     * @throws Exception 异常
     */
    public Object eval(String dbName, String script) throws Exception {
        try {
            this.shellEngine().db(dbName);
            return this.shellEngine().eval(script);
        } catch (Exception ex) {
            JulLog.warn("script:\n" + script);
            throw ex;
        }
    }
}
