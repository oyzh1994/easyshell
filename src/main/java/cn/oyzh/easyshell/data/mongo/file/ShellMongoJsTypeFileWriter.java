package cn.oyzh.easyshell.data.mongo.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.util.mongo.ShellMongoDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Mongo Js类型文件写入器
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoJsTypeFileWriter extends ShellMongoTypeFileWriter {

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
     * 构造 Mongo Js类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellMongoJsTypeFileWriter(String filePath, DBDataExportConfig config, MongoColumns columns) throws FileNotFoundException {
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

//    @Override
//    public Object parameterized(MongoColumn column, Object value, DBDataExportConfig config) {
//        if (value == null) {
//            return null;
//        }
//        return super.parameterized(column, value, config);
//    }
}
