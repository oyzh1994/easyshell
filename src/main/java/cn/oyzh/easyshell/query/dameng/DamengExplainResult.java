package cn.oyzh.easyshell.query.dameng;

import cn.oyzh.easyshell.dameng.ShellDamengHelper;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.record.DamengRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2024/08/16
 */
public class DamengExplainResult extends DamengQueryResult {

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws SQLException {
        this.columns = ShellDamengHelper.parseColumns(resultSet);
        this.records = new ArrayList<>();
        while (resultSet.next()) {
            int colIndex = 1;
            DamengRecord record = new DamengRecord(this.columns, readonly);
            for (DamengColumn dbColumn : this.columns) {
                Object data = resultSet.getObject(colIndex++);
                record.putValue(dbColumn, data);
            }
            this.records.add(record);
        }
    }

}
