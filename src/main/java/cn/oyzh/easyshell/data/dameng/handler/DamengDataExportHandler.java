package cn.oyzh.easyshell.data.dameng.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.data.DamengCsvTypeFileWriter;
import cn.oyzh.easyshell.dameng.data.DamengDataExportConfig;
import cn.oyzh.easyshell.dameng.data.DamengExcelTypeFileWriter;
import cn.oyzh.easyshell.dameng.data.DamengHtmlTypeFileWriter;
import cn.oyzh.easyshell.dameng.data.DamengJsonTypeFileWriter;
import cn.oyzh.easyshell.dameng.data.DamengSqlTypeFileWriter;
import cn.oyzh.easyshell.dameng.data.DamengTxtTypeFileWriter;
import cn.oyzh.easyshell.dameng.data.DamengTypeFileWriter;
import cn.oyzh.easyshell.dameng.data.DamengXmlTypeFileWriter;
import cn.oyzh.easyshell.dameng.dto.ShellDamengDataExportTable;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.util.dameng.DamengDataUtil;
import cn.oyzh.fx.db.data.handler.DBDataExportHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class DamengDataExportHandler extends DBDataExportHandler {

    /**
     * db客户端
     */
    private ShellDamengClient dbClient;

    /**
     * 导出配置
     */
    private final DamengDataExportConfig config;

    /**
     * 导出表
     */
    private List<ShellDamengDataExportTable> tables;

    public DamengDataExportHandler(ShellDamengClient dbClient, String schema) {
        super(schema);
        this.dbClient = dbClient;
        this.config = new DamengDataExportConfig();
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

    private DamengTypeFileWriter initWriter(String filePath, DamengColumns columns) throws IOException {
        if (this.isSqlType()) {
            return new DamengSqlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isExcelType()) {
            return new DamengExcelTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isHtmlType()) {
            return new DamengHtmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsonType()) {
            return new DamengJsonTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isXmlType()) {
            return new DamengXmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isCsvType()) {
            return new DamengCsvTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isTxtType()) {
            return new DamengTxtTypeFileWriter(filePath, this.config, columns);
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
        try (DamengTypeFileWriter writer = this.initWriter(table.getFilePath(), columns)) {
            this.writeHeader(writer, table, columns);
            if (!columns.isEmpty()) {
                boolean stop = false;
                while (!stop) {
                    this.checkInterrupt();
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
    private void writeHeader(DamengTypeFileWriter writer, ShellDamengDataExportTable table, DamengColumns columns) throws Exception {
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
    private void writeRecord(DamengTypeFileWriter writer, ShellDamengDataExportTable table, DamengColumns columns, List<DamengRecord> records) throws Exception {
        List<Map<String, Object>> objects = new ArrayList<>();
        for (DamengRecord object : records) {
            objects.add(object.toMap());
        }
        // 规整化值
        for (Map<String, Object> object : objects) {
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                entry.setValue(DamengDataUtil.valueStandardization(entry.getValue()));
            }
        }
        writer.writeObjects(objects);
    }

    /**
     * 写入尾
     *
     * @throws IOException 异常
     */
    private void writeTail(DamengTypeFileWriter writer) throws Exception {
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

    public DamengDataExportConfig getConfig() {
        return config;
    }

}
