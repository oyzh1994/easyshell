package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite SQL 执行结果。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteExecuteResult extends DBQueryResult {

    private List<SQLiteColumn> columns;

    private List<DBRecordData> records;

    @Override
    public int getCount() {
        return this.records == null ? 0 : this.records.size();
    }

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        this.columns = new ArrayList<>();
        this.records = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int index = 1; index <= metaData.getColumnCount(); index++) {
            SQLiteColumn column = new SQLiteColumn();
            column.setName(metaData.getColumnLabel(index));
            column.setType(metaData.getColumnTypeName(index));
            column.setSize(metaData.getColumnDisplaySize(index));
            column.setAutoIncrement(metaData.isAutoIncrement(index));
            this.columns.add(column);
        }
        while (resultSet.next()) {
            DBRecordData record = new DBRecordData();
            for (int index = 0; index < this.columns.size(); index++) {
                record.put(this.columns.get(index), resultSet.getObject(index + 1));
            }
            this.records.add(record);
        }
    }

    public List<SQLiteColumn> getColumns() {
        return this.columns == null ? List.of() : this.columns;
    }

    public void setColumns(List<SQLiteColumn> columns) {
        this.columns = columns;
    }

    public List<DBRecordData> getRecords() {
        return this.records == null ? List.of() : this.records;
    }

    public void setRecords(List<DBRecordData> records) {
        this.records = records;
    }
}
