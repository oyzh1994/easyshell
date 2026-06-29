package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.mongo.config.MongoDataExportConfig;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataExportCollection;
import cn.oyzh.easyshell.data.mongo.file.MongoCsvTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.MongoExcelTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.MongoHtmlTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.MongoJsTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.MongoJsonTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.MongoTxtTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.MongoTypeFileWriter;
import cn.oyzh.easyshell.data.mongo.file.MongoXmlTypeFileWriter;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.mongo.MongoRecord;
import cn.oyzh.easyshell.mongo.MongoSelectRecordParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public abstract class DBDataExportHandler extends DBDataHandler {

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 文件类型
     * sql
     * json
     */
    private String fileType;

    /**
     * db客户端
     */
    private ShellMongoClient dbClient;

    /**
     * 查询限制
     */
    private int queryLimit = 500;

    /**
     * 导出表
     */
    private List<ShellMongoDataExportCollection> tables;

    /**
     * 导出配置
     */
    private final MongoDataExportConfig config;

    public DBDataExportHandler(ShellMongoClient dbClient, String dbName) {
        this.dbClient = dbClient;
        this.dbName = dbName;
        this.config = new MongoDataExportConfig();
    }

    /**
     * 是否sql类型
     *
     * @return 结果
     */
    public boolean isSqlType() {
        return "sql".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xml类型
     *
     * @return 结果
     */
    public boolean isXmlType() {
        return "xml".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否csv类型
     *
     * @return 结果
     */
    public boolean isCsvType() {
        return "csv".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否html类型
     *
     * @return 结果
     */
    public boolean isHtmlType() {
        return "html".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xls类型
     *
     * @return 结果
     */
    public boolean isXlsType() {
        return "xls".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xlsx类型
     *
     * @return 结果
     */
    public boolean isXlsxType() {
        return "xlsx".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否excel类型
     *
     * @return 结果
     */
    public boolean isExcelType() {
        return this.isXlsType() || this.isXlsxType();
    }

    /**
     * 是否json类型
     *
     * @return 结果
     */
    public boolean isJsonType() {
        return "json".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否txt类型
     *
     * @return 结果
     */
    public boolean isTxtType() {
        return "txt".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否js类型
     *
     * @return 结果
     */
    public boolean isJsType() {
        return "js".equalsIgnoreCase(this.fileType);
    }

    /**
     * 执行导出
     *
     * @throws Exception 异常
     */
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

    private MongoTypeFileWriter initWriter(String filePath, MongoColumns columns) throws IOException {
        if (this.isExcelType()) {
            return new MongoExcelTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isHtmlType()) {
            return new MongoHtmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsonType()) {
            return new MongoJsonTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isXmlType()) {
            return new MongoXmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isCsvType()) {
            return new MongoCsvTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isTxtType()) {
            return new MongoTxtTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsType()) {
            return new MongoJsTypeFileWriter(filePath, this.config, columns);
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
        this.message("Exporting Table " + table.getName());
        this.message("Exporting Records of Table " + table.getName());
        long start = 0;
        MongoColumns columns = new MongoColumns(table.selectedColumns());
        try (MongoTypeFileWriter writer = this.initWriter(table.getFilePath(), columns)) {
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
            this.message("Exporting Table " + tableName + " To -> " + table.getFilePath());
        }
    }

    /**
     * 写入头
     *
     * @throws IOException 异常
     */
    private void writeHeader(MongoTypeFileWriter writer) throws Exception {
        writer.writeHeader();
    }

    /**
     * 写入记录
     *
     * @param records 记录列表
     * @throws IOException 异常
     */
    private void writeRecord(MongoTypeFileWriter writer, List<MongoRecord> records) throws Exception {
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
    private void writeTail(MongoTypeFileWriter writer) throws Exception {
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

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public ShellMongoClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellMongoClient dbClient) {
        this.dbClient = dbClient;
    }

    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }

    public List<ShellMongoDataExportCollection> getTables() {
        return tables;
    }

    public void setTables(List<ShellMongoDataExportCollection> tables) {
        this.tables = tables;
    }

    public MongoDataExportConfig getConfig() {
        return config;
    }
}

