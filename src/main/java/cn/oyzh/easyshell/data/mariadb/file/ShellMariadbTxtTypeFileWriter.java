package cn.oyzh.easyshell.data.mariadb.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Mariadb Txt类型文件写入器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTxtTypeFileWriter extends ShellMariadbTypeFileWriter {

    /**
     * 字段列表
     */
    private MariadbColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private LineFileWriter writer;

    /**
     * 构造 Mariadb Txt类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellMariadbTxtTypeFileWriter(String filePath, DBDataExportConfig config, MariadbColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeHeader() throws Exception {
        this.writer.write(this.formatLine(this.columns.columnNames(), this.config.getFieldSeparator(), this.config.getTxtIdentifier(),
                this.config.getRecordSeparator()));
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        Object[] values = new Object[this.columns.size()];
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            int index = this.columns.index(entry.getKey());
            MariadbColumn column = this.columns.column(entry.getKey());
            Object val = this.parameterized(column, entry.getValue(), this.config);
            values[index] = val;
        }
        this.writer.write(this.formatLine(values,
                this.config.getFieldSeparator(),
                this.config.getTxtIdentifier(),
                this.config.getRecordSeparator()));
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
}
