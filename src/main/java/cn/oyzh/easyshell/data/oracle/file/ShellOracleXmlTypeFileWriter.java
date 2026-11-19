package cn.oyzh.easyshell.data.oracle.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Oracle Xml类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleXmlTypeFileWriter extends ShellOracleTypeFileWriter {

    /**
     * 字段列表
     */
    private OracleColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private LineFileWriter writer;

    /**
     * 构造 Oracle Xml类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellOracleXmlTypeFileWriter(String filePath, DBDataExportConfig config, OracleColumns columns) throws FileNotFoundException {
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
                OracleColumn column = this.columns.column(entry.getKey());
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
                OracleColumn column = this.columns.column(entry.getKey());
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
    public Object parameterized(OracleColumn column, Object value, DBDataExportConfig config) {
        if (column == null) {
            return value;
        }
        if (value == null) {
            return null;
        }
        value = ShellOracleDataUtil.valueStandardization(value);
        if (column.isObjectType() || column.isCollectionType()) {
            String json = value instanceof CharSequence sequence
                    ? sequence.toString() : JSONUtil.toJson(value);
            return xmlEscape(json);
        }
        if (column.supportJson()) {
            String json = value instanceof CharSequence sequence
                    ? sequence.toString() : JSONUtil.toJson(value);
            return xmlEscape(json);
        }
        if (column.supportString()) {
            return xmlEscape(value.toString());
        }
        return super.parameterized(column, value, config);
    }

    private static String xmlEscape(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> builder.append("&amp;");
                case '<' -> builder.append("&lt;");
                case '>' -> builder.append("&gt;");
                case '"' -> builder.append("&quot;");
                case '\'' -> builder.append("&apos;");
                default -> {
                    if (c < 0x20 && c != '\n' && c != '\r' && c != '\t') {
                        builder.append(' ');
                    } else {
                        builder.append(c);
                    }
                }
            }
        }
        return builder.toString();
    }

    @Override
    public void close() throws IOException {
        IOUtil.close(this.writer);
        this.writer = null;
        this.config = null;
        this.columns = null;
    }

//    @Override
//    public Object parameterized(OracleColumn column, Object value, DBDataExportConfig config) {
//        if (value == null) {
//            return null;
//        }
//        return super.parameterized(column, value, config);
//    }
}
