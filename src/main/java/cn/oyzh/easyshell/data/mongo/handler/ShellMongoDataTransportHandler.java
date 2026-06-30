package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.db.handler.DBDataTransportHandler;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataTransportCollection;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataTransportFunction;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.MongoFunction;
import cn.oyzh.easyshell.mongo.MongoRecord;
import cn.oyzh.easyshell.mongo.MongoSelectRecordParam;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import org.bson.BsonValue;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/06
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
    protected List<ShellMongoDataTransportCollection> tables;

    /**
     * 函数
     */
    protected List<ShellMongoDataTransportFunction> functions;

    @Override
    public void doTransport() throws Exception {
        this.message("Transport Starting");
        try {
            if (CollectionUtil.isNotEmpty(this.tables)) {
                for (ShellMongoDataTransportCollection table : this.tables) {
                    this.transportTable(table.getName());
                }
            }
            if (CollectionUtil.isNotEmpty(this.functions)) {
                for (ShellMongoDataTransportFunction function : this.functions) {
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
     * @throws InterruptedException 异常
     */
    private void transportTable(String tableName) throws Exception {
        this.checkInterrupt();
        // 删除表
        this.targetClient.dropCollection(this.targetDatabase, tableName);
        this.message("Drop Collection " + tableName);
        this.processedIncr();

        // 创建表
        this.targetClient.clearCollection(this.targetDatabase, tableName);
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

    public void setFunctions(List<ShellMongoDataTransportFunction> functions) {
        this.functions = functions;
    }

    public List<ShellMongoDataTransportFunction> getFunctions() {
        return functions;
    }

    public ShellMongoClient getSourceClient() {
        return sourceClient;
    }

    public void setSourceClient(ShellMongoClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    public ShellMongoClient getTargetClient() {
        return targetClient;
    }

    public void setTargetClient(ShellMongoClient targetClient) {
        this.targetClient = targetClient;
    }

    public List<ShellMongoDataTransportCollection> getTables() {
        return tables;
    }

    public void setTables(List<ShellMongoDataTransportCollection> tables) {
        this.tables = tables;
    }
}

