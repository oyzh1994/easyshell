package cn.oyzh.easyshell.db.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataExportTable;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.data.MysqlCsvTypeFileWriter;
import cn.oyzh.easyshell.mysql.data.MysqlDataExportConfig;
import cn.oyzh.easyshell.mysql.data.MysqlExcelTypeFileWriter;
import cn.oyzh.easyshell.mysql.data.MysqlHtmlTypeFileWriter;
import cn.oyzh.easyshell.mysql.data.MysqlJsonTypeFileWriter;
import cn.oyzh.easyshell.mysql.data.MysqlSqlTypeFileWriter;
import cn.oyzh.easyshell.mysql.data.MysqlTxtTypeFileWriter;
import cn.oyzh.easyshell.mysql.data.MysqlTypeFileWriter;
import cn.oyzh.easyshell.mysql.data.MysqlXmlTypeFileWriter;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class DBDataExportHandler extends DBDataHandler {

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
    private ShellMysqlClient dbClient;

    /**
     * 查询限制
     */
    private int queryLimit = 1000;

    /**
     * 导出表
     */
    private List<ShellMysqlDataExportTable> tables;

    // /**
    //  * xls行记录
    //  */
    // private int xlsRowIndex = 1;
    //
    // /**
    //  * xls工作薄
    //  */
    // private Workbook workbook;
    //
    // /**
    //  * 文件写入器
    //  */
    // private FastFileWriter writer;

    /**
     * 导出配置
     */
    private final MysqlDataExportConfig config;

    public DBDataExportHandler(ShellMysqlClient dbClient, String dbName) {
        this.dbClient = dbClient;
        this.dbName = dbName;
        this.config = new MysqlDataExportConfig();
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
     * 执行导出
     *
     * @throws Exception 异常
     */
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

    private MysqlTypeFileWriter initWriter(String filePath, MysqlColumns columns) throws IOException {
        if (this.isSqlType()) {
            return new MysqlSqlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isExcelType()) {
            return new MysqlExcelTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isHtmlType()) {
            return new MysqlHtmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isJsonType()) {
            return new MysqlJsonTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isXmlType()) {
            return new MysqlXmlTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isCsvType()) {
            return new MysqlCsvTypeFileWriter(filePath, this.config, columns);
        }
        if (this.isTxtType()) {
            return new MysqlTxtTypeFileWriter(filePath, this.config, columns);
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
        try (MysqlTypeFileWriter writer = this.initWriter(table.getFilePath(), columns)) {
            this.writeHeader(writer, table, columns);
            if (!columns.isEmpty()) {
                while (true) {
                    this.checkInterrupt();
                    long start1 = System.currentTimeMillis();
                    MysqlSelectRecordParam param = new MysqlSelectRecordParam();
                    param.setStart(start);
                    param.setReadonly(true);
                    param.setColumns(columns);
                    param.setDbName(this.dbName);
                    param.setTableName(tableName);
                    param.setLimit((long) this.queryLimit);
                    List<MysqlRecord> records = this.dbClient.selectRecords(param);
                    if (CollectionUtil.isEmpty(records)) {
                        break;
                    }
                    long end1 = System.currentTimeMillis();
                    JulLog.info("查询耗时: {}ms", (end1 - start1));
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
    private void writeHeader(MysqlTypeFileWriter writer, ShellMysqlDataExportTable table, MysqlColumns columns) throws Exception {
        // if (this.isJsonType()) {
        //     this.writer = new FastFileWriter(table.getFilePath());
        //     this.writer.appendLine("{");
        //     this.writer.appendLine(" \"RECORDS\": [");
        // } else if (this.isXmlType()) {
        //     this.writer = new FastFileWriter(table.getFilePath());
        //     this.writer.appendLine("<?xml version=\"1.0\" standalone=\"yes\"?>");
        //     this.writer.appendLine("<RECORDS>");
        // } else if (this.isCsvType()) {
        //     this.writer = new FastFileWriter(table.getFilePath());
        //     List<DBColumn> columnList = columns.sortOfPosition();
        //     StringBuilder builder = new StringBuilder();
        //     for (DBColumn dbColumn : columnList) {
        //         builder.append("\"").append(dbColumn.getName()).append("\",");
        //     }
        //     this.writer.appendLine(builder.substring(0, builder.length() - 1));
        // }

        // if (this.isHtmlType()) {
        //     this.writer = new FastFileWriter(table.getFilePath());
        //     String head = """
        //             <!DOCTYPE html>
        //             <html>
        //             <head>
        //             <meta charset="UTF-8">
        //             <style>
        //             table{
        //             border-collapse: collapse;
        //             width: 100%;
        //             }
        //             th, td{
        //             text-align: left;
        //             padding: 8px;
        //             }
        //             tr:nth-child(even){
        //             background-color: #fafafa;
        //             }
        //             th{
        //             background-color: #7799AA;
        //             color: white;
        //             }
        //             </style>
        //             </head>
        //             <body>
        //             <table>
        //             """;
        //     List<DBColumn> columnList = columns.sortOfPosition();
        //     StringBuilder builder = new StringBuilder(head);
        //     builder.append("\n<tr>");
        //     for (DBColumn dbColumn : columnList) {
        //         builder.append("<th>").append(dbColumn.getName()).append("</th>");
        //     }
        //     builder.append("</tr>");
        //     this.writer.appendLine(builder.toString());
        // }

        // if (this.isExcelType()) {
        //     // 创建一个新的Excel工作簿
        //     this.workbook = WorkbookHelper.create(this.isXlsxType());
        //     // 重置行索引
        //     this.xlsRowIndex = 1;
        //     // 创建一个新的工作表sheet
        //     Sheet sheet = this.workbook.createSheet(columns.getTableName());
        //     // 创建列名行
        //     Row headerRow = sheet.createRow(0);
        //     // 写入列名
        //     List<DBColumn> columnList = columns.sortOfPosition();
        //     for (int i = 0; i < columnList.size(); i++) {
        //         Cell cell = headerRow.createCell(i);
        //         cell.setCellValue(columnList.get(i).getName());
        //     }
        //     // 写入数据
        //     WorkbookHelper.write(this.workbook, table.getFilePath());
        // } else {
        writer.writeHeader();
        // }
    }

    /**
     * 写入记录
     *
     * @param table   导出表
     * @param columns 字段列表
     * @param records 记录列表
     * @throws IOException 异常
     */
    private void writeRecord(MysqlTypeFileWriter writer, ShellMysqlDataExportTable table, MysqlColumns columns, List<MysqlRecord> records) throws Exception {
        // if (this.isJsonType()) {
        //     this.writeJsonRecord(this.writer, columns, records);
        // }

        // if (this.isSqlType()) {
        //     this.writeSqlRecord(this.writer, columns, records);
        // } else if (this.isXmlType()) {
        //     this.writeXmlRecord(this.writer, columns, records);
        // } else if (this.isCsvType()) {
        //     this.writeCsvRecord(this.writer, columns, records);
        // } else if (this.isHtmlType()) {
        //     this.writeHtmlRecord(this.writer, columns, records);
        // } else if (this.isExcelType()) {
        //     this.writeExcelRecord(table, columns, records);
        // } else {
        List<Map<String, Object>> objects = new ArrayList<>();
        for (MysqlRecord object : records) {
            objects.add(object.toMap());
        }
        writer.writeObjects(objects);
        // }
    }

    /**
     * 写入尾
     *
     * @throws IOException 异常
     */
    private void writeTail(MysqlTypeFileWriter writer) throws Exception {
        writer.writeTrial();
        // writer.close();
    }
    //
    // /**
    //  * 写入sql记录
    //  *
    //  * @param writer  写入器
    //  * @param columns 字段列表
    //  * @param records 记录列表
    //  * @throws IOException 异常
    //  */
    // private void writeSqlRecord(FastFileWriter writer, DBColumns columns, List<DBRecord> records) throws IOException {
    //     List<String> inserts = DataExportHelper.toExportSql(columns, records, this.config);
    //     writer.appendLines(inserts);
    // }

    // /**
    //  * 写入json记录
    //  *
    //  * @param writer  写入器
    //  * @param columns 字段列表
    //  * @param records 记录列表
    //  * @throws IOException 异常
    //  */
    // private void writeJsonRecord(FastFileWriter writer, DBColumns columns, List<DBRecord> records) throws IOException, InterruptedException {
    //     List<Map<String, Object>> inserts = DataExportHelper.toExportJson(columns, records, this.config);
    //     for (int i = 0; i < inserts.size(); i++) {
    //         this.checkInterrupt();
    //         Map<String, Object> insert = inserts.get(i);
    //         StringBuilder builder = new StringBuilder("  {\n");
    //         int index = insert.size();
    //         for (Map.Entry<String, Object> entry : insert.entrySet()) {
    //             // 名称
    //             builder.append("   \"").append(entry.getKey()).append("\" : ");
    //             // 值处理
    //             Object value = entry.getValue();
    //             if (value != null) {
    //                 // 数字
    //                 if (value instanceof Number) {
    //                     builder.append(value);
    //                 } else {// 其他类型
    //                     builder.append("\"").append(value).append("\"");
    //                 }
    //             } else {
    //                 builder.append("null");
    //             }
    //             // 不是最后一个则拼接逗号
    //             if (--index > 0) {
    //                 builder.append(",");
    //             }
    //             builder.append("\n");
    //         }
    //         builder.append("  }");
    //         // 不是最后一个则拼接逗号
    //         if (i != inserts.size() - 1) {
    //             builder.append(",");
    //         }
    //         writer.appendLine(builder.toString());
    //     }
    // }

    // /**
    //  * 写入xml记录
    //  *
    //  * @param writer  写入器
    //  * @param columns 字段列表
    //  * @param records 记录列表
    //  * @throws IOException 异常
    //  */
    // private void writeXmlRecord(FastFileWriter writer, DBColumns columns, List<DBRecord> records) throws IOException, InterruptedException {
    //     List<Map<String, Object>> inserts = DataExportHelper.toExportXml(columns, records, this.config);
    //     for (Map<String, Object> insert : inserts) {
    //         this.checkInterrupt();
    //         StringBuilder builder = new StringBuilder("  <RECORD>\n");
    //         for (Map.Entry<String, Object> entry : insert.entrySet()) {
    //             // 名称
    //             builder.append("   <").append(entry.getKey());
    //             // 值处理
    //             Object value = entry.getValue();
    //             if (value != null) {
    //                 builder.append(">");
    //                 builder.append(value);
    //                 builder.append("</").append(entry.getKey()).append(">");
    //             } else {
    //                 builder.append("/>");
    //             }
    //             builder.append("\n");
    //         }
    //         builder.append("  </RECORD>");
    //         writer.appendLine(builder.toString());
    //     }
    // }
    //
    // /**
    //  * 写入csv记录
    //  *
    //  * @param writer  写入器
    //  * @param columns 字段列表
    //  * @param records 记录列表
    //  * @throws IOException 异常
    //  */
    // private void writeCsvRecord(FastFileWriter writer, DBColumns columns, List<DBRecord> records) throws IOException, InterruptedException {
    //     List<List<Object>> inserts = DataExportHelper.toExportCsv(columns, records, this.config);
    //     for (List<Object> insert : inserts) {
    //         this.checkInterrupt();
    //         StringBuilder builder = new StringBuilder();
    //         for (Object val : insert) {
    //             builder.append(val).append(",");
    //         }
    //         writer.appendLine(builder.substring(0, builder.length() - 1));
    //     }
    // }

    // /**
    //  * 写入html记录
    //  *
    //  * @param writer  写入器
    //  * @param columns 字段列表
    //  * @param records 记录列表
    //  * @throws IOException 异常
    //  */
    // private void writeHtmlRecord(FastFileWriter writer, DBColumns columns, List<DBRecord> records) throws IOException, InterruptedException {
    //     List<List<Object>> inserts = DataExportHelper.toExportHtml(columns, records, this.config);
    //     for (List<Object> insert : inserts) {
    //         this.checkInterrupt();
    //         StringBuilder builder = new StringBuilder();
    //         builder.append("<tr>");
    //         for (Object val : insert) {
    //             builder.append("<td>").append(val).append("</td>");
    //         }
    //         builder.append("</tr>");
    //         writer.appendLine(builder.toString());
    //     }
    // }

    // /**
    //  * 写入excel记录
    //  *
    //  * @param columns 字段列表
    //  * @param records 记录列表
    //  * @throws IOException 异常
    //  */
    // private void writeExcelRecord(ShellMysqlDataExportTable table, DBColumns columns, List<DBRecord> records) throws IOException, InterruptedException, InvalidFormatException {
    //     List<List<Object>> inserts = DataExportHelper.toExportXls(columns, records, this.config);
    //     Sheet sheet = WorkbookHelper.getActiveSheet(this.workbook);
    //     for (List<Object> insert : inserts) {
    //         this.checkInterrupt();
    //         Row row = sheet.createRow(this.xlsRowIndex++);
    //         // 创建数据行
    //         for (int i = 0; i < insert.size(); i++) {
    //             Cell cell = row.createCell(i);
    //             Object val = insert.get(i);
    //             switch (val) {
    //                 case null -> {
    //                 }
    //                 case Date v -> cell.setCellValue(v);
    //                 case Double v -> cell.setCellValue(v);
    //                 case String v -> cell.setCellValue(v);
    //                 case Boolean v -> cell.setCellValue(v);
    //                 case Calendar v -> cell.setCellValue(v);
    //                 case LocalDate v -> cell.setCellValue(v);
    //                 case LocalDateTime v -> cell.setCellValue(v);
    //                 case Number v -> cell.setCellValue(v.doubleValue());
    //                 default -> cell.setCellValue(val.toString());
    //             }
    //         }
    //     }
    //     // 写入数据
    //     WorkbookHelper.write(this.workbook, table.getFilePath());
    // }

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

    public ShellMysqlClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(ShellMysqlClient dbClient) {
        this.dbClient = dbClient;
    }

    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }

    public List<ShellMysqlDataExportTable> getTables() {
        return tables;
    }

    public void setTables(List<ShellMysqlDataExportTable> tables) {
        this.tables = tables;
    }

    public MysqlDataExportConfig getConfig() {
        return config;
    }
}

