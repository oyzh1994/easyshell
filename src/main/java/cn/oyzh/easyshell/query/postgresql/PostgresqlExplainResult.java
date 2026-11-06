package cn.oyzh.easyshell.query.postgresql;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlHelper;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * PostgreSQL解释结果
 * <p>
 * PostgreSQL通过{@code EXPLAIN (FORMAT TEXT) <sql>}输出执行计划，
 * 每行计划文本对应结果集的一条记录（字段名一般为QUERY PLAN），
 * 此处按行映射为字段与记录，保持解释结果界面的表格展示方式。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class PostgresqlExplainResult extends PostgresqlQueryResult {

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws SQLException {
        this.columns = ShellPostgresqlHelper.parseColumns(resultSet);
        this.records = new ArrayList<>();
        while (resultSet.next()) {
            int colIndex = 1;
            PostgresqlRecord record = new PostgresqlRecord(this.columns, readonly);
            for (PostgresqlColumn dbColumn : this.columns) {
                // 计划文本逐行读取；json格式时转换为字符串展示
                Object data = this.getValue(resultSet, dbColumn, colIndex++);
                record.putValue(dbColumn, this.parseValue(data));
            }
            this.records.add(record);
        }
    }

}
