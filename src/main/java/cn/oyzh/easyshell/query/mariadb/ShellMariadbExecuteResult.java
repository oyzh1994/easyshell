package cn.oyzh.easyshell.query.mariadb;

import cn.oyzh.easyshell.mariadb.ShellMariadbHelper;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
        List<Object[]> rows = new ArrayList<>();
        while (resultSet.next()) {
            Object[] row = new Object[this.columns.size()];
            for (int i = 0; i < this.columns.size(); i++) {
                row[i] = resultSet.getObject(i + 1);
            }
            rows.add(row);
        }
        ShellMariadbHelper.enrichColumnMetadata(
                connection, this.dbName(), this.tableName(), this.columns);
        for (Object[] row : rows) {
            MariadbRecord record = new MariadbRecord(this.columns, readonly);
            for (int i = 0; i < this.columns.size(); i++) {
                MariadbColumn dbColumn = this.columns.get(i);
                Object data = row[i];
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
