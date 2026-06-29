package cn.oyzh.easyshell.data.mongo.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.data.mongo.config.MongoDataExportConfig;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.util.mongo.MongoUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-04
 */
public class MongoJsonTypeFileWriter extends MongoTypeFileWriter {

    /**
     * 字段列表
     */
    private MongoColumns columns;

    /**
     * 导出配置
     */
    private MongoDataExportConfig config;

    /**
     * 文件读取器
     */
    private LineFileWriter writer;

    /**
     * 是否首次写入
     */
    private boolean firstWrite = true;

    public MongoJsonTypeFileWriter(String filePath, MongoDataExportConfig config, MongoColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeHeader() throws Exception {
        if (this.config.isEarlyVersion()) {
            this.writer.writeLine("{");
            this.writer.writeLine(" \"RECORDS\": [");
        } else {
            this.writer.writeLine("[");
        }
    }

    @Override
    public void writeTrial() throws Exception {
        if (this.config.isEarlyVersion()) {
            this.writer.write("\n]}");
        } else {
            this.writer.write("\n]");
        }
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        if (!this.firstWrite) {
            this.writer.write(",\n");
        }
        int size = object.size();
        StringBuilder builder = new StringBuilder("  {\n");
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            // 名称
            builder.append("   \"").append(entry.getKey()).append("\" : ");
            // 值处理
            MongoColumn column = this.columns.column(entry.getKey());
            Object val = this.parameterized(column, entry.getValue(), this.config);
            if (val != null) {
                if (MongoUtil.isPrimaryType(entry.getValue())) {
                    builder.append(val);
                } else {// 其他类型
                    builder.append("\"").append(val).append("\"");
                }
            } else {
                builder.append("null");
            }
            if (--size != 0) {
                builder.append(",\n");
            } else {
                builder.append("\n");
            }
        }
        builder.append("  }");
        this.writer.write(builder.toString());
        this.firstWrite = false;
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

    @Override
    public Object parameterized(MongoColumn column, Object value, MongoDataExportConfig config) {
        if (value == null) {
            return null;
        }
        return super.parameterized(column, value, config);
    }
}
