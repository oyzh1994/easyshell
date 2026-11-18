package cn.oyzh.easyshell.query.oracle;

import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.record.OracleRecord;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Oracle执行结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class OracleExecuteResult extends OracleQueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        // 获取列数
        this.records = new ArrayList<>();
        this.columns = ShellOracleHelper.parseColumns(resultSet);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        String schema = resultSetMetaData.getSchemaName(1);
        String tableName = resultSetMetaData.getTableName(1);
        this.initPrimaryKeys(connection, schema, tableName);
        while (resultSet.next()) {
            OracleRecord record = new OracleRecord(columns, readonly);
            int colIndex = 1;
            for (OracleColumn dbColumn : this.columns) {
                Object data = resultSet.getObject(colIndex++);
//                // 获取几何值
//                if (dbColumn.supportGeometry()) {
//                    data = OracleHelper.getGeometryString(connection, data);
//                }
                record.putValue(dbColumn, data);
            }
            this.records.add(record);
        }
    }

    private void initPrimaryKeys(Connection connection, String schema, String tableName) throws SQLException {
        if (connection == null || this.columns == null || schema == null || schema.isBlank()
                || tableName == null || tableName.isBlank()) {
            return;
        }
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet keys = metaData.getPrimaryKeys(null, schema, tableName)) {
            while (keys.next()) {
                OracleColumn column = this.columns.column(keys.getString("COLUMN_NAME"));
                if (column != null) {
                    column.setPrimaryKey(true);
                }
            }
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
