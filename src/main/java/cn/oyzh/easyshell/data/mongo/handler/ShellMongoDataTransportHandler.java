package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoSelectRecordParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.handler.DBDataTransportHandler;
import org.bson.BsonValue;

import java.util.List;


/**
 * Mongo数据传输处理器
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoDataTransportHandler extends DBDataTransportHandler<MongoRecord> {

    /**
     * 来源客户端
     */
    protected ShellMongoClient sourceClient;

    /**
     * 目标客户端
     */
    protected ShellMongoClient targetClient;

    /**
     * 表
     */
    protected List<DBDataTransportObject> tables;

    /**
     * 函数
     */
    protected List<DBDataTransportObject> functions;

    /**
     * 构造 Mongo数据传输处理器
     */
    public ShellMongoDataTransportHandler() {
        super(DBDialect.MONGODB);
    }

    @Override
    public void doTransport() throws Exception {
        this.message("Transport Starting");
        try {
            if (CollectionUtil.isNotEmpty(this.tables)) {
                for (DBDataTransportObject table : this.tables) {
                    this.transportTable(table.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.functions)) {
                for (DBDataTransportObject function : this.functions) {
                    this.transportFunction(function.getName());
                }
            }
        } catch (Exception ex) {
            this.exception(ex);
        } finally {
            this.message("Transport Finished");
        }
    }

    /**
     * 传输表
     *
     * @param tableName 表名称
     * @throws Exception 异常
     */
    private void transportTable(String tableName) throws Exception {
        this.checkInterrupt();
        // 删除表
        this.targetClient.dropCollection(this.targetDatabase, tableName);
        this.message("Drop Collection " + tableName);
        this.processedIncr();

        // 创建表
        this.targetClient.createCollection(this.targetDatabase, tableName);
        this.message("Create Collection " + tableName);
        this.processedIncr();

        // 传输表
        this.message("Transport Collection " + tableName + " Starting");
        long start = 0;
        while (true) {
            this.checkInterrupt();
            MongoSelectRecordParam param = new MongoSelectRecordParam();
            param.setStart(start);
            param.setReadonly(true);
            param.setCollectionName(tableName);
            param.setDbName(this.sourceDatabase);
            param.setLimit((long) this.selectLimit);
            List<MongoRecord> records = this.sourceClient.selectCollectionRecords(param);
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            this.addInsert(records);
            start += this.selectLimit;
        }
        this.message("Transport Collection " + tableName + " Finished");
    }

    /**
     * 传输函数
     *
     * @param functionName 函数名称
     * @throws InterruptedException 异常
     */
    private void transportFunction(String functionName) throws InterruptedException {
        this.checkInterrupt();
        // 删除函数
        this.targetClient.dropFunction(this.targetDatabase, functionName);
        this.message("Drop Function " + functionName);
        this.processedIncr();

        // 创建函数
        MongoFunction function = this.sourceClient.selectFunction(this.sourceDatabase, functionName);
        this.targetClient.createFunction(this.targetDatabase, functionName, function.getCode());
        this.message("Create Function " + functionName);
        this.processedIncr();
    }

    @Override
    public void doBatchInsert(List<MongoRecord> list, boolean parallel) {
        try {
            for (MongoRecord record : list) {
                for (MongoColumn column : record.getColumns()) {
                    column.setDbName(this.getTargetDatabase());
                }
            }
            List<BsonValue> result = this.targetClient.insertCollectionRecord(list);
            this.processedIncr(result.size());
        } catch (Exception ex) {
            this.processedDecr(list.size());
            throw ex;
        }
    }

    /**
     * 设置函数列表
     *
     * @param functions 函数列表
     */
    public void setFunctions(List<DBDataTransportObject> functions) {
        this.functions = functions;
    }

    /**
     * 获取函数列表
     *
     * @return 函数列表
     */
    public List<DBDataTransportObject> getFunctions() {
        return functions;
    }

    /**
     * 获取来源客户端
     *
     * @return 来源客户端
     */
    public ShellMongoClient getSourceClient() {
        return sourceClient;
    }

    /**
     * 设置来源客户端
     *
     * @param sourceClient 来源客户端
     */
    public void setSourceClient(ShellMongoClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    /**
     * 获取目标客户端
     *
     * @return 目标客户端
     */
    public ShellMongoClient getTargetClient() {
        return targetClient;
    }

    /**
     * 设置目标客户端
     *
     * @param targetClient 目标客户端
     */
    public void setTargetClient(ShellMongoClient targetClient) {
        this.targetClient = targetClient;
    }

    /**
     * 获取表列表
     *
     * @return 表列表
     */
    public List<DBDataTransportObject> getTables() {
        return tables;
    }

    /**
     * 设置表列表
     *
     * @param tables 表列表
     */
    public void setTables(List<DBDataTransportObject> tables) {
        this.tables = tables;
    }
}

