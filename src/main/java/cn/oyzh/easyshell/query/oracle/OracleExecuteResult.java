package cn.oyzh.easyshell.query.oracle;

import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.util.oracle.OracleComplexValueUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.util.DBDataUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
        List<Object[]> rows = new ArrayList<>();
        while (resultSet.next()) {
            int colIndex = 1;
            Object[] row = new Object[this.columns.size()];
            for (OracleColumn dbColumn : this.columns) {
                Object data;
                if (dbColumn.isObjectType() || dbColumn.isCollectionType()) {
                    data = resultSet.getObject(colIndex++);
                } else {
                    data = dbColumn.supportJson()
                            ? resultSet.getString(colIndex++)
                            : resultSet.getObject(colIndex++);
                }
                row[colIndex - 2] = data;
            }
            rows.add(row);
        }
        // Oracle JDBC只允许同一连接同时存在一个活动结果流。
        // 结果集读完后再查询当前schema和主键，避免ORA-17027。
        if (tableName == null || tableName.isBlank()) {
            SourceName sourceName = this.resolveSourceName(this.content, null);
            if (sourceName != null) {
                tableName = sourceName.name();
                if (schema == null || schema.isBlank()) {
                    schema = sourceName.schema();
                    if (schema == null || schema.isBlank()) {
                        schema = this.currentSchema(connection);
                    }
                }
            }
        }
        this.fillColumnSource(schema, tableName);
        this.enrichColumnMetadata(connection, schema, tableName);
        this.initPrimaryKeys(connection, schema, tableName);
        for (Object[] row : rows) {
            OracleRecord record = new OracleRecord(this.columns, readonly);
            for (int i = 0; i < this.columns.size(); i++) {
                OracleColumn column = this.columns.get(i);
                record.putValue(column, this.normalizeValue(connection, column, row[i]));
            }
            this.records.add(record);
        }
    }

    private Object normalizeValue(Connection connection, OracleColumn column, Object data) throws SQLException {
        if (column.supportJson() && data instanceof CharSequence sequence) {
            data = DBDataUtil.normalizeJson(sequence.toString());
        }
        if (column.isObjectType() || column.isCollectionType()) {
            return OracleComplexValueUtil.toJsonValue(connection, data);
        }
        return ShellOracleDataUtil.valueStandardization(connection, data);
    }

    /**
     * 回填结果字段所属的表和模式。
     * Oracle JDBC对查询结果通常不返回表名，而查询编辑依赖列上的表信息生成UPDATE/DELETE。
     *
     * @param schema    模式
     * @param tableName 表名
     */
    void fillColumnSource(String schema, String tableName) {
        if (this.columns == null || tableName == null || tableName.isBlank()) {
            return;
        }
        for (OracleColumn column : this.columns) {
            if (column.getTableName() == null || column.getTableName().isBlank()) {
                column.setTableName(tableName);
            }
            if ((column.getSchema() == null || column.getSchema().isBlank())
                    && schema != null && !schema.isBlank()) {
                column.setSchema(schema);
            }
        }
    }

    /**
     * 使用来源表元数据补齐ResultSetMetaData缺少的虚拟列和默认值信息。
     *
     * @param connection 连接
     * @param schema     模式
     * @param tableName  表名
     * @throws SQLException 异常
     */
    void enrichColumnMetadata(Connection connection, String schema, String tableName) throws SQLException {
        if (connection == null || this.columns == null
                || schema == null || schema.isBlank()
                || tableName == null || tableName.isBlank()) {
            return;
        }
        String sql = """
                SELECT
                    C.COLUMN_NAME,
                    C.DATA_TYPE,
                    C.DATA_TYPE_OWNER,
                    T.TYPECODE,
                    C.VIRTUAL_COLUMN,
                    C.DATA_DEFAULT
                FROM ALL_TAB_COLS C
                LEFT JOIN ALL_TYPES T
                  ON T.OWNER = C.DATA_TYPE_OWNER
                 AND T.TYPE_NAME = C.DATA_TYPE
                WHERE C.OWNER = ?
                  AND C.TABLE_NAME = ?
                  AND C.HIDDEN_COLUMN = 'NO'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OracleColumn column = this.columns.column(resultSet.getString("COLUMN_NAME"));
                    if (column == null) {
                        continue;
                    }
                    String dataType = resultSet.getString("DATA_TYPE");
                    if (dataType != null && !dataType.isBlank()) {
                        // ResultSet元数据可能返回SYS.XMLTYPE等限定名称，使用表列元数据规范化。
                        column.setType(dataType);
                    }
                    String typeOwner = resultSet.getString("DATA_TYPE_OWNER");
                    String typeCode = resultSet.getString("TYPECODE");
                    if (typeOwner != null && !typeOwner.isBlank()) {
                        column.setTypeOwner(typeOwner);
                    }
                    if (typeCode != null && !typeCode.isBlank()) {
                        column.setTypeCode(typeCode);
                    }
                    if (column.isObjectType() || column.isCollectionType()) {
                        if (column.getTypeOwner() == null || column.getTypeOwner().isBlank()) {
                            column.setTypeOwner(schema);
                        }
                        column.setTypeMeta(OracleComplexValueUtil.loadTypeMeta(
                                connection, column.getTypeOwner(), column.getType()));
                    }
                    String defaultValue = resultSet.getString("DATA_DEFAULT");
                    boolean virtual = "YES".equalsIgnoreCase(resultSet.getString("VIRTUAL_COLUMN"));
                    column.setVirtual(virtual);
                    if (column.isGenerated()) {
                        column.setGeneratedExpression(defaultValue);
                        column.setDefaultValue(null);
                    } else {
                        column.setDefaultValue(defaultValue);
                    }
                }
            }
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

    SourceName resolveSourceName(String sql, String defaultSchema) {
        SourceName sourceName = this.findSourceName(sql);
        if (sourceName == null) {
            return null;
        }
        String schema = sourceName.schema() == null || sourceName.schema().isBlank()
                ? defaultSchema
                : sourceName.schema();
        return new SourceName(schema, sourceName.name());
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

    record SourceName(String schema, String name) {
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
