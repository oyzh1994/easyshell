package cn.oyzh.easyshell.data.mongo.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.data.mongo.config.ShellMongoDataExportConfig;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.util.mongo.ShellMongoDataUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-04
 */
public class ShellMongoJsTypeFileWriter extends ShellMongoTypeFileWriter {

    /**
     * 字段列表
     */
    private MongoColumns columns;

    /**
     * 导出配置
     */
    private ShellMongoDataExportConfig config;

    /**
     * 文件读取器
     */
    private LineFileWriter writer;

    public ShellMongoJsTypeFileWriter(String filePath, ShellMongoDataExportConfig config, MongoColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        MongoRecord record = new MongoRecord(this.columns);
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            record.putValue(entry.getKey(), entry.getValue());
        }
        String script = ShellMongoDataUtil.toInsertScript(record);
        this.writer.writeLine(script);
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
    public Object parameterized(MongoColumn column, Object value, ShellMongoDataExportConfig config) {
        if (value == null) {
            return null;
        }
        return super.parameterized(column, value, config);
    }
}
