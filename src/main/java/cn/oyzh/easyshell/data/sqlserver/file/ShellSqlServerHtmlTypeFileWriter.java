package cn.oyzh.easyshell.data.sqlserver.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * SqlServer Html类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerHtmlTypeFileWriter extends ShellSqlServerTypeFileWriter {

    /**
     * 字段列表
     */
    private SqlServerColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private LineFileWriter writer;

    /**
     * 构造 SqlServer Html类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellSqlServerHtmlTypeFileWriter(String filePath, DBDataExportConfig config, SqlServerColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeHeader() throws Exception {
        String head = """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <style>
                table{
                border-collapse: collapse;
                width: 100%;
                }
                th, td{
                text-align: left;
                padding: 8px;
                }
                tr:nth-child(even){
                background-color: #fafafa;
                }
                th{
                background-color: #7799AA;
                color: white;
                }
                </style>
                </head>
                <body>
                <table>
                """;
        List<SqlServerColumn> columnList = columns.sortOfPosition();
        StringBuilder builder = new StringBuilder(head);
        builder.append("\n<tr>");
        for (SqlServerColumn dbColumn : columnList) {
            builder.append("<th>").append(dbColumn.getName()).append("</th>");
        }
        builder.append("</tr>");
        this.writer.writeLine(builder.toString());
    }

    @Override
    public void writeTrial() throws Exception {
        String tail = """
                </table>
                </body>
                </html>
                """;
        this.writer.writeLine(tail);
    }

    @Override
    public void writeRecord(Map<String, Object> object) throws Exception {
        StringBuilder builder = new StringBuilder("<tr>");
        Object[] values = new Object[this.columns.size()];
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            int index = this.columns.index(entry.getKey());
            SqlServerColumn column = this.columns.column(entry.getKey());
            Object val = this.parameterized(column, entry.getValue(), this.config);
            values[index] = val;
        }
        for (Object val : values) {
            builder.append("<td>").append(this.escapeHtml(val)).append("</td>");
        }
        builder.append("</tr>");
        this.writer.writeLine(builder.toString());
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        this.writer = null;
        this.config = null;
        this.columns = null;
    }

    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
