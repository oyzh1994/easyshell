package cn.oyzh.easyshell.query.sqlserver;

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
