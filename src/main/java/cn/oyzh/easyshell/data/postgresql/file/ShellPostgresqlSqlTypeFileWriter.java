package cn.oyzh.easyshell.data.postgresql.file;

import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Postgresql Sql类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlSqlTypeFileWriter extends ShellPostgresqlTypeFileWriter {

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
    private final LineFileWriter writer;

    /**
     * 构造 Postgresql Sql类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellPostgresqlSqlTypeFileWriter(String filePath, DBDataExportConfig config, PostgresqlColumns columns) throws FileNotFoundException {
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
        // PostgreSQL序列值重置，保证后续插入不会与已导入的自增值冲突
        if (this.columns != null && !this.columns.isEmpty()) {
            List<String> resets = ShellPostgresqlDataImportHelper.buildSequenceResetSql(this.columns, this.columns.schema());
            for (String reset : resets) {
                this.writer.writeLine(reset);
            }
        }
        super.writeTrial();
    }

    @Override
    public void writeRecord(Map<String, Object> object) throws Exception {
        String tableName = this.columns.tableName();
        List<PostgresqlColumn> columnList = this.columns.sortOfPosition();
        // PostgreSQL使用"schema"."table"定位表
        String table = StringUtil.isBlank(this.columns.schema())
                ? DBUtil.wrap(tableName, DBDialect.POSTGRESQL)
                : DBUtil.wrap(this.columns.schema(), tableName, DBDialect.POSTGRESQL);
        final String sqlBase = "INSERT INTO " + table;
        StringBuilder sql = new StringBuilder(sqlBase);
        if (this.config.isIncludeTitle()) {
            sql.append(" (");
            for (PostgresqlColumn dbColumn : columnList) {
                sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.POSTGRESQL)).append(", ");
            }
            if (sql.toString().endsWith(", ")) {
                sql.delete(sql.length() - 2, sql.length());
            }
            sql.append(")");
        }
        sql.append(" VALUES (");
        for (PostgresqlColumn dbColumn : columnList) {
            Object val = object.get(dbColumn.getName());
            sql.append(this.parameterized(dbColumn, val, this.config)).append(", ");
        }
        if (sql.toString().endsWith(", ")) {
            sql.delete(sql.length() - 2, sql.length());
        }
        sql.append(");");
        this.writer.writeLine(sql.toString());
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        this.config = null;
        this.columns = null;
    }

    @Override
    public Object parameterized(PostgresqlColumn column, Object value, DBDataExportConfig config) {
        return this.toSqlLiteral(column, value, config);
    }
}
