package cn.oyzh.easyshell.data.dameng.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.data.dameng.file.ShellDamengCsvTypeFileWriter;
import cn.oyzh.easyshell.data.dameng.file.ShellDamengExcelTypeFileWriter;
import cn.oyzh.easyshell.data.dameng.file.ShellDamengHtmlTypeFileWriter;
import cn.oyzh.easyshell.data.dameng.file.ShellDamengJsonTypeFileWriter;
import cn.oyzh.easyshell.data.dameng.file.ShellDamengSqlTypeFileWriter;
import cn.oyzh.easyshell.data.dameng.file.ShellDamengTxtTypeFileWriter;
import cn.oyzh.easyshell.data.dameng.file.ShellDamengTypeFileWriter;
import cn.oyzh.easyshell.data.dameng.file.ShellDamengXmlTypeFileWriter;
import cn.oyzh.easyshell.data.dameng.dto.ShellDamengDataExportTable;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.util.dameng.ShellDamengDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.handler.DBDataExportHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class ShellDamengDataExportHandler extends DBDataExportHandler {

    /**
     * db客户端
     */
    private ShellDamengClient dbClient;

    /**
     * 导出配置
     */
    private final DBDataExportConfig config;

    /**
     * 导出表
     */
    private List<ShellDamengDataExportTable> tables;

    public ShellDamengDataExportHandler(ShellDamengClient dbClient, String schema) {
        super(schema);
        this.dbClient = dbClient;
        this.config = new DBDataExportConfig();
    }

    @Override
    public void doExport() throws Exception {
        this.message("Export Starting");
        if (CollectionUtil.isNotEmpty(this.tables)) {
            for (ShellDamengDataExportTable table : this.tables) {
                this.checkInterrupt();
                this.exportTable(table);
                this.processedIncr();
            }
        }
        this.message("Export Finished");
    }

    private ShellDamengTypeFileWriter initWriter(String filePath, DamengColumns columns) throws IOException {
        if (this.isSqlType()) {
            return new ShellDamengSqlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isExcelType()) {
            return new ShellDamengExcelTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isHtmlType()) {
            return new ShellDamengHtmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsonType()) {
            return new ShellDamengJsonTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isXmlType()) {
            return new ShellDamengXmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isCsvType()) {
            return new ShellDamengCsvTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isTxtType()) {
            return new ShellDamengTxtTypeFileWriter(filePath, this.config, columns);
        }
        return null;
    }

    /**
     * 导出表
     *
     * @param table 表
     * @throws Exception 异常
     */
    protected void exportTable(ShellDamengDataExportTable table) throws Exception {
        String tableName = table.getName();
        this.message("Exporting Table " + table.getName());
        this.message("Exporting Records of Table " + table.getName());
        long start = 0;
        DamengColumns columns = new DamengColumns(table.selectedColumns());
        try (ShellDamengTypeFileWriter writer = this.initWriter(table.getFilePath(), columns)) {
            this.writeHeader(writer, table, columns);
            if (!columns.isEmpty()) {
                boolean stop = false;
                while (!stop) {
                    this.checkInterrupt();
                    try {
                        List<DamengRecord> records;
                        // 正常导出
                        if (table.getRecords() == null) {
                            long start1 = System.currentTimeMillis();
                            DamengSelectRecordParam param = new DamengSelectRecordParam();
                            param.setStart(start);
                            param.setReadonly(true);
                            param.setColumns(columns);
                            param.setSchema(this.name);
                            param.setTableName(tableName);
                            param.setLimit((long) this.queryLimit);
                            records = this.dbClient.selectRecords(param);
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
                        this.writeRecord(writer, table, columns, records);
                        long end2 = System.currentTimeMillis();
                        JulLog.info("写入耗时: {}ms", (end2 - start2));
                        start += this.queryLimit;
                        this.processed(records.size());
                    } catch (Exception ex) {
                        if (this.config.isContinueWithError()) {
                            this.exception(ex);
                        } else {
                            throw ex;
                        }
                    }
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
     * @param table   导出表
     * @param columns 字段列表
     * @throws IOException 异常
     */
    private void writeHeader(ShellDamengTypeFileWriter writer, ShellDamengDataExportTable table, DamengColumns columns) throws Exception {
        writer.writeHeader();
    }

    /**
     * 写入记录
     *
     * @param table   导出表
     * @param columns 字段列表
     * @param records 记录列表
     * @throws IOException 异常
     */
    private void writeRecord(ShellDamengTypeFileWriter writer, ShellDamengDataExportTable table, DamengColumns columns, List<DamengRecord> records) throws Exception {
        List<Map<String, Object>> objects = new ArrayList<>();
        for (DamengRecord object : records) {
            objects.add(object.toMap());
        }
        // 规整化值
        for (Map<String, Object> object : objects) {
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                entry.setValue(ShellDamengDataUtil.valueStandardization(entry.getValue()));
            }
        }
        writer.writeObjects(objects);
    }

    /**
     * 写入尾
     *
     * @throws IOException 异常
     */
    private void writeTail(ShellDamengTypeFileWriter writer) throws Exception {
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

    public void continueWithError(boolean continueWithError) {
        this.config.setContinueWithError(continueWithError);
    }

    public ShellDamengClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellDamengClient dbClient) {
        this.dbClient = dbClient;
    }

    public List<ShellDamengDataExportTable> getTables() {
        return tables;
    }

    public void setTables(List<ShellDamengDataExportTable> tables) {
        this.tables = tables;
    }

    public DBDataExportConfig getConfig() {
        return config;
    }

}
