package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SQLite SQL 执行结果。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteExecuteResult extends SQLiteQueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        this.readonly = readonly;
        this.columns = new SQLiteColumns();
        this.records = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int index = 1; index <= metaData.getColumnCount(); index++) {
            SQLiteColumn column = new SQLiteColumn();
            String typeName = metaData.getColumnTypeName(index);
            column.setName(metaData.getColumnLabel(index));
            column.setSize(ShellSqliteColumnUtil.normalizeJdbcSize(typeName));
            column.setType(typeName);
            //column.setType(ShellSqliteColumnUtil.normalizeJdbcType(metaData.getColumnType(index)));
            column.setPosition(index);
            // SQLite JDBC 的 catalog 元数据返回表名，数据库统一使用 main。
            column.setDbName("main");
            column.setTableName(metaData.getTableName(index));
            column.setNotNull(metaData.isNullable(index) == ResultSetMetaData.columnNoNulls);
            column.setAutoIncrement(metaData.isAutoIncrement(index));
            this.columns.add(column);
        }
        while (resultSet.next()) {
            SQLiteRecord record = new SQLiteRecord(this.columns, readonly);
            for (int index = 0; index < this.columns.size(); index++) {
                record.putValue(this.columns.get(index), resultSet.getObject(index + 1));
            }
            this.records.add(record);
        }
    }

    /**
     * 根据表字段补充查询结果字段信息
     *
     * @param tableColumns 表字段列表
     */
    public void enrichColumns(List<SQLiteColumn> tableColumns) {
        if (this.columns == null || this.columns.isEmpty() || tableColumns == null || tableColumns.isEmpty()) {
            return;
        }
        Map<String, SQLiteColumn> columnMap = new HashMap<>();
        for (SQLiteColumn column : tableColumns) {
            columnMap.put(column.getName().toLowerCase(Locale.ROOT), column);
        }
        String dbName = this.dbName();
        String tableName = this.tableName();
        for (SQLiteColumn column : this.columns) {
            SQLiteColumn tableColumn = columnMap.get(column.getName().toLowerCase(Locale.ROOT));
            if (tableColumn == null) {
                continue;
            }
            String label = column.getName();
            String type = column.getType();
            Integer size = column.getSize();
            column.copy(tableColumn);
            column.setName(label);
            column.setType(type);
            column.setSize(size == null ? tableColumn.getSize() : size);
            if (StringUtil.isBlank(column.getDbName())) {
                column.setDbName(dbName);
            }
            if (StringUtil.isBlank(column.getTableName())) {
                column.setTableName(tableName);
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
        return this.fullColumn;
    }
}
