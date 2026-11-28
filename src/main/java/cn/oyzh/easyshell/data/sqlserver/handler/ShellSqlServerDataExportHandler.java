package cn.oyzh.easyshell.data.sqlserver.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.data.sqlserver.dto.ShellSqlServerDataExportTable;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerCsvTypeFileWriter;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerExcelTypeFileWriter;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerHtmlTypeFileWriter;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerJsonTypeFileWriter;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerSqlTypeFileWriter;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerTxtTypeFileWriter;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerTypeFileWriter;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerXmlTypeFileWriter;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.handler.DBDataExportHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SqlServer数据导出处理器
 *
 * @author oyzh
 * @since 2025-11-26
 */
public class ShellSqlServerDataExportHandler extends DBDataExportHandler {

    /**
     * db客户端
     */
    private ShellSqlServerClient dbClient;

    /**
     * 导出配置
     */
    private final DBDataExportConfig config;

    /**
     * 导出表
     */
    private List<ShellSqlServerDataExportTable> tables;

    /**
     * 构造 SqlServer数据导出处理器
     *
     * @param dbClient 数据库客户端
     * @param schema   模式名称
     */
    public ShellSqlServerDataExportHandler(ShellSqlServerClient dbClient, String schema) {
        super(schema);
        this.dbClient = dbClient;
        this.config = new DBDataExportConfig();
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Export Starting");
        if (CollectionUtil.isNotEmpty(this.tables)) {
            for (ShellSqlServerDataExportTable table : this.tables) {
                this.checkInterrupt();
                this.exportTable(table);
                this.processedIncr();
            }
        }
        this.message("Export Finished");
    }

    /**
     * 初始化写入器
     *
     * @param filePath 文件路径
     * @param columns  字段列表
     * @return 类型文件写入器
     * @throws IOException IO异常
     */
    private ShellSqlServerTypeFileWriter initWriter(String filePath, SqlServerColumns columns) throws IOException {
        if (this.isSqlType()) {
            return new ShellSqlServerSqlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isExcelType()) {
            return new ShellSqlServerExcelTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isHtmlType()) {
            return new ShellSqlServerHtmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsonType()) {
            return new ShellSqlServerJsonTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isXmlType()) {
            return new ShellSqlServerXmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isCsvType()) {
            return new ShellSqlServerCsvTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isTxtType()) {
            return new ShellSqlServerTxtTypeFileWriter(filePath, this.config, columns);
        }
        return null;
    }

    /**
     * 导出表
     *
     * @param table 表
     * @throws Exception 异常
     */
    protected void exportTable(ShellSqlServerDataExportTable table) throws Exception {
        String tableName = table.getName();
        this.message("Exporting Table " + table.getName());
        this.message("Exporting Records of Table " + table.getName());
        long start = 0;
        SqlServerColumns columns = new SqlServerColumns(table.selectedColumns());
        try (ShellSqlServerTypeFileWriter writer = this.initWriter(table.getFilePath(), columns)) {
            this.writeHeader(writer, table, columns);
            if (!columns.isEmpty()) {
                boolean stop = false;
                while (!stop) {
                    this.checkInterrupt();
                    try {
                        List<SqlServerRecord> records;
                        // 正常导出
                        if (table.getRecords() == null) {
                            long start1 = System.currentTimeMillis();
                            SqlServerSelectRecordParam param = new SqlServerSelectRecordParam();
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
     * @param writer  类型文件写入器
     * @param table   导出表
     * @param columns 字段列表
     * @throws Exception 异常
     */
    private void writeHeader(ShellSqlServerTypeFileWriter writer, ShellSqlServerDataExportTable table, SqlServerColumns columns) throws Exception {
        if (writer instanceof ShellSqlServerSqlTypeFileWriter sqlTypeFileWriter) {
            sqlTypeFileWriter.setTable(table.getName());
        }
        writer.writeHeader();
    }

    /**
     * 写入尾
     *
     * @param writer 类型文件写入器
     * @throws Exception 异常
     */
    private void writeTail(ShellSqlServerTypeFileWriter writer) throws Exception {
        writer.writeTrial();
        if (writer instanceof ShellSqlServerSqlTypeFileWriter sqlTypeFileWriter) {
            sqlTypeFileWriter.setTable(null);
        }
    }

    /**
     * 写入记录
     *
     * @param writer  类型文件写入器
     * @param table   导出表
     * @param columns 字段列表
     * @param records 记录列表
     * @throws Exception 异常
     */
    private void writeRecord(ShellSqlServerTypeFileWriter writer, ShellSqlServerDataExportTable table, SqlServerColumns columns, List<SqlServerRecord> records) throws Exception {
        List<Map<String, Object>> objects = new ArrayList<>();
        for (SqlServerRecord object : records) {
            objects.add(object.toMap());
        }
        // 规整化值
        for (Map<String, Object> object : objects) {
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                entry.setValue(ShellSqlServerDataUtil.valueStandardization(entry.getValue()));
            }
        }
        writer.writeRecords(objects);
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
     * 设置是否包含标题
     *
     * @param includeTitle 是否包含标题
     */
    public void includeTitle(boolean includeTitle) {
        this.config.setIncludeTitle(includeTitle);
    }

    /**
     * 设置属性作为字段
     *
     * @param fieldToAttr 属性作为字段
     */
    public void fieldToAttr(boolean fieldToAttr) {
        this.config.setFieldToAttr(fieldToAttr);
    }

    /**
     * 设置是否早期版本
     *
     * @param earlyVersion 是否早期版本
     */
    public void earlyVersion(boolean earlyVersion) {
        this.config.setEarlyVersion(earlyVersion);
    }

    /**
     * 设置是否遇错继续
     *
     * @param continueWithError 是否遇错继续
     */
    public void continueWithError(boolean continueWithError) {
        this.config.setContinueWithError(continueWithError);
    }

    /**
     * 获取数据库客户端
     *
     * @return 数据库客户端
     */
    public ShellSqlServerClient getDbClient() {
        return dbClient;
    }

    /**
     * 设置数据库客户端
     *
     * @param dbClient 数据库客户端
     */
    public void setDbClient(ShellSqlServerClient dbClient) {
        this.dbClient = dbClient;
    }

    /**
     * 获取导出表列表
     *
     * @return 导出表列表
     */
    public List<ShellSqlServerDataExportTable> getTables() {
        return tables;
    }

    /**
     * 设置导出表列表
     *
     * @param tables 导出表列表
     */
    public void setTables(List<ShellSqlServerDataExportTable> tables) {
        this.tables = tables;
    }

    /**
     * 获取导出配置
     *
     * @return 导出配置
     */
    public DBDataExportConfig getConfig() {
        return config;
    }

}
