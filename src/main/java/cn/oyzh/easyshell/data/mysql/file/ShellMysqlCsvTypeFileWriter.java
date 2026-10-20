package cn.oyzh.easyshell.data.mysql.file;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.store.file.CsvFileWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Mysql CSV类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlCsvTypeFileWriter extends ShellMysqlTypeFileWriter {

    /**
     * 字段列表
     */
    private MysqlColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * CSV 写入器
     */
    private final CsvFileWriter writer;

    /**
     * 构造 Mysql CSV类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws IOException IO 异常
     */
    public ShellMysqlCsvTypeFileWriter(String filePath, DBDataExportConfig config, MysqlColumns columns) throws IOException {
        this.columns = columns;
        this.config = config;
        this.writer = new CsvFileWriter(filePath, config);
    }

    @Override
    public void writeHeader() throws Exception {
        this.writer.write(this.columns.columnNames());
    }

    @Override
    public void writeRecord(Map<String, Object> object) throws Exception {
        Object[] values = new Object[this.columns.size()];
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            int index = this.columns.index(entry.getKey());
            MysqlColumn column = this.columns.column(entry.getKey());
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
