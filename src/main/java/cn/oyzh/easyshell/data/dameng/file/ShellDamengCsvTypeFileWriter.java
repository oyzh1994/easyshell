package cn.oyzh.easyshell.data.dameng.file;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.store.file.CsvFileWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Dameng CSV类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengCsvTypeFileWriter extends ShellDamengTypeFileWriter {

    /**
     * 字段列表
     */
    private DamengColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * CSV 写入器
     */
    private final CsvFileWriter writer;

    /**
     * 构造 Dameng CSV类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws IOException IO 异常
     */
    public ShellDamengCsvTypeFileWriter(String filePath, DBDataExportConfig config, DamengColumns columns) throws IOException {
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
            DamengColumn column = this.columns.column(entry.getKey());
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
