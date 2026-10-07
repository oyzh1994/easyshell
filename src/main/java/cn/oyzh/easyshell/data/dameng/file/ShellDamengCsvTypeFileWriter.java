package cn.oyzh.easyshell.data.dameng.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;

import java.io.FileNotFoundException;
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
     * 文件读取器
     */
    private final LineFileWriter writer;

    /**
     * 构造 Dameng CSV类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellDamengCsvTypeFileWriter(String filePath, DBDataExportConfig config, DamengColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeHeader() throws Exception {
        this.writer.write(this.formatLine(this.columns.columnNames(), ",",
                this.config.getTxtIdentifier(),
                this.config.getRecordSeparator()));
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        Object[] values = new Object[this.columns.size()];
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            int index = this.columns.index(entry.getKey());
            DamengColumn column = this.columns.column(entry.getKey());
            Object val = this.parameterized(column, entry.getValue(), this.config);
            values[index] = val;
        }
        this.writer.write(this.formatLine(values, ",",
                this.config.getTxtIdentifier(),
                this.config.getRecordSeparator()));
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        this.config = null;
        this.columns = null;
    }
}
