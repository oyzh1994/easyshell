package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.handler.DBDataExportHandler;
import cn.oyzh.easyshell.data.mongo.config.ShellMongoDataExportConfig;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataExportCollection;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoCsvTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoExcelTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoHtmlTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoJsTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoJsonTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoTxtTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoXmlTypeFileWriter;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoSelectRecordParam;
import cn.oyzh.easyshell.mongo.ShellMongoClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class ShellMongoDataExportHandler extends DBDataExportHandler {

    /**
     * db客户端
     */
    private ShellMongoClient dbClient;

    /**
     * 导出表
     */
    private List<ShellMongoDataExportCollection> tables;

    /**
     * 导出配置
     */
    private final ShellMongoDataExportConfig config;

    public ShellMongoDataExportHandler(ShellMongoClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
        this.config = new ShellMongoDataExportConfig();
    }

    @Override
    public void doExport() throws Exception {
        this.message("Export Starting");
        if (CollectionUtil.isNotEmpty(this.tables)) {
            for (ShellMongoDataExportCollection table : this.tables) {
                this.checkInterrupt();
                this.exportTable(table);
                this.processedIncr();
            }
        }
        this.message("Export Finished");
    }

    private ShellMongoTypeFileWriter initWriter(String filePath, MongoColumns columns) throws IOException {
        if (this.isExcelType()) {
            return new ShellMongoExcelTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isHtmlType()) {
            return new ShellMongoHtmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsonType()) {
            return new ShellMongoJsonTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isXmlType()) {
            return new ShellMongoXmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isCsvType()) {
            return new ShellMongoCsvTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isTxtType()) {
            return new ShellMongoTxtTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsType()) {
            return new ShellMongoJsTypeFileWriter(filePath, this.config, columns);
        }
        return null;
    }

    /**
     * 导出表
     *
     * @param table 表
     * @throws Exception 异常
     */
    protected void exportTable(ShellMongoDataExportCollection table) throws Exception {
        String tableName = table.getName();
        this.message("Exporting Collection " + table.getName());
        this.message("Exporting Records of Collection " + table.getName());
        long start = 0;
        MongoColumns columns = new MongoColumns(table.selectedColumns());
        try (ShellMongoTypeFileWriter writer = this.initWriter(table.getFilePath(), columns)) {
            this.writeHeader(writer);
            if (!columns.isEmpty()) {
                boolean stop = false;
                while (!stop) {
                    this.checkInterrupt();
                    List<MongoRecord> records;
                    // 正常导出
                    if (table.getRecords() == null) {
                        long start1 = System.currentTimeMillis();
                        MongoSelectRecordParam param = new MongoSelectRecordParam();
                        param.setStart(start);
                        param.setReadonly(true);
                        param.setColumns(columns);
                        param.setDbName(this.dbName);
                        param.setCollectionName(tableName);
                        param.setLimit((long) this.queryLimit);
                        records = this.dbClient.selectCollectionRecords(param);
                        if (CollectionUtil.isEmpty(records)) {
                            break;
                        }
                        long end1 = System.currentTimeMillis();
                        JulLog.info("查询耗时: {}ms", (end1 - start1));
                    } else {// 查询导出
                        records = table.getRecords();
                        stop = true;
                    }
                    // 写入记录
                    long start2 = System.currentTimeMillis();
                    this.writeRecord(writer, records);
                    long end2 = System.currentTimeMillis();
                    JulLog.info("写入耗时: {}ms", (end2 - start2));
                    start += this.queryLimit;
                    this.processed(records.size());
                }
            }
            this.writeTail(writer);
        } finally {
            this.message("Exporting Collection " + tableName + " To -> " + table.getFilePath());
        }
    }

    /**
     * 写入头
     *
     * @throws IOException 异常
     */
    private void writeHeader(ShellMongoTypeFileWriter writer) throws Exception {
        writer.writeHeader();
    }

    /**
     * 写入记录
     *
     * @param records 记录列表
     * @throws IOException 异常
     */
    private void writeRecord(ShellMongoTypeFileWriter writer, List<MongoRecord> records) throws Exception {
        List<Map<String, Object>> objects = new ArrayList<>();
        for (MongoRecord object : records) {
            objects.add(object.toMap());
        }
        writer.writeObjects(objects);
    }

    /**
     * 写入尾
     *
     * @throws IOException 异常
     */
    private void writeTail(ShellMongoTypeFileWriter writer) throws Exception {
        writer.writeTrial();
    }

    /**
     * 设置日期格式
     *
     * @param dateFormat 日期格式
     */
    public void dateFormat(String dateFormat) {
        if (StringUtil.isBlank(dateFormat)) {
            this.config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        } else {
            this.config.setDateFormat(dateFormat);
        }
    }

    public void recordSeparator(String recordSeparator) {
        this.config.setRecordSeparator(recordSeparator);
    }

    public void txtIdentifier(String txtIdentifier) {
        this.config.setTxtIdentifier(txtIdentifier);
    }

    public void fieldSeparator(String fieldSeparator) {
        this.config.setFieldSeparator(fieldSeparator);
    }

    public void includeFields(boolean includeFields) {
        this.config.setIncludeFields(includeFields);
    }

    public void fieldToAttr(boolean fieldToAttr) {
        this.config.setFieldToAttr(fieldToAttr);
    }

    public void earlyVersion(boolean earlyVersion) {
        this.config.setEarlyVersion(earlyVersion);
    }

    public ShellMongoClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellMongoClient dbClient) {
        this.dbClient = dbClient;
    }

    public List<ShellMongoDataExportCollection> getTables() {
        return tables;
    }

    public void setTables(List<ShellMongoDataExportCollection> tables) {
        this.tables = tables;
    }

    public ShellMongoDataExportConfig getConfig() {
        return config;
    }
}

