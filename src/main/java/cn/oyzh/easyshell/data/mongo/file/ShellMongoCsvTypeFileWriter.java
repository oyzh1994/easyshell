package cn.oyzh.easyshell.data.mongo.file;

import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.file.csv.DBDataCsvFileWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Mongo CSV类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoCsvTypeFileWriter extends ShellMongoTypeFileWriter {

    /**
     * 字段列表
     */
    private MongoColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * CSV 写入器
     */
    private final DBDataCsvFileWriter writer;

    /**
     * 构造 Mongo CSV类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws IOException IO 异常
     */
    public ShellMongoCsvTypeFileWriter(String filePath, DBDataExportConfig config, MongoColumns columns) throws IOException {
        this.columns = columns;
        this.config = config;
        this.writer = new DBDataCsvFileWriter(filePath, config);
    }

    @Override
    public void writeHeader() throws Exception {
        this.writer.write(this.columns.columnNames());
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        Object[] values = new Object[this.columns.size()];
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            int index = this.columns.index(entry.getKey());
            MongoColumn column = this.columns.column(entry.getKey());
            Object val = this.parameterized(column, entry.getValue(), this.config);
            values[index] = val;
        }
        this.writer.write(values);
    }

    @Override
    public void close() throws IOException {
        if (this.writer != null) {
            this.writer.close();
            this.config = null;
            this.columns = null;
        }
    }
}
