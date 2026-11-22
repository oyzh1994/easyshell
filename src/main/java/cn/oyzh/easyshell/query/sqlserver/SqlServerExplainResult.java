package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.easyshell.sqlserver.ShellSqlServerHelper;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * SqlServer解释结果
 * <p>
 * SQL Server通过{@code SET SHOWPLAN_TEXT ON}输出执行计划，
 * 此处按行映射为字段与记录，保持解释结果界面的表格展示方式。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class SqlServerExplainResult extends SqlServerQueryResult {

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws SQLException {
        this.columns = ShellSqlServerHelper.parseColumns(resultSet);
        this.records = new ArrayList<>();
        while (resultSet.next()) {
            int colIndex = 1;
            SqlServerRecord record = new SqlServerRecord(this.columns, readonly);
            for (SqlServerColumn dbColumn : this.columns) {
                // 计划文本逐行读取；json格式时转换为字符串展示
                Object data = this.getValue(resultSet, dbColumn, colIndex++);
                record.putValue(dbColumn, this.parseValue(data));
            }
            this.records.add(record);
        }
    }

}
