package cn.oyzh.easyshell.data.mysql.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.mysql.config.ShellMysqlDataExportConfig;
import cn.oyzh.easyshell.data.mysql.file.ShellMysqlCsvTypeFileWriter;
import cn.oyzh.easyshell.data.mysql.file.ShellMysqlExcelTypeFileWriter;
import cn.oyzh.easyshell.data.mysql.file.ShellMysqlHtmlTypeFileWriter;
import cn.oyzh.easyshell.data.mysql.file.ShellMysqlJsonTypeFileWriter;
import cn.oyzh.easyshell.data.mysql.file.ShellMysqlSqlTypeFileWriter;
import cn.oyzh.easyshell.data.mysql.file.ShellMysqlTxtTypeFileWriter;
import cn.oyzh.easyshell.data.mysql.file.ShellMysqlTypeFileWriter;
import cn.oyzh.easyshell.data.mysql.file.ShellMysqlXmlTypeFileWriter;
import cn.oyzh.easyshell.data.db.handler.DBDataExportHandler;
import cn.oyzh.easyshell.data.mysql.ui.ShellMysqlDataExportTable;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author oyzh
 * @since 2025-11-26
 */
public class ShellMysqlDataExportHandler extends DBDataExportHandler {

    /**
     * db客户端
     */
    private ShellMysqlClient dbClient;

    /**
     * 导出配置
     */
    private final ShellMysqlDataExportConfig config;

    /**
     * 导出表
     */
    private List<ShellMysqlDataExportTable> tables;

    public ShellMysqlDataExportHandler(ShellMysqlClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
        this.config = new ShellMysqlDataExportConfig();
    }

    @Override
    public void doExport() throws Exception {
        this.message("Export Starting");
        if (CollectionUtil.isNotEmpty(this.tables)) {
            for (ShellMysqlDataExportTable table : this.tables) {
                this.checkInterrupt();
                this.exportTable(table);
                this.processedIncr();
            }
        }
        this.message("Export Finished");
    }

    private ShellMysqlTypeFileWriter initWriter(String filePath, MysqlColumns columns) throws IOException {
        if (this.isSqlType()) {
            return new ShellMysqlSqlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isExcelType()) {
            return new ShellMysqlExcelTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isHtmlType()) {
            return new ShellMysqlHtmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsonType()) {
            return new ShellMysqlJsonTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isXmlType()) {
            return new ShellMysqlXmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isCsvType()) {
            return new ShellMysqlCsvTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isTxtType()) {
            return new ShellMysqlTxtTypeFileWriter(filePath, this.config, columns);
        }
        return null;
    }

    /**
     * 导出表
     *
     * @param table 表
     * @throws Exception 异常
     */
    protected void exportTable(ShellMysqlDataExportTable table) throws Exception {
        String tableName = table.getName();
        this.message("Exporting Table " + table.getName());
        this.message("Exporting Records of Table " + table.getName());
        long start = 0;
        MysqlColumns columns = new MysqlColumns(table.selectedColumns());
        try (ShellMysqlTypeFileWriter writer = this.initWriter(table.getFilePath(), columns)) {
            this.writeHeader(writer, table, columns);
            if (!columns.isEmpty()) {
                boolean stop = false;
                while (!stop) {
                    this.checkInterrupt();
                    List<MysqlRecord> records;
                    // 正常导出
                    if (table.getRecords() == null) {
                        long start1 = System.currentTimeMillis();
                        MysqlSelectRecordParam param = new MysqlSelectRecordParam();
                        param.setStart(start);
                        param.setReadonly(true);
                        param.setColumns(columns);
                        param.setDbName(this.dbName);
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
    private void writeHeader(ShellMysqlTypeFileWriter writer, ShellMysqlDataExportTable table, MysqlColumns columns) throws Exception {
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
    private void writeRecord(ShellMysqlTypeFileWriter writer, ShellMysqlDataExportTable table, MysqlColumns columns, List<MysqlRecord> records) throws Exception {
        List<Map<String, Object>> objects = new ArrayList<>();
        for (MysqlRecord object : records) {
            objects.add(object.toMap());
        }
        writer.writeObjects(objects);
    }

    /**
     * 写入尾
     *
     * @throws IOException 异常
     */
    private void writeTail(ShellMysqlTypeFileWriter writer) throws Exception {
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

    public ShellMysqlClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellMysqlClient dbClient) {
        this.dbClient = dbClient;
    }

    public List<ShellMysqlDataExportTable> getTables() {
        return tables;
    }

    public void setTables(List<ShellMysqlDataExportTable> tables) {
        this.tables = tables;
    }

    public ShellMysqlDataExportConfig getConfig() {
        return config;
    }

}
