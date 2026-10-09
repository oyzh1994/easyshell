package cn.oyzh.easyshell.query.mariadb;

import cn.oyzh.easyshell.mariadb.ShellMariadbHelper;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * MariaDB执行结果
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbExecuteResult extends ShellMariadbQueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        // 获取列数
        this.records = new ArrayList<>();
        this.columns = ShellMariadbHelper.parseColumns(resultSet);
        while (resultSet.next()) {
            MariadbRecord record = new MariadbRecord(this.columns, readonly);
            int colIndex = 1;
            for (MariadbColumn dbColumn : this.columns) {
                Object data = resultSet.getObject(colIndex++);
                // 获取几何值
                if (dbColumn.supportGeometry()) {
                    data = ShellMariadbHelper.getGeometryString(connection, data);
                }
                record.putValue(dbColumn, data);
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
