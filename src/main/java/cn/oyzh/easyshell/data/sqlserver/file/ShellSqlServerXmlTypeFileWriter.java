package cn.oyzh.easyshell.data.sqlserver.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * SqlServer Xml类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerXmlTypeFileWriter extends ShellSqlServerTypeFileWriter {

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
     * 构造 SqlServer Xml类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellSqlServerXmlTypeFileWriter(String filePath, DBDataExportConfig config, SqlServerColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeHeader() throws Exception {
        this.writer.writeLine("<?xml version=\"1.0\" standalone=\"yes\"?>");
        this.writer.writeLine("<RECORDS>");
    }

    @Override
    public void writeTrial() throws Exception {
        this.writer.writeLine("</RECORDS>");
    }

    @Override
    public void writeRecord(Map<String, Object> object) throws Exception {
        StringBuilder builder;
        if (this.config.isFieldToAttr()) {
            builder = new StringBuilder("  <RECORD ");
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                // 值处理
                SqlServerColumn column = this.columns.column(entry.getKey());
                Object val = this.parameterized(column, entry.getValue(), this.config);
                if (val != null) {
                    builder.append(entry.getKey())
                            .append("=\"")
                            .append(this.escapeXml(val.toString()))
                            .append("\" ");
                }
            }
            builder.append(" />");
        } else {
            builder = new StringBuilder("  <RECORD>\n");
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                // 名称
                builder.append("   <").append(entry.getKey());
                // 值处理
                SqlServerColumn column = this.columns.column(entry.getKey());
                Object val = this.parameterized(column, entry.getValue(), this.config);
                if (val != null) {
                    builder.append(">");
                    builder.append(this.escapeXml(val.toString()));
                    builder.append("</").append(entry.getKey()).append(">");
                } else {
                    builder.append("/>");
                }
                builder.append("\n");
            }
            builder.append("  </RECORD>");
        }
        this.writer.writeLine(builder.toString());
    }

    /**
     * 转义XML文本和属性值。
     *
     * @param value 值
     * @return 转义结果
     */
    private String escapeXml(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Override
    public void close() throws IOException {
        IOUtil.close(this.writer);
        this.writer = null;
        this.config = null;
        this.columns = null;
    }

//    @Override
//    public Object parameterized(SqlServerColumn column, Object value, DBDataExportConfig config) {
//        if (value == null) {
//            return null;
//        }
//        return super.parameterized(column, value, config);
//    }
}
