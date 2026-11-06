package cn.oyzh.easyshell.query.postgresql;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlHelper;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * PostgreSQL执行结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class PostgresqlExecuteResult extends PostgresqlQueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        // 解析字段列表
        this.records = new ArrayList<>();
        this.columns = ShellPostgresqlHelper.parseColumns(resultSet);
        while (resultSet.next()) {
            PostgresqlRecord record = new PostgresqlRecord(columns, readonly);
            int colIndex = 1;
            for (PostgresqlColumn dbColumn : this.columns) {
                // 获取字段值并处理PostgreSQL特有类型（json/jsonb、bytea、uuid、数组等）
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
