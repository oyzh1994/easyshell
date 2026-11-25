package cn.oyzh.easyshell.data.oracle.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.util.oracle.OracleInsertSqlBuilder;
import cn.oyzh.easyshell.util.oracle.OracleComplexValueUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Oracle Sql类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleSqlTypeFileWriter extends ShellOracleTypeFileWriter {

    /**
     * 字段列表
     */
    private OracleColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private final LineFileWriter writer;

    /**
     * 构造 Oracle Sql类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellOracleSqlTypeFileWriter(String filePath, DBDataExportConfig config, OracleColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    /**
     * 表名称
     */
    private String table;

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public void writeHeader() throws Exception {
        super.writeHeader();
    }

    @Override
    public void writeTrial() throws Exception {
        super.writeTrial();
    }

    @Override
    public void writeRecord(Map<String, Object> object) throws Exception {
        this.writer.writeLine(OracleInsertSqlBuilder.build(this.columns, object, this.config.isIncludeTitle()));
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        this.config = null;
        this.columns = null;
    }

    @Override
    public Object parameterized(OracleColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return "NULL";
        }
        value = ShellOracleDataUtil.valueStandardization(value);
        //        if (column.supportGeometry()) {
        //            return "ST_GeomFromText('" + value + "')";
        //        }
        if (column.isObjectType() || column.isCollectionType()) {
            return OracleComplexValueUtil.toSqlLiteral(column, value, false);
        }
        return ShellOracleDataUtil.parameterizedForSql(column, value);
    }
}
