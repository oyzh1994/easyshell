package cn.oyzh.easyshell.data.mariadb.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.mariadb.ShellMariadbDataImportHelper;
import cn.oyzh.easyshell.data.mariadb.dto.ShellMariadbDataImportFile;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.column.MariadbSelectColumnParam;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.DBDataCsvTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataExcelTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataJsonTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataTxtTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataXmlTypeFileReader;
import cn.oyzh.fx.db.data.handler.DBDataImportHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mariadb数据导入处理器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDataImportHandler extends DBDataImportHandler<String> {

    /**
     * db客户端
     */
    private final ShellMariadbClient dbClient;

    /**
     * 导入文件
     */
    private List<ShellMariadbDataImportFile> files;

    /**
     * 导入配置
     */
    private final DBDataImportConfig config;

    /**
     * 构造 Mariadb数据导入处理器
     *
     * @param dbClient 数据库客户端
     * @param dbName   数据库名称
     */
    public ShellMariadbDataImportHandler(ShellMariadbClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
        this.config = new DBDataImportConfig();
    }

    @Override
    public void doImport() throws Exception {
        this.message("Import Starting");
        if (CollectionUtil.isNotEmpty(this.files)) {
            for (ShellMariadbDataImportFile file : files) {
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
     * @param file 导入文件
     * @throws Exception 异常
     */
    protected void importRecord(ShellMariadbDataImportFile file) throws Exception {
        String tableName = file.getTargetTableName();
        this.message("Importing Table " + tableName);
        this.message("Importing Records of Table " + tableName);
        // 复制模式
        if (this.config.isCopyMode()) {
            this.dbClient.clearTable(this.name, tableName);
        }
        try (DBDataTypeFileReader reader = this.initReader(file.getFile())) {
            // 获取数据库表字段
            MariadbColumns dbColumns = new MariadbColumns(this.dbClient.selectColumns(new MariadbSelectColumnParam(this.name, tableName)));
            if (!dbColumns.isEmpty()) {
                while (true) {
                    this.checkInterrupt();
                    long start1 = System.currentTimeMillis();
                    List<MariadbRecord> records = this.readRecords(reader, this.readLimit);
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

    /**
     * 初始化读取器
     *
     * @param file 文件
     * @return 类型文件读取器
     * @throws Exception 异常
     */
    private DBDataTypeFileReader initReader(File file) throws Exception {
        if (this.isCsvType()) {
            return new DBDataCsvTypeFileReader(file, this.config);
        }
        if (this.isJsonType()) {
            return new DBDataJsonTypeFileReader(file, this.config);
        }
        if (this.isXmlType()) {
            return new DBDataXmlTypeFileReader(file, this.config);
        }
        if (this.isExcelType()) {
            return new DBDataExcelTypeFileReader(file, this.config);
        }
        if (this.isTxtType()) {
            return new DBDataTxtTypeFileReader(file, this.config);
        }
        return null;
    }

    /**
     * 读取记录
     *
     * @param reader 类型文件读取器
     * @param count  读取数量
     * @return 记录列表
     * @throws Exception 异常
     */
    private List<MariadbRecord> readRecords(DBDataTypeFileReader reader, int count) throws Exception {
        List<MariadbRecord> records = new ArrayList<>();
        List<Map<String, Object>> list = reader.readObjects(count);
        for (Map<String, Object> objectMap : list) {
            MariadbRecord record = new MariadbRecord(null);
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
     * @throws Exception 异常
     */
    private void writeRecord(MariadbColumns columns, List<MariadbRecord> records) throws Exception {
        List<String> sqlList = ShellMariadbDataImportHelper.toInsertSql(columns, records, this.config);
        this.addInsert(sqlList);
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) {
        try {
            int result = this.dbClient.insertBatch(this.name, list, parallel);
            this.processedIncr(result);
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

    /**
     * 设置记录分隔符
     *
     * @param recordSeparator 记录分隔符
     */
    public void recordSeparator(String recordSeparator) {
        this.config.setRecordSeparator(recordSeparator);
    }

    /**
     * 设置文本标识符
     *
     * @param txtIdentifier 文本标识符
     */
    public void txtIdentifier(String txtIdentifier) {
        this.config.setTxtIdentifier(txtIdentifier);
    }

    /**
     * 设置字段分隔符
     *
     * @param fieldSeparator 字段分隔符
     */
    public void fieldSeparator(String fieldSeparator) {
        this.config.setFieldSeparator(fieldSeparator);
    }

    /**
     * 获取导入文件列表
     *
     * @return 导入文件列表
     */
    public List<ShellMariadbDataImportFile> getFiles() {
        return files;
    }

    /**
     * 设置导入文件列表
     *
     * @param files 导入文件列表
     */
    public void setFiles(List<ShellMariadbDataImportFile> files) {
        this.files = files;
    }

    /**
     * 获取导入配置
     *
     * @return 导入配置
     */
    public DBDataImportConfig getConfig() {
        return config;
    }
}
