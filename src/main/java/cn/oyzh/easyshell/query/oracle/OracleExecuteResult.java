package cn.oyzh.easyshell.query.oracle;

import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.record.OracleRecord;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Oracle执行结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class OracleExecuteResult extends OracleQueryResult {

    private static final Pattern FROM_SOURCE = Pattern.compile(
            "\\bFROM\\s+((?:\"[^\"]+\"|[A-Za-z][A-Za-z0-9_$#]*)(?:\\s*\\.\\s*(?:\"[^\"]+\"|[A-Za-z][A-Za-z0-9_$#]*))?)",
            Pattern.CASE_INSENSITIVE);

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
        if (tableName == null || tableName.isBlank()) {
            SourceName sourceName = this.findSourceName(this.content);
            if (sourceName != null) {
                tableName = sourceName.name();
                if (schema == null || schema.isBlank()) {
                    schema = sourceName.schema() != null ? sourceName.schema() : this.currentSchema(connection);
                }
            }
        }
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
        boolean found = false;
        try (ResultSet keys = metaData.getPrimaryKeys(null, schema, tableName)) {
            while (keys.next()) {
                found = true;
                OracleColumn column = this.columns.column(keys.getString("COLUMN_NAME"));
                if (column != null) {
                    column.setPrimaryKey(true);
                }
            }
        }
        if (found) {
            return;
        }
        String dependencySql = """
                SELECT DISTINCT REFERENCED_OWNER, REFERENCED_NAME
                FROM ALL_DEPENDENCIES
                WHERE OWNER = ?
                  AND NAME = ?
                  AND REFERENCED_TYPE = 'TABLE'
                """;
        try (PreparedStatement statement = connection.prepareStatement(dependencySql)) {
            statement.setString(1, schema);
            statement.setString(2, tableName);
            try (ResultSet dependencies = statement.executeQuery()) {
                while (dependencies.next()) {
                    String owner = dependencies.getString(1);
                    String referencedTable = dependencies.getString(2);
                    if (owner == null || owner.isBlank() || referencedTable == null || referencedTable.isBlank()) {
                        continue;
                    }
                    try (ResultSet keys = metaData.getPrimaryKeys(null, owner, referencedTable)) {
                        while (keys.next()) {
                            OracleColumn column = this.columns.column(keys.getString("COLUMN_NAME"));
                            if (column != null) {
                                column.setPrimaryKey(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private SourceName findSourceName(String sql) {
        if (sql == null || sql.isBlank()) {
            return null;
        }
        Matcher matcher = FROM_SOURCE.matcher(sql);
        if (!matcher.find()) {
            return null;
        }
        String source = matcher.group(1).trim();
        int dot = source.lastIndexOf('.');
        if (dot < 0) {
            return new SourceName(null, unquote(source));
        }
        return new SourceName(unquote(source.substring(0, dot)), unquote(source.substring(dot + 1)));
    }

    private String currentSchema(Connection connection) {
        if (connection == null) {
            return null;
        }
        try {
            String schema = connection.getSchema();
            if (schema != null && !schema.isBlank()) {
                return schema;
            }
        } catch (SQLException ignored) {
        }
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL")) {
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException ignored) {
            return null;
        }
    }

    private String unquote(String value) {
        String result = value.trim();
        if (result.length() >= 2 && result.startsWith("\"") && result.endsWith("\"")) {
            result = result.substring(1, result.length() - 1);
        }
        return result.toUpperCase(Locale.ROOT);
    }

    private record SourceName(String schema, String name) {
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
