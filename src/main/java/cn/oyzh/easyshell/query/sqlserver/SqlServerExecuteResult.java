package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerHelper;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * SqlServer执行结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class SqlServerExecuteResult extends SqlServerQueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        // 解析字段列表
        this.records = new ArrayList<>();
        this.columns = ShellSqlServerHelper.parseColumns(resultSet);
        this.fillSource();
        while (resultSet.next()) {
            SqlServerRecord record = new SqlServerRecord(columns, readonly);
            int colIndex = 1;
            for (SqlServerColumn dbColumn : this.columns) {
                // 获取字段值并处理SQL Server二进制及CLR类型
                Object data = this.getValue(resultSet, dbColumn, colIndex++);
                record.putValue(dbColumn, this.parseValue(data));
            }
            this.records.add(record);
        }
    }

    /**
     * 回填查询结果所属表
     * SQL Server JDBC 的 ResultSetMetaData 通常不返回 schema 和 tableName，
     * 对可安全编辑的单表查询从 SQL 中恢复来源表。
     */
    void fillSource() {
        if (this.columns == null || this.columns.isEmpty() || StringUtil.isBlank(this.content)) {
            return;
        }
        SqlServerQuerySource source = SqlServerQuerySource.parse(this.content);
        if (source == null) {
            return;
        }
        for (SqlServerColumn column : this.columns) {
            if (StringUtil.isBlank(column.getTableName())) {
                column.setTableName(source.table());
            }
            if (StringUtil.isNotBlank(source.schema()) && StringUtil.isBlank(column.getSchema())) {
                column.setSchema(source.schema());
            }
        }
    }

    /**
     * 设置是否全字段
     *
     * @param fullColumn 是否全字段
     */
    public void setFullColumn(boolean fullColumn) {
        this.fullColumn = fullColumn;
    }

    /**
     * 是否全字段
     *
     * @return 结果
     */
    public boolean isFullColumn() {
        return fullColumn;
    }
}
