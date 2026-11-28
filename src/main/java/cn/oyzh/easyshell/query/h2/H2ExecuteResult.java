package cn.oyzh.easyshell.query.h2;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;
import cn.oyzh.easyshell.util.h2.ShellH2DataUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * H2 SQL 执行结果。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2ExecuteResult extends H2QueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        this.readonly = readonly;
        this.columns = new H2Columns();
        this.records = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int index = 1; index <= metaData.getColumnCount(); index++) {
            H2Column column = new H2Column();
            String typeName = metaData.getColumnTypeName(index);
            column.setName(metaData.getColumnLabel(index));
            column.setType(typeName);
            int precision = metaData.getPrecision(index);
            int scale = metaData.getScale(index);
            column.setSize(precision > 0 ? precision : ShellH2ColumnUtil.normalizeJdbcSize(typeName));
            column.setDigits(scale > 0 ? scale : null);
            column.setPosition(index);
            String schemaName = metaData.getSchemaName(index);
            column.setDbName(StringUtil.isBlank(schemaName) ? null : schemaName);
            column.setTableName(metaData.getTableName(index));
            column.setNotNull(metaData.isNullable(index) == ResultSetMetaData.columnNoNulls);
            column.setIdentity(metaData.isAutoIncrement(index));
            this.columns.add(column);
        }
        while (resultSet.next()) {
            H2Record record = new H2Record(this.columns, readonly);
            for (int index = 0; index < this.columns.size(); index++) {
                H2Column column = this.columns.get(index);
                record.putValue(column,
                        ShellH2DataUtil.valueStandardization(column, resultSet.getObject(index + 1)));
            }
            this.records.add(record);
        }
    }

    /**
     * 根据表字段补充查询结果字段信息
     *
     * @param tableColumns 表字段列表
     */
    public void enrichColumns(List<H2Column> tableColumns) {
        if (this.columns == null || this.columns.isEmpty() || tableColumns == null || tableColumns.isEmpty()) {
            return;
        }
        Map<String, H2Column> columnMap = new HashMap<>();
        for (H2Column column : tableColumns) {
            columnMap.put(column.getName().toLowerCase(Locale.ROOT), column);
        }
        String dbName = this.dbName();
        String tableName = this.tableName();
        for (H2Column column : this.columns) {
            H2Column tableColumn = columnMap.get(column.getName().toLowerCase(Locale.ROOT));
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
