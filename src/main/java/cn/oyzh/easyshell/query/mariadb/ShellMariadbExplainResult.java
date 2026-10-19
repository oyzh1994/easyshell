package cn.oyzh.easyshell.query.mariadb;

import cn.oyzh.easyshell.mariadb.ShellMariadbHelper;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * MariaDB解释结果
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbExplainResult extends ShellMariadbQueryResult {

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws SQLException {
        this.columns = ShellMariadbHelper.parseColumns(resultSet);
        this.records = new ArrayList<>();
        while (resultSet.next()) {
            int colIndex = 1;
            MariadbRecord record = new MariadbRecord(this.columns, readonly);
            for (MariadbColumn dbColumn : this.columns) {
                Object data = resultSet.getObject(colIndex++);
                record.putValue(dbColumn, data);
            }
            this.records.add(record);
        }
    }

}
