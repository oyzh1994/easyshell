package cn.oyzh.easyshell.data.mongo.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Mongo Json类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoJsonTypeFileWriter extends ShellMongoTypeFileWriter {

    private static final JsonWriterSettings JSON_SETTINGS = JsonWriterSettings.builder()
            .outputMode(JsonMode.RELAXED)
            .indent(true)
            .build();

    /**
     * 字段列表
     */
    private MongoColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件读取器
     */
    private LineFileWriter writer;

    /**
     * 是否首次写入
     */
    private boolean firstWrite = true;

    /**
     * 构造 Mongo Json类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellMongoJsonTypeFileWriter(String filePath, DBDataExportConfig config, MongoColumns columns) throws FileNotFoundException {
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
    public void writeRecord(Map<String, Object> object) throws Exception {
        if (!this.firstWrite) {
            this.writer.write(",\n");
        }
        Document document = new Document();
        for (MongoColumn column : this.columns) {
            if (object.containsKey(column.getName())) {
                document.put(column.getName(), object.get(column.getName()));
            }
        }
        this.writer.write(document.toJson(JSON_SETTINGS));
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

//    @Override
//    public Object parameterized(MongoColumn column, Object value, DBDataExportConfig config) {
//        if (value == null) {
//            return null;
//        }
//        return super.parameterized(column, value, config);
//    }
}
