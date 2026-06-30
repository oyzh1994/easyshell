package cn.oyzh.easyshell.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
import com.mysql.cj.conf.PropertyKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/7/1
 */
public class ShellMysqlHelper {

    public static String getFunctionDefinition(Connection connection, String functionName) throws Exception {
        String sql = "SHOW CREATE FUNCTION " + ShellMysqlUtil.wrap(functionName, DBDialect.MYSQL);
        Statement statement = connection.createStatement();
        // 执行SQL查询并获取结果集
        ResultSet resultSet = statement.executeQuery(sql);
        String createDefinition = "";
        if (resultSet.next()) {
            createDefinition = resultSet.getString("Create Function");
        }
        ShellMysqlUtil.close(resultSet);
        ShellMysqlUtil.close(statement);
        return createDefinition;
    }

    public static List<MysqlRoutineParam> listRoutineParam(Connection connection, String dbName, String routineName, String routineType) throws Exception {
        String sql = """
                SELECT
                	`DATA_TYPE`,
                	`COLLATION_NAME`,
                	`DTD_IDENTIFIER`,
                	`PARAMETER_MODE`,
                	`PARAMETER_NAME`,
                	`CHARACTER_SET_NAME`
                FROM
                	INFORMATION_SCHEMA.PARAMETERS
                WHERE
                	ROUTINE_TYPE = ?
                AND
                    SPECIFIC_SCHEMA = ?
                AND
                    SPECIFIC_NAME = ?
                """;
        List<MysqlRoutineParam> params = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, routineType);
        statement.setString(2, dbName);
        statement.setString(3, routineName);
        // 执行SQL查询并获取结果集
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            MysqlRoutineParam param = new MysqlRoutineParam();
            // param.setType(resultSet.getString("DATA_TYPE"));
            param.setName(resultSet.getString("PARAMETER_NAME"));
            param.setMode(resultSet.getString("PARAMETER_MODE"));
            param.setCollation(resultSet.getString("COLLATION_NAME"));
            param.setCharset(resultSet.getString("CHARACTER_SET_NAME"));
            param.setDtdIdentifier(resultSet.getString("DTD_IDENTIFIER"));
            params.add(param);
        }
        ShellMysqlUtil.close(resultSet);
        ShellMysqlUtil.close(statement);
        return params;
    }

    public static List<MysqlRoutineParam> listFunctionParam(Connection connection, String dbName, String functionName) throws Exception {
        return listRoutineParam(connection, dbName, functionName, "FUNCTION");
    }

    public static List<MysqlRoutineParam> listProcedureParam(Connection connection, String dbName, String procedureName) throws Exception {
        return listRoutineParam(connection, dbName, procedureName, "PROCEDURE");
    }

    public static boolean isViewUpdatable(Connection connection, String dbName, String viewName) throws Exception {
        String sql = """
                SELECT
                    `IS_UPDATABLE`
                FROM
                    information_schema.`VIEWS`
                WHERE
                    `TABLE_SCHEMA` = ?
                AND
                    `TABLE_NAME` = ?
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, dbName);
        statement.setString(2, viewName);
        // 执行SQL查询并获取结果集
        ResultSet resultSet = statement.executeQuery();
        String isUpdatable = "";
        if (resultSet.next()) {
            isUpdatable = resultSet.getString("IS_UPDATABLE");
        }
        ShellMysqlUtil.close(resultSet);
        ShellMysqlUtil.close(statement);
        return StringUtil.equalsIgnoreCase(isUpdatable, "YES");
    }

    public static Map<String, String> getViewInfo(Connection connection, String dbName, String viewName) throws Exception {
        String sql = """
                SELECT
                    `IS_UPDATABLE` AS `UPDATABLE`,
                    `CHECK_OPTION` AS `CHECK_OPTION`,
                    `VIEW_DEFINITION` AS `DEFINITION`,
                    `SECURITY_TYPE` AS `SECURITY_TYPE`
                FROM
                    information_schema.`VIEWS`
                WHERE
                    `TABLE_SCHEMA` = ?
                AND
                    `TABLE_NAME` = ?
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, dbName);
        statement.setString(2, viewName);
        // 执行SQL查询并获取结果集
        ResultSet resultSet = statement.executeQuery();
        Map<String, String> info = new HashMap<>();
        while (resultSet.next()) {
            info.put("UPDATABLE", resultSet.getString("UPDATABLE"));
            info.put("DEFINITION", resultSet.getString("DEFINITION"));
            info.put("CHECK_OPTION", resultSet.getString("CHECK_OPTION"));
            info.put("SECURITY_TYPE", resultSet.getString("SECURITY_TYPE"));
        }
        String createView = showCreateView(connection, ShellMysqlUtil.wrap(dbName, viewName, DBDialect.MYSQL));
        String[] arr = createView.split(" ");
        for (String string : arr) {
            if (StringUtil.startWithIgnoreCase(string, "DEFINER=")) {
                info.put("DEFINER", string.substring(8));
            }
            if (StringUtil.startWithIgnoreCase(string, "ALGORITHM=")) {
                info.put("ALGORITHM", string.substring(10));
            }
        }
        info.put("CREATE_VIEW", createView);
        ShellMysqlUtil.close(resultSet);
        ShellMysqlUtil.close(statement);
        return info;
    }

    public static String getGeometryString(Connection connection, Object val) throws Exception {
        String value = null;
        if (val != null) {
            String sql = "SELECT ST_AsText(?);";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, val);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                value = resultSet.getString(1);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
        }
        return value;
    }

    public static Long lastInsertId(Connection connection) throws Exception {
        String sql = "SELECT LAST_INSERT_ID();";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        Long insertId = null;
        if (resultSet.next()) {
            insertId = resultSet.getLong(1);
        }
        ShellMysqlUtil.close(statement);
        return insertId;
    }

    public static MysqlColumns parseColumns(ResultSet resultSet) throws SQLException {
        return parseColumns(resultSet, Collections.emptyList());
    }

    public static MysqlColumns parseColumns(ResultSet resultSet, List<String> excludes) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        MysqlColumns columns = new MysqlColumns();
        // 遍历结果集并输出列名
        for (int i = 1; i <= columnCount; i++) {
            // 获取列名
            String columnName = resultSetMetaData.getColumnName(i);
            if (excludes.contains(columnName)) {
                continue;
            }
//            int columnType = resultSetMetaData.getColumnType(i);
            String columnTypeName = resultSetMetaData.getColumnTypeName(i);
//            String columnLabel = resultSetMetaData.getColumnLabel(i);
            int displaySize = resultSetMetaData.getColumnDisplaySize(i);
//            boolean signed = resultSetMetaData.isSigned(i);
//            boolean readOnly = resultSetMetaData.isReadOnly(i);
//            boolean writable = resultSetMetaData.isWritable(i);
//            boolean searchable = resultSetMetaData.isSearchable(i);
//            int precision = resultSetMetaData.getPrecision(i);
//            boolean caseSensitive = resultSetMetaData.isCaseSensitive(i);
//            boolean currency = resultSetMetaData.isCurrency(i);
            boolean autoIncrement = resultSetMetaData.isAutoIncrement(i);
//            boolean definitelyWritable = resultSetMetaData.isDefinitelyWritable(i);
            int nullable = resultSetMetaData.isNullable(i);
            int scale = resultSetMetaData.getScale(i);
            String tableName = resultSetMetaData.getTableName(i);
//            String schemaName = resultSetMetaData.getSchemaName(i);
            String catalogName = resultSetMetaData.getCatalogName(i);
//            String columnClassName = resultSetMetaData.getColumnClassName(i);

//            System.out.println("columnType=" + columnType);
//            System.out.println("columnTypeName=" + columnTypeName);
//            System.out.println("columnName=" + columnName);
//            System.out.println("columnLabel=" + columnLabel);
//            System.out.println("displaySize=" + displaySize);
//            System.out.println("signed=" + signed);
//            System.out.println("readOnly=" + readOnly);
//            System.out.println("writable=" + writable);
//            System.out.println("searchable=" + searchable);
//            System.out.println("precision=" + precision);
//            System.out.println("caseSensitive=" + caseSensitive);
//            System.out.println("currency=" + currency);
//            System.out.println("autoIncrement=" + autoIncrement);
//            System.out.println("definitelyWritable=" + definitelyWritable);
//            System.out.println("nullable=" + nullable);
//            System.out.println("scale=" + scale);
//            System.out.println("tableName=" + tableName);
//            System.out.println("schemaName=" + schemaName);
//            System.out.println("catalogName=" + catalogName);
//            System.out.println("columnClassName=" + columnClassName);
//            System.out.println("---------------");

            MysqlColumn dbColumn = new MysqlColumn();
            dbColumn.setDigits(scale);
            dbColumn.setName(columnName);
            dbColumn.setSize(displaySize);
            dbColumn.setDbName(catalogName);
            dbColumn.setTableName(tableName);
            dbColumn.setType(columnTypeName);
            dbColumn.setAutoIncrement(autoIncrement);
            dbColumn.setNullable(nullable == ResultSetMetaData.columnNullable);

            columns.add(dbColumn);
        }
        return columns;
    }

    public static String showCreateView(Connection connection, String viewName) throws Exception {
        String sql = "SHOW CREATE VIEW " + ShellMysqlUtil.wrap(viewName, DBDialect.MYSQL);
        Statement statement = connection.createStatement();
        // 执行SQL查询并获取结果集
        ResultSet resultSet = statement.executeQuery(sql);
        String createDefinition = "";
        if (resultSet.next()) {
            createDefinition = resultSet.getString("Create View");
        }
        ShellMysqlUtil.close(resultSet);
        ShellMysqlUtil.close(statement);
        return createDefinition;
    }

    public static Map<String, String> DEFAULT_ENVIRONMENT = new HashMap<>();

    static {
        DEFAULT_ENVIRONMENT.put(PropertyKey.tcpNoDelay.getKeyName(), "true");
        DEFAULT_ENVIRONMENT.put(PropertyKey.tcpKeepAlive.getKeyName(), "true");
        DEFAULT_ENVIRONMENT.put(PropertyKey.autoReconnect.getKeyName(), "true");
        DEFAULT_ENVIRONMENT.put(PropertyKey.characterEncoding.getKeyName(), "utf8");
        DEFAULT_ENVIRONMENT.put(PropertyKey.allowPublicKeyRetrieval.getKeyName(), "true");
        DEFAULT_ENVIRONMENT.put(PropertyKey.zeroDateTimeBehavior.getKeyName(), "convertToNull");
        if (Locale.getDefault().equals(Locale.CHINA)) {
            DEFAULT_ENVIRONMENT.put(PropertyKey.connectionTimeZone.getKeyName(), "Asia/Shanghai");
        } else {
            DEFAULT_ENVIRONMENT.put(PropertyKey.connectionTimeZone.getKeyName(), "UTC");
        }
    }

    public static String defaultEnvironment() {
        StringBuilder sb = new StringBuilder();
        DEFAULT_ENVIRONMENT.forEach((key, value) -> {
            sb.append(key).append("=").append(value).append("\n");
        });
        return sb.toString();
    }
}
