package cn.oyzh.easyshell.mariadb;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MariaDB工具类
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbHelper {

    //public static String getFunctionDefinition(Connection connection, String functionName) throws Exception {
    //    String sql = "SHOW CREATE FUNCTION " + DBUtil.wrap(functionName, DBDialect.MARIADB);
    //    Statement statement = connection.createStatement();
    //    // 执行SQL查询并获取结果集
    //    ResultSet resultSet = statement.executeQuery(sql);
    //    String createDefinition = "";
    //    if (resultSet.next()) {
    //        createDefinition = resultSet.getString("Create Function");
    //    }
    //    DBUtil.close(resultSet);
    //    DBUtil.close(statement);
    //    return createDefinition;
    //}

    //public static List<MariadbRoutineParam> listRoutineParam(Connection connection, String dbName, String routineName, String routineType) throws Exception {
    //    String sql = """
    //            SELECT
    //            	`DATA_TYPE`,
    //            	`COLLATION_NAME`,
    //            	`DTD_IDENTIFIER`,
    //            	`PARAMETER_MODE`,
    //            	`PARAMETER_NAME`,
    //            	`CHARACTER_SET_NAME`
    //            FROM
    //            	INFORMATION_SCHEMA.PARAMETERS
    //            WHERE
    //            	ROUTINE_TYPE = ?
    //            AND
    //                SPECIFIC_SCHEMA = ?
    //            AND
    //                SPECIFIC_NAME = ?
    //            """;
    //    List<MariadbRoutineParam> params = new ArrayList<>();
    //    PreparedStatement statement = connection.prepareStatement(sql);
    //    statement.setString(1, routineType);
    //    statement.setString(2, dbName);
    //    statement.setString(3, routineName);
    //    // 执行SQL查询并获取结果集
    //    ResultSet resultSet = statement.executeQuery();
    //    while (resultSet.next()) {
    //        MariadbRoutineParam param = new MariadbRoutineParam();
    //        // param.setType(resultSet.getString("DATA_TYPE"));
    //        param.setName(resultSet.getString("PARAMETER_NAME"));
    //        param.setMode(resultSet.getString("PARAMETER_MODE"));
    //        param.setCollation(resultSet.getString("COLLATION_NAME"));
    //        param.setCharset(resultSet.getString("CHARACTER_SET_NAME"));
    //        param.setDtdIdentifier(resultSet.getString("DTD_IDENTIFIER"));
    //        params.add(param);
    //    }
    //    DBUtil.close(resultSet);
    //    DBUtil.close(statement);
    //    return params;
    //}
    //
    //public static List<MariadbRoutineParam> listFunctionParam(Connection connection, String dbName, String functionName) throws Exception {
    //    return listRoutineParam(connection, dbName, functionName, "FUNCTION");
    //}
    //
    //public static List<MariadbRoutineParam> listProcedureParam(Connection connection, String dbName, String procedureName) throws Exception {
    //    return listRoutineParam(connection, dbName, procedureName, "PROCEDURE");
    //}

    /**
     * 视图是否可更新
     *
     * @param connection 数据库连接
     * @param dbName     库名称
     * @param viewName   视图名称
     * @return 结果
     * @throws Exception 异常
     */
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
        IOUtil.close(resultSet);
        IOUtil.close(statement);
        return StringUtil.equalsIgnoreCase(isUpdatable, "YES");
    }

    //public static Map<String, String> getViewInfo(Connection connection, String dbName, String viewName) throws Exception {
    //    String sql = """
    //            SELECT
    //                `IS_UPDATABLE` AS `UPDATABLE`,
    //                `CHECK_OPTION` AS `CHECK_OPTION`,
    //                `VIEW_DEFINITION` AS `DEFINITION`,
    //                `SECURITY_TYPE` AS `SECURITY_TYPE`
    //            FROM
    //                information_schema.`VIEWS`
    //            WHERE
    //                `TABLE_SCHEMA` = ?
    //            AND
    //                `TABLE_NAME` = ?
    //            """;
    //    PreparedStatement statement = connection.prepareStatement(sql);
    //    statement.setString(1, dbName);
    //    statement.setString(2, viewName);
    //    // 执行SQL查询并获取结果集
    //    ResultSet resultSet = statement.executeQuery();
    //    Map<String, String> info = new HashMap<>();
    //    while (resultSet.next()) {
    //        info.put("UPDATABLE", resultSet.getString("UPDATABLE"));
    //        info.put("DEFINITION", resultSet.getString("DEFINITION"));
    //        info.put("CHECK_OPTION", resultSet.getString("CHECK_OPTION"));
    //        info.put("SECURITY_TYPE", resultSet.getString("SECURITY_TYPE"));
    //    }
    //    String createView = showCreateView(connection, DBUtil.wrap(dbName, viewName, DBDialect.MARIADB));
    //    String[] arr = createView.split(" ");
    //    for (String string : arr) {
    //        if (StringUtil.startWithIgnoreCase(string, "DEFINER=")) {
    //            info.put("DEFINER", string.substring(8));
    //        }
    //        if (StringUtil.startWithIgnoreCase(string, "ALGORITHM=")) {
    //            info.put("ALGORITHM", string.substring(10));
    //        }
    //    }
    //    info.put("CREATE_VIEW", createView);
    //    DBUtil.close(resultSet);
    //    DBUtil.close(statement);
    //    return info;
    //}

    /**
     * 获取几何类型字符串值
     *
     * @param connection 数据库连接
     * @param val        几何值
     * @return 几何字符串值
     * @throws Exception 异常
     */
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
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        }
        return value;
    }

    /**
     * 查询建表语句。
     *
     * @param connection 数据库连接
     * @param dbName     数据库名称
     * @param tableName  表名称
     * @return 建表语句
     * @throws Exception 异常
     */
    public static String showCreateTable(Connection connection, String dbName, String tableName) throws Exception {
        String sql = "SHOW CREATE TABLE " + DBUtil.wrap(dbName, tableName, DBDialect.MARIADB);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        String definition = "";
        if (resultSet.next()) {
            definition = resultSet.getString(2);
        }
        IOUtil.close(resultSet);
        IOUtil.close(statement);
        return definition;
    }

    /**
     * 从建表语句中提取生成列表达式。
     *
     * <p>MariaDB 的 INFORMATION_SCHEMA 会丢失生成表达式中的补充平面字符，
     * 因此优先从 SHOW CREATE TABLE 中提取完整表达式。</p>
     *
     * @param createSql  建表语句
     * @param columnName 字段名称
     * @return 表达式，未找到时返回 null
     */
    public static String extractGeneratedExpression(String createSql, String columnName) {
        String line = findColumnDefinitionLine(createSql, columnName);
        if (line == null) {
            return null;
        }
        String lower = line.toLowerCase(Locale.ROOT);
        int generated = lower.indexOf("generated always as (");
        if (generated < 0) {
            return null;
        }
        int expressionStart = generated + "generated always as (".length();
        int depth = 1;
        char quote = 0;
        for (int i = expressionStart; i < line.length(); i++) {
            char current = line.charAt(i);
            if (quote != 0) {
                if (current == '\\' && quote != '`' && i + 1 < line.length()) {
                    i++;
                } else if (current == quote) {
                    if (i + 1 < line.length() && line.charAt(i + 1) == quote) {
                        i++;
                    } else {
                        quote = 0;
                    }
                }
                continue;
            }
            if (current == '\'' || current == '"' || current == '`') {
                quote = current;
            } else if (current == '(') {
                depth++;
            } else if (current == ')' && --depth == 0) {
                return line.substring(expressionStart, i);
            }
        }
        return null;
    }

    /**
     * 判断字段是否为 MariaDB JSON 别名。
     *
     * <p>MariaDB 将 JSON 存储为 LONGTEXT，并在建表语句中生成
     * {@code CHECK (json_valid(column))} 约束。</p>
     *
     * @param createSql  建表语句
     * @param columnName 字段名称
     * @return 结果
     */
    public static boolean isJsonColumn(String createSql, String columnName) {
        String line = findColumnDefinitionLine(createSql, columnName);
        if (line == null) {
            return false;
        }
        String quotedName = DBUtil.wrap(columnName, DBDialect.MARIADB).toLowerCase(Locale.ROOT);
        return line.toLowerCase(Locale.ROOT).contains("json_valid(" + quotedName + ")");
    }

    /**
     * 查找字段定义所在行。
     *
     * @param createSql  建表语句
     * @param columnName 字段名称
     * @return 字段定义行
     */
    private static String findColumnDefinitionLine(String createSql, String columnName) {
        if (StringUtil.isBlank(createSql) || StringUtil.isBlank(columnName)) {
            return null;
        }
        String quotedName = DBUtil.wrap(columnName, DBDialect.MARIADB);
        int columnStart = createSql.indexOf("\n  " + quotedName + " ");
        if (columnStart < 0) {
            columnStart = createSql.indexOf("\n" + quotedName + " ");
        }
        if (columnStart < 0) {
            return null;
        }
        int lineEnd = createSql.indexOf('\n', columnStart + 1);
        if (lineEnd < 0) {
            lineEnd = createSql.length();
        }
        return createSql.substring(columnStart, lineEnd);
    }

//    public static Long lastInsertId(Connection connection) throws Exception {
//        String sql = "SELECT LAST_INSERT_ID();";
//        Statement statement = connection.createStatement();
//        ResultSet resultSet = statement.executeQuery(sql);
//        Long insertId = null;
//        if (resultSet.next()) {
//            insertId = resultSet.getLong(1);
//        }
//        IOUtil.close(statement);
//        IOUtil.close(resultSet);
//        return insertId;
//    }

    /**
     * 解析字段列表
     *
     * @param resultSet 结果集
     * @return 字段列表
     * @throws SQLException 异常
     */
    public static MariadbColumns parseColumns(ResultSet resultSet) throws SQLException {
        return parseColumns(resultSet, Collections.emptyList());
    }

    /**
     * 解析字段列表
     *
     * @param resultSet 结果集
     * @param excludes  排除的字段
     * @return 字段列表
     * @throws SQLException 异常
     */
    public static MariadbColumns parseColumns(ResultSet resultSet, List<String> excludes) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        MariadbColumns columns = new MariadbColumns();
        // 遍历结果集并输出列名
        for (int i = 1; i <= columnCount; i++) {
            // 获取列名
            //            String columnName = resultSetMetaData.getColumnLabel(i);
            String columnLabel = resultSetMetaData.getColumnLabel(i);
            if (excludes.contains(columnLabel)) {
                continue;
            }
            //            int columnType = resultSetMetaData.getColumnType(i);
            String columnTypeName = resultSetMetaData.getColumnTypeName(i);
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
            String schemaName = resultSetMetaData.getSchemaName(i);
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

            MariadbColumn dbColumn = new MariadbColumn();
            dbColumn.setDigits(scale);
            dbColumn.setName(columnLabel);
            dbColumn.setSize(displaySize);
            dbColumn.setSchema(schemaName);
            dbColumn.setDbName(catalogName);
            dbColumn.setTableName(tableName);
            dbColumn.setType(columnTypeName);
            dbColumn.setAutoIncrement(autoIncrement);
            dbColumn.setNullable(nullable == ResultSetMetaData.columnNullable);

            columns.add(dbColumn);
        }
        return columns;
    }

    /**
     * 查询视图创建语句
     *
     * @param connection 数据库连接
     * @param viewName   视图名称
     * @return 视图创建语句
     * @throws Exception 异常
     */
    public static String showCreateView(Connection connection, String viewName) throws Exception {
        String sql = "SHOW CREATE VIEW " + DBUtil.wrap(viewName, DBDialect.MARIADB);
        Statement statement = connection.createStatement();
        // 执行SQL查询并获取结果集
        ResultSet resultSet = statement.executeQuery(sql);
        String createDefinition = "";
        if (resultSet.next()) {
            createDefinition = resultSet.getString("Create View");
        }
        IOUtil.close(resultSet);
        IOUtil.close(statement);
        return createDefinition;
    }

    /**
     * 默认环境变量
     */
    public static Map<String, Object> DEFAULT_ENVIRONMENT = new HashMap<>();

    static {
        DEFAULT_ENVIRONMENT.put("tcpKeepAlive", "true");
        DEFAULT_ENVIRONMENT.put("allowPublicKeyRetrieval", "true");
        DEFAULT_ENVIRONMENT.put("forceConnectionTimeZoneToSession", "true");
        if (Locale.getDefault().equals(Locale.CHINA)) {
            DEFAULT_ENVIRONMENT.put("connectionTimeZone", "Asia/Shanghai");
        } else {
            DEFAULT_ENVIRONMENT.put("connectionTimeZone", "UTC");
        }
    }

    /**
     * 获取默认环境变量字符串
     *
     * @return 默认环境变量字符串
     */
    public static String defaultEnvironment() {
        StringBuilder sb = new StringBuilder();
        DEFAULT_ENVIRONMENT.forEach((key, value) -> {
            sb.append(key).append("=").append(value).append("\n");
        });
        return sb.toString();
    }
}
