package cn.oyzh.easyshell.mysql.query;

import cn.oyzh.easyshell.mysql.MysqlHelper;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.query.MysqlQueryResult;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2024/08/16
 */
public class MysqlExplainResult extends MysqlQueryResult {

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws SQLException {
        this.columns = MysqlHelper.parseColumns(resultSet);
        this.records = new ArrayList<>();
        while (resultSet.next()) {
            int colIndex = 1;
            MysqlRecord record = new MysqlRecord(this.columns, readonly);
            for (MysqlColumn dbColumn : this.columns) {
                Object data = resultSet.getObject(colIndex++);
                record.putValue(dbColumn, data);
            }
            this.records.add(record);
        }
    }

}
