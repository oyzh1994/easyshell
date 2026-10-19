package cn.oyzh.easyshell.data.mariadb.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Mariadb Html类型文件写入器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbHtmlTypeFileWriter extends ShellMariadbTypeFileWriter {

    /**
     * 字段列表
     */
    private MariadbColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private LineFileWriter writer;

    /**
     * 构造 Mariadb Html类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellMariadbHtmlTypeFileWriter(String filePath, DBDataExportConfig config, MariadbColumns columns) throws FileNotFoundException {
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
        List<MariadbColumn> columnList = columns.sortOfPosition();
        StringBuilder builder = new StringBuilder(head);
        builder.append("\n<tr>");
        for (MariadbColumn dbColumn : columnList) {
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
        Object[] values = new Object[this.columns.size()];
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            int index = this.columns.index(entry.getKey());
            MariadbColumn column = this.columns.column(entry.getKey());
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
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
            this.config = null;
            this.columns = null;
        }
    }
}
