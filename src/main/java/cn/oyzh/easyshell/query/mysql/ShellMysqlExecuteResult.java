package cn.oyzh.easyshell.query.mysql;

import cn.oyzh.easyshell.mysql.ShellMysqlHelper;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * mysql执行结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlExecuteResult extends ShellMysqlQueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        // 获取列数
        this.records = new ArrayList<>();
        this.columns = ShellMysqlHelper.parseColumns(resultSet);
        List<Object[]> rows = new ArrayList<>();
        while (resultSet.next()) {
            Object[] row = new Object[this.columns.size()];
            for (int i = 0; i < this.columns.size(); i++) {
                row[i] = resultSet.getObject(i + 1);
            }
            rows.add(row);
        }
        ShellMysqlHelper.enrichColumnMetadata(
                connection, this.dbName(), this.tableName(), this.columns);
        for (Object[] row : rows) {
            MysqlRecord record = new MysqlRecord(this.columns, readonly);
            for (int i = 0; i < this.columns.size(); i++) {
                MysqlColumn dbColumn = this.columns.get(i);
                Object data = row[i];
                // 获取几何值
                if (dbColumn.supportGeometry()) {
                    data = ShellMysqlHelper.getGeometryString(connection, data);
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
