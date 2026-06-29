package cn.oyzh.easyshell.data.mysql.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.mysql.config.mysql.MysqlCsvTypeFileReader;
import cn.oyzh.easyshell.data.mysql.config.mysql.MysqlDataImportConfig;
import cn.oyzh.easyshell.data.mysql.config.mysql.MysqlDataImportHelper;
import cn.oyzh.easyshell.data.mysql.config.mysql.MysqlExcelTypeFileReader;
import cn.oyzh.easyshell.data.mysql.config.mysql.MysqlJsonTypeFileReader;
import cn.oyzh.easyshell.data.mysql.config.mysql.MysqlTxtTypeFileReader;
import cn.oyzh.easyshell.data.mysql.config.mysql.MysqlTypeFileReader;
import cn.oyzh.easyshell.data.mysql.config.mysql.MysqlXmlTypeFileReader;
import cn.oyzh.easyshell.db.handler.DBDataImportHandler;
import cn.oyzh.easyshell.data.mysql.dto.ShellMysqlDataImportFile;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author oyzh
 * @since 2025-11-26
 */
public class ShellMysqlDataImportHandler extends DBDataImportHandler {

    /**
     * db客户端
     */
    private final ShellMysqlClient dbClient;

    /**
     * 导入文件
     */
    private List<ShellMysqlDataImportFile> files;

    /**
     * 导入配置
     */
    private final MysqlDataImportConfig config;

    public ShellMysqlDataImportHandler(ShellMysqlClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
        this.config = new MysqlDataImportConfig();
    }

    @Override
    public void doImport() throws Exception {
        this.message("Import Starting");
        if (CollectionUtil.isNotEmpty(this.files)) {
            for (ShellMysqlDataImportFile file : files) {
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
    protected void importRecord(ShellMysqlDataImportFile file) throws Exception {
        String tableName = file.getTargetTableName();
        this.message("Importing Table " + tableName);
        this.message("Importing Records of Table " + tableName);
        // 复制模式
        if (this.config.isCopyMode()) {
            this.dbClient.clearTable(this.dbName, tableName);
        }
        try (MysqlTypeFileReader reader = this.initReader(file.getFile())) {
            // 获取数据库表字段
            MysqlColumns dbColumns = new MysqlColumns(this.dbClient.selectColumns(new MysqlSelectColumnParam(this.dbName, tableName)));
            if (!dbColumns.isEmpty()) {
                while (true) {
                    this.checkInterrupt();
                    long start1 = System.currentTimeMillis();
                    List<MysqlRecord> records = this.readRecords(reader, this.readLimit);
                    if (CollectionUtil.isEmpty(records)) {
                        break;
                    }
                    long end1 = System.currentTimeMillis();
                    JulLog.info("读取耗时: {}ms", (end1 - start1));
                    long start2 = System.currentTimeMillis();
                    this.writeRecord(dbColumns, records);
                    long end2 = System.currentTimeMillis();
                    JulLog.info("写入耗时: {}ms", (end2 - start2));
                    // this.processed(records.size());
                }
            }
            // 收尾批量插入
            this.doBatchInsert();
        } finally {
            this.insertList = null;
            this.message("Importing Table " + tableName + " From -> " + file.getFilePath());
        }
    }

    private MysqlTypeFileReader initReader(File file) throws Exception {
        if (this.isCsvType()) {
            return new MysqlCsvTypeFileReader(file, this.config);
        }
        if (this.isJsonType()) {
            return new MysqlJsonTypeFileReader(file, this.config);
        }
        if (this.isXmlType()) {
            return new MysqlXmlTypeFileReader(file, this.config);
        }
        if (this.isExcelType()) {
            return new MysqlExcelTypeFileReader(file, this.config);
        }
        if (this.isTxtType()) {
            return new MysqlTxtTypeFileReader(file, this.config);
        }
        return null;
    }

    private List<MysqlRecord> readRecords(MysqlTypeFileReader reader, int count) throws Exception {
        List<MysqlRecord> records = new ArrayList<>();
        List<Map<String, Object>> list = reader.readObjects(count);
        for (Map<String, Object> objectMap : list) {
            MysqlRecord record = new MysqlRecord(null);
            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                record.putValue(entry.getKey(), entry.getValue());
            }
            records.add(record);
        }
        return records;
    }

    /**
     * 写入记录
     *
     * @param columns 字段列表
     * @param records 记录列表
     */
    private void writeRecord(MysqlColumns columns, List<MysqlRecord> records) throws Exception {
        List<String> sqlList = MysqlDataImportHelper.toInsertSql(columns, records, this.config);
        this.addInsertSql(sqlList);
    }

    @Override
    protected void doBatchInsert(List<String> sqlList, boolean parallel) {
        try {
            int result = this.dbClient.insertBatch(this.dbName, sqlList, parallel);
            this.processedIncr(result);
        } catch (Exception ex) {
            this.processedDecr(sqlList.size());
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

    public List<ShellMysqlDataImportFile> getFiles() {
        return files;
    }

    public void setFiles(List<ShellMysqlDataImportFile> files) {
        this.files = files;
    }

    public MysqlDataImportConfig getConfig() {
        return config;
    }
}
