package cn.oyzh.easyshell.mysql.data;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-04
 */
public class MysqlXmlTypeFileWriter extends MysqlTypeFileWriter {

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

    public MysqlXmlTypeFileWriter(String filePath, MysqlDataExportConfig config, MysqlColumns columns) throws FileNotFoundException {
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
    public void writeObject(Map<String, Object> object) throws Exception {
        StringBuilder builder;
        if (this.config.isFieldToAttr()) {
            builder = new StringBuilder("  <RECORD ");
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                // 值处理
                MysqlColumn column = this.columns.column(entry.getKey());
                Object val = this.parameterized(column, entry.getValue(), this.config);
                if (val != null) {
                    builder.append(entry.getKey())
                            .append("=\"")
                            .append(val)
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
                MysqlColumn column = this.columns.column(entry.getKey());
                Object val = this.parameterized(column, entry.getValue(), this.config);
                if (val != null) {
                    builder.append(">");
                    builder.append(val);
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

    @Override
    public void close() throws IOException {
        IOUtil.close(this.writer);
        this.writer = null;
        this.config = null;
        this.columns = null;
    }

    @Override
    public Object parameterized(MysqlColumn column, Object value, MysqlDataExportConfig config) {
        if (value == null) {
            return null;
        }
        return super.parameterized(column, value, config);
    }
}
