package cn.oyzh.easyshell.mysql.data;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-04
 */
public class MysqlHtmlTypeFileWriter extends MysqlTypeFileWriter {

    /**
     * 字段列表
     */
    private MysqlColumns columns;

    /**
     * 导出配置
     */
    private MysqlDataExportConfig config;

    /**
     * 文件写入器
     */
    private LineFileWriter writer;

    public MysqlHtmlTypeFileWriter(String filePath, MysqlDataExportConfig config, MysqlColumns columns) throws FileNotFoundException {
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
        List<MysqlColumn> columnList = columns.sortOfPosition();
        StringBuilder builder = new StringBuilder(head);
        builder.append("\n<tr>");
        for (MysqlColumn dbColumn : columnList) {
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
    public void writeObject(Map<String, Object> object) throws Exception {
        StringBuilder builder = new StringBuilder("<tr>");
        Object[] values = new Object[object.size()];
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            int index = this.columns.index(entry.getKey());
            MysqlColumn column = this.columns.column(entry.getKey());
            Object val = this.parameterized(column, entry.getValue(), this.config);
            values[index] = val;
        }
        for (Object val : values) {
            builder.append("<td>").append(val).append("</td>");
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
}
