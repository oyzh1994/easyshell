package cn.oyzh.easyshell.data.postgresql.file;

import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.store.file.CsvFileWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Postgresql Txt类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlTxtTypeFileWriter extends ShellPostgresqlTypeFileWriter {

    /**
     * 字段列表
     */
    private PostgresqlColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private CsvFileWriter writer;

    /**
     * 构造 Postgresql Txt类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws IOException IO 异常
     */
    public ShellPostgresqlTxtTypeFileWriter(String filePath, DBDataExportConfig config, PostgresqlColumns columns) throws IOException {
        this.columns = columns;
        this.config = config;
        this.writer = new CsvFileWriter(filePath, config, config.getFieldSeparator());
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
            PostgresqlColumn column = this.columns.column(entry.getKey());
            Object val = this.parameterized(column, entry.getValue(), this.config);
            values[index] = val;
        }
        this.writer.write(values);
    }

    @Override
    public void close() throws IOException {
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
        }
        this.config = null;
        this.columns = null;
    }
}
