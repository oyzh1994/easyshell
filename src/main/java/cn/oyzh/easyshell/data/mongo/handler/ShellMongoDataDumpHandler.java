package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.db.handler.DBDataDumpHandler;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.collection.MongoCollection;
import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoSelectRecordParam;
import cn.oyzh.easyshell.util.mongo.MongoDataUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/10
 */
public class ShellMongoDataDumpHandler extends DBDataDumpHandler {

    /**
     * db客户端
     */
    protected ShellMongoClient dbClient;

    public ShellMongoDataDumpHandler(ShellMongoClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
    }

    @Override
    public void doDump() throws Exception {
        if (this.fileWriter == null || this.dumpType == null || this.dataType == null) {
            throw new RuntimeException("parameter invalid!");
        }
        this.message("Dump Starting");
        this.writeHeader();
        if (this.dumpType == 1) {
            this.dumpCollection();
            this.dumpFunction();
        } else if (this.dumpType == 2) {
            MongoCollection collection = new MongoCollection();
            collection.setDbName(this.dbName);
            collection.setName(this.tableName);
            this.dumpCollection(collection);
            this.writeTail();
            this.fileWriter.close();
            this.message("Dump Finished");
            this.message("Dump File To -> " + this.dumpFile.getPath());
        }
    }

    protected void dumpCollection() throws InterruptedException, IOException {
        List<MongoCollection> collections = this.dbClient.listCollections(this.dbName);
        if (CollectionUtil.isNotEmpty(collections)) {
            for (MongoCollection table : collections) {
                this.checkInterrupt();
                this.dumpCollection(table);
            }
            this.processed(collections.size());
        }
    }

    protected void dumpCollection(MongoCollection collection) throws InterruptedException, IOException {
        String line0 = "";
        String line1 = "// ----------------------------";
        String line2 = "// Collection structure for " + collection.getName();
        String line3 = "// ----------------------------";
        String line4 = "db.getCollection('" + collection.getName() + "').drop();";
        String line5 = "db.createCollection('" + collection.getName() + "');";
        this.message("Dumping Collection " + collection.getName());
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3, line4, line5));
        if (this.isDumpRecord()) {
            this.message("Dumping Records of Collection " + collection.getName());
            this.dumpRecord(collection.getName());
        }
    }

    protected void dumpRecord(String tableName) throws InterruptedException, IOException {
        long start = 0;
        String line0 = "";
        String line1 = "// ----------------------------";
        String line2 = "// Documents of " + tableName;
        String line3 = "// ----------------------------";
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3));
        while (true) {
            this.checkInterrupt();
            long start1 = System.currentTimeMillis();
            MongoSelectRecordParam param = new MongoSelectRecordParam();
            param.setStart(start);
            param.setReadonly(true);
            param.setDbName(this.dbName);
            param.setCollectionName(tableName);
            param.setLimit((long) this.queryLimit);
            List<MongoRecord> records = this.dbClient.selectCollectionRecords(param);
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            long end1 = System.currentTimeMillis();
            JulLog.info("查询耗时: {}ms", (end1 - start1));
            long start2 = System.currentTimeMillis();
            List<String> inserts = MongoDataUtil.toInsertScript(records);
            this.fileWriter.appendLines(inserts);
            long end2 = System.currentTimeMillis();
            JulLog.info("写入耗时: {}ms", (end2 - start2));
            start += this.queryLimit;
            this.processed(records.size());
        }
    }

    protected void dumpFunction() throws Exception {
        List<MongoFunction> functions = this.dbClient.listFunctions(this.dbName);
        if (CollectionUtil.isNotEmpty(functions)) {
            for (MongoFunction function : functions) {
                this.checkInterrupt();
                this.message("Dumping Function " + function.getName());
                String line0 = "";
                String line1 = "// ----------------------------";
                String line2 = "// Function structure for " + function.getName();
                String line3 = "// ----------------------------";

                String id = MongoDataUtil.buildRecordValue(function.getName(), 0).toString();
                String dropFunction = """
                        db.getCollection('system.js').deleteOne({
                            _id: $id
                        });
                        """;
                dropFunction = dropFunction.replace("$id", id);

                String createDefinition = """
                        db.getCollection('system.js').insert({
                            _id: $id,
                            value: Code('$code')
                        })
                        """;
                String code = function.getCode();
                code = code.replace("\n", "\\n");
                code = code.replace("\r", "\\r");
                createDefinition = createDefinition.replace("$id", id).replace("$code", code);
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropFunction, createDefinition));
            }
            this.processed(functions.size());
        }
    }

    @Override
    protected void writeHeader() throws IOException {
        String version = this.dbClient.selectVersion();
        String clientCharacter = "utf-8";
        String header = "/*\n";
        header += " " + Project.load().getName() + " Data Transfer";
        header += "\n\n";
        header += " Source Server : " + this.connect.getName();
        header += "\n";
        header += " Source Server Type : Mongdb";
        header += "\n";
        header += " Source Server Version : " + version;
        header += "\n";
        header += " Source Host : " + this.connect.getHost();
        header += "\n";
        header += " Source Schema : " + this.dbName;
        header += "\n\n";
        header += " Target Server Type : Mongodb";
        header += "\n";
        header += " Target Server Version : " + version;
        header += "\n";
        header += " File Encoding : " + clientCharacter;
        header += "\n\n";
        header += " Date : " + DateHelper.formatDateTimeSimple();
        header += "\n";
        header += "*/";

        this.fileWriter.writeLines(List.of(header));
    }

    @Override
    protected void writeTail() throws IOException {

    }

    public ShellMongoClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellMongoClient dbClient) {
        this.dbClient = dbClient;
    }
}

