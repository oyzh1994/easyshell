package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.handler.DBDataImportHandler;
import cn.oyzh.easyshell.data.mongo.config.ShellMongoDataImportConfig;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataImportFile;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoExcelTypeFileReader;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoJsonTypeFileReader;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoTypeFileReader;
import cn.oyzh.easyshell.data.mongo.file.ShellMongoXmlTypeFileReader;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.mongo.MongoRecord;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import org.bson.BsonValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class ShellMongoDataImportHandler extends DBDataImportHandler<MongoRecord> {

    /**
     * db客户端
     */
    private ShellMongoClient dbClient;

    /**
     * 导入文件
     */
    private List<ShellMongoDataImportFile> files;

    /**
     * 导入配置
     */
    private final ShellMongoDataImportConfig config;

    public ShellMongoDataImportHandler(ShellMongoClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
        this.config = new ShellMongoDataImportConfig();
    }

    @Override
    public void doImport() throws Exception {
        this.message("Import Starting");
        if (CollectionUtil.isNotEmpty(this.files)) {
            for (ShellMongoDataImportFile file : files) {
                this.checkInterrupt();
                this.importRecord(file);
            }
            this.processed(files.size());
        }
        this.message("Import Finished");
    }

    /**
     * 导入表
     *
     * @throws Exception 异常
     */
    protected void importRecord(ShellMongoDataImportFile file) throws Exception {
        String tableName = file.getTargetTableName();
        this.message("Importing Collection " + tableName);
        this.message("Importing Records of Collection " + tableName);
        // 复制模式
        if (this.config.isCopyMode()) {
            this.dbClient.clearCollection(this.dbName, tableName);
        }
        try (ShellMongoTypeFileReader reader = this.initReader(file.getFile())) {
            // 获取数据库表字段
            while (true) {
                this.checkInterrupt();
                long start1 = System.currentTimeMillis();
                List<MongoRecord> records = this.readRecords(reader, this.readLimit);
                if (CollectionUtil.isEmpty(records)) {
                    break;
                }
                long end1 = System.currentTimeMillis();
                JulLog.info("读取耗时: {}ms", (end1 - start1));
                long start2 = System.currentTimeMillis();
                this.addInsert(records);
                long end2 = System.currentTimeMillis();
                JulLog.info("写入耗时: {}ms", (end2 - start2));
            }
            // 收尾批量插入
            this.doBatchInsert();
        } finally {
            this.insertList = null;
            this.message("Importing Collection " + tableName + " From -> " + file.getFilePath());
        }
    }

    private ShellMongoTypeFileReader initReader(File file) throws Exception {
        if (this.isJsonType()) {
            return new ShellMongoJsonTypeFileReader(file, this.config);
        }
        if (this.isXmlType()) {
            return new ShellMongoXmlTypeFileReader(file, this.config);
        }
        if (this.isExcelType()) {
            return new ShellMongoExcelTypeFileReader(file, this.config);
        }
        return null;
    }

    private List<MongoRecord> readRecords(ShellMongoTypeFileReader reader, int count) throws Exception {
        List<MongoRecord> records = new ArrayList<>();
        List<Map<String, Object>> list = reader.readObjects(count);
        for (Map<String, Object> objectMap : list) {
            MongoColumns columns = new MongoColumns();
            String collectionName = reader.getFile().getName();
            collectionName = FileNameUtil.removeExtName(collectionName);
            for (String s : objectMap.keySet()) {
                MongoColumn column = new MongoColumn();
                column.setName(s);
                column.setCollectionName(collectionName);
                columns.add(column);
            }
            MongoRecord record = new MongoRecord(columns);
            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                record.putValue(entry.getKey(), entry.getValue());
            }
            records.add(record);
        }
        return records;
    }

    @Override
    public void doBatchInsert(List<MongoRecord> list, boolean parallel) {
        try {
            for (MongoRecord record : list) {
                record.getColumns().forEach(c -> c.setDbName(this.dbName));
            }
            List<BsonValue> records = this.dbClient.insertCollectionRecord(list);
            this.processedIncr(records.size());
        } catch (Exception ex) {
            this.processedDecr(list.size());
            throw ex;
        }
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

    /**
     * 设置导入模式
     *
     * @param importMode 导入模式
     */
    public void importMode(String importMode) {
        this.config.setImportMode(importMode);
    }

    /**
     * 设置字段索引
     *
     * @param columnIndex 字段索引
     */
    public void columnIndex(int columnIndex) {
        this.config.setColumnIndex(columnIndex);
    }

    /**
     * 设置数据起始索引
     *
     * @param dataStartIndex 数据起始索引
     */
    public void dataStartIndex(int dataStartIndex) {
        this.config.setDataStartIndex(dataStartIndex);
    }

    /**
     * 设置字段标签
     *
     * @param recordLabel 字段标签
     */
    public void recordLabel(String recordLabel) {
        this.config.setRecordLabel(recordLabel);
    }

    /**
     * 设置属性作为字段
     *
     * @param attrToColumn 属性作为字段
     */
    public void attrToColumn(boolean attrToColumn) {
        this.config.setAttrToColumn(attrToColumn);
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

    public ShellMongoClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellMongoClient dbClient) {
        this.dbClient = dbClient;
    }

    public List<ShellMongoDataImportFile> getFiles() {
        return files;
    }

    public void setFiles(List<ShellMongoDataImportFile> files) {
        this.files = files;
    }

    public ShellMongoDataImportConfig getConfig() {
        return config;
    }
}

