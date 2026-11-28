package cn.oyzh.easyshell.data.oracle.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleInsertRecordParam;
import cn.oyzh.easyshell.data.oracle.dto.ShellOracleDataImportFile;
import cn.oyzh.easyshell.data.oracle.file.ShellOracleDataImportHelper;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.DBDataCsvTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataExcelTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataJsonTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataTxtTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataXmlTypeFileReader;
import cn.oyzh.fx.db.data.handler.DBDataImportHandler;
import cn.oyzh.fx.db.util.DBDataUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Oracle数据导入处理器
 *
 * @author oyzh
 * @since 2025-11-26
 */
public class ShellOracleDataImportHandler extends DBDataImportHandler<String> {

    /**
     * db客户端
     */
    private final ShellOracleClient dbClient;

    /**
     * 导入文件
     */
    private List<ShellOracleDataImportFile> files;

    /**
     * 导入配置
     */
    private final DBDataImportConfig config;

    /**
     * 构造 Oracle数据导入处理器
     *
     * @param dbClient 数据库客户端
     * @param name     名称
     */
    public ShellOracleDataImportHandler(ShellOracleClient dbClient, String name) {
        super(name);
        this.dbClient = dbClient;
        this.config = new DBDataImportConfig();
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Import Starting");
        if (CollectionUtil.isNotEmpty(this.files)) {
            for (ShellOracleDataImportFile file : files) {
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
    protected void importRecord(ShellOracleDataImportFile file) throws Exception {
        String tableName = file.getTargetTableName();
        try {
            this.message("Importing Table " + tableName);
            this.message("Importing Records of Table " + tableName);
            // 复制模式
            if (this.config.isCopyMode()) {
                this.dbClient.clearTable(this.name, tableName);
            }
            OracleColumns columns = new OracleColumns(this.dbClient.selectColumns(new OracleSelectColumnParam(file.getSchema(), tableName)));
            try (DBDataTypeFileReader reader = this.initReader(file.getFile())) {
                if (reader == null) {
                    return;
                }
                // 获取数据库表字段
                OracleColumns dbColumns = new OracleColumns(this.dbClient.selectColumns(new OracleSelectColumnParam(this.name, tableName)));
                if (!dbColumns.isEmpty()) {
                    while (true) {
                        this.checkInterrupt();
                        try {
                            long start1 = System.currentTimeMillis();
                            List<OracleRecord> records = this.readRecords(
                                    reader, this.readLimit, dbColumns);
                            if (CollectionUtil.isEmpty(records)) {
                                break;
                            }
                            long end1 = System.currentTimeMillis();
                            JulLog.info("读取耗时: {}ms", (end1 - start1));
                            long start2 = System.currentTimeMillis();
                            this.writeRecord(dbColumns, records);
                            long end2 = System.currentTimeMillis();
                            JulLog.info("写入耗时: {}ms", (end2 - start2));
//                            this.processed(records.size());
                        } catch (Exception ex) {
                            if (this.isContinueWithErrors()) {
                                this.exception(ex);
                                this.processedDecr();
                                this.getInsertList().clear();
                            } else {
                                throw ex;
                            }
                        }
                    }
                }
                // 收尾批量插入
                this.doBatchInsert();
            }
        } catch (Exception ex) {
            if (this.isContinueWithErrors()) {
                this.exception(ex);
                this.processedDecr();
                this.getInsertList().clear();
            } else {
                throw ex;
            }
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
    private List<OracleRecord> readRecords(DBDataTypeFileReader reader, int count,
                                           OracleColumns columns) throws Exception {
        List<OracleRecord> records = new ArrayList<>();
        List<Map<String, Object>> list = reader.readRecords(count);
        for (Map<String, Object> objectMap : list) {
            OracleRecord record = new OracleRecord(columns);
            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                OracleColumn column = columns.column(entry.getKey());
                if (column != null) {
                    record.putValue(column, entry.getValue());
                }
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
    private void writeRecord(OracleColumns columns, List<OracleRecord> records) throws Exception {
        if (this.requiresBindingImport(columns)) {
            for (OracleRecord record : records) {
                DBRecordData data = new DBRecordData();
                for (OracleColumn column : columns) {
                    if (!column.isDmlWritable()) {
                        continue;
                    }
                    Object value = this.convertBindValue(
                            column, record.getValue(column.getName()));
                    data.put(column, value);
                }
                OracleInsertRecordParam param = new OracleInsertRecordParam();
                param.setSchema(this.name);
                param.setTableName(columns.tableName());
                param.setRecord(data);
                int count = this.dbClient.insertRecord(param);
                this.processedIncr(count);
            }
            return;
        }
        List<String> sqlList = ShellOracleDataImportHelper.toInsertSql(columns, records, this.config);
        this.addInsert(sqlList);
    }

    private boolean requiresBindingImport(OracleColumns columns) {
        for (OracleColumn column : columns) {
            String type = column.getType() == null ? "" : column.getType().toUpperCase();
            if (column.supportText() || column.supportBinary() || column.isComplexType()
                    || column.supportJson() || "VECTOR".equals(type)
                    || type.contains("INTERVAL") || column.supportTimestamp()
                    || column.isDateType()) {
                return true;
            }
        }
        return false;
    }

    private Object convertBindValue(OracleColumn column, Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        if (text.isEmpty()) {
            return null;
        }
        String type = column.getType() == null ? "" : column.getType().toUpperCase();
        if (column.supportBinary()) {
            return value instanceof byte[] bytes ? bytes
                    : HexUtil.decodeHexStr(text.replaceFirst("^(?i)0x", ""));
        }
        if (column.supportBoolean()) {
            if (value instanceof Boolean bool) {
                return bool;
            }
            return "1".equals(text) || "true".equalsIgnoreCase(text);
        }
        if (column.supportJson()) {
            return DBDataUtil.normalizeJson(text);
        }
        if (column.supportText() || column.supportString()) {
            text = TextUtil.unescape(text);
        }
        if (column.isDateType() || column.supportTimestamp()) {
            if (value instanceof java.util.Date || value instanceof java.time.temporal.Temporal) {
                return value;
            }
            if (type.contains("WITH TIME ZONE")) {
                return java.time.OffsetDateTime.parse(
                        text.replace(' ', 'T'));
            }
            return Timestamp.valueOf(text);
        }
        if (column.supportInteger() || column.supportDigits()) {
            try {
                return new BigDecimal(text.trim());
            } catch (NumberFormatException ignored) {
                return value;
            }
        }
        return text;
    }

    //    /**
    //     * 添加插入sql
    //     *
    //     * @param sqlList 插入sql列表
    //     * @throws Exception 异常
    //     */
    //    private void addInsertSql(List<String> sqlList) throws Exception {
    //        if (CollectionUtil.isNotEmpty(sqlList)) {
    //            this.getInsertList().addAll(sqlList);
    //            if (this.getInsertList().size() >= this.batchLimit) {
    //                this.doBatchInsert();
    //            }
    //        }
    //    }

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
     * 设置数据起始行索引
     *
     * @param dataRowStarts 数据起始行索引
     */
    public void dataRowStarts(int dataRowStarts) {
        this.config.setDataRowStarts(dataRowStarts);
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
     * 获取数据库客户端
     *
     * @return 数据库客户端
     */
    public ShellOracleClient getDbClient() {
        return dbClient;
    }

    /**
     * 获取导入文件列表
     *
     * @return 导入文件列表
     */
    public List<ShellOracleDataImportFile> getFiles() {
        return files;
    }

    /**
     * 设置导入文件列表
     *
     * @param files 导入文件列表
     */
    public void setFiles(List<ShellOracleDataImportFile> files) {
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

    @Override
    public boolean enableParallel() {
        return false;
    }
}
