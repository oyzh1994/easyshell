package cn.oyzh.easyshell.postgresql;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.fx.db.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * PostgreSQL工具类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlHelper {

    //    public static String getFunctionDefinition(Connection connection, String functionName) throws Exception {
    //        String sql = "SELECT DBMS_METADATA.GET_DDL('FUNCTION', " + DBUtil.wrapData(functionName) + ") FROM DUAL";
    //        Statement statement = connection.createStatement();
    //        // 执行SQL查询并获取结果集
    //        ResultSet resultSet = statement.executeQuery(sql);
    //        String createDefinition = "";
    //        if (resultSet.next()) {
    //            createDefinition = resultSet.getString(1);
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(statement);
    //        return createDefinition;
    //    }
    //
    //    public static String showCreateProcedure(Connection connection, String procedureName) throws Exception {
    //        String sql = "SELECT DBMS_METADATA.GET_DDL('PROCEDURE', " + DBUtil.wrapData(procedureName) + ") FROM DUAL";
    //        Statement statement = connection.createStatement();
    //        // 执行SQL查询并获取结果集
    //        ResultSet resultSet = statement.executeQuery(sql);
    //        String createDefinition = "";
    //        if (resultSet.next()) {
    //            createDefinition = resultSet.getString(1);
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(statement);
    //        return createDefinition;
    //    }
    //
    //    public static String showCreateTrigger(Connection connection, String triggerName) throws Exception {
    //        String sql = "SELECT DBMS_METADATA.GET_DDL('TRIGGER', " + DBUtil.wrapData(triggerName) + ") FROM DUAL";
    //        Statement statement = connection.createStatement();
    //        // 执行SQL查询并获取结果集
    //        ResultSet resultSet = statement.executeQuery(sql);
    //        String createDefinition = "";
    //        if (resultSet.next()) {
    //            createDefinition = resultSet.getString(1);
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(statement);
    //        return createDefinition;
    //    }
    //
    //    public static String showCreateFunction(Connection connection, String functionName) throws Exception {
    //        String sql = "SELECT DBMS_METADATA.GET_DDL('FUNCTION', " + DBUtil.wrapData(functionName) + ") FROM DUAL";
    //        Statement statement = connection.createStatement();
    //        // 执行SQL查询并获取结果集
    //        ResultSet resultSet = statement.executeQuery(sql);
    //        String createDefinition = "";
    //        if (resultSet.next()) {
    //            createDefinition = resultSet.getString(1);
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(statement);
    //        return createDefinition;
    //    }

    //public static List<PostgresqlRoutineParam> listRoutineParam(Connection connection, String dbName, String routineName, String routineType) throws Exception {
    //    String sql = """
    //            SELECT
    //            	a.POSITION,
    //            	a.DATA_TYPE,
    //            	a.DATA_LENGTH AS SIZE,
    //            	a.DATA_SCALE AS DIGITS,
    //            	a.IN_OUT AS PARAMETER_MODE,
    //            	a.ARGUMENT_NAME AS PARAMETER_NAME,
    //            	a.CHARACTER_SET_NAME
    //            FROM
    //            	ALL_ARGUMENTS a
    //            WHERE
    //            	a.OWNER = ?
    //            AND
    //            	a.OBJECT_NAME = ?
    //            AND
    //            	a.PACKAGE_NAME IS NULL
    //            """;
    //    List<PostgresqlRoutineParam> params = new ArrayList<>();
    //    PreparedStatement statement = connection.prepareStatement(sql);
    //    statement.setString(1, dbName);
    //    statement.setString(2, routineName);
    //    // 执行SQL查询并获取结果集
    //    ResultSet resultSet = statement.executeQuery();
    //    while (resultSet.next()) {
    //        PostgresqlRoutineParam param = new PostgresqlRoutineParam();
    //        param.setSize(resultSet.getInt("SIZE"));
    //        param.setDigits(resultSet.getInt("DIGITS"));
    //        param.setPosition(resultSet.getInt("POSITION"));
    //        param.setType(resultSet.getString("DATA_TYPE"));
    //        param.setName(resultSet.getString("PARAMETER_NAME"));
    //        param.setMode(resultSet.getString("PARAMETER_MODE"));
    //        param.setCharset(resultSet.getString("CHARACTER_SET_NAME"));
    //        params.add(param);
    //    }
    //    DBUtil.close(resultSet);
    //    DBUtil.close(statement);
    //    return params;
    //}
    //
    //public static List<PostgresqlRoutineParam> listFunctionParam(Connection connection, String dbName, String functionName) throws Exception {
    //    return listRoutineParam(connection, dbName, functionName, "FUNCTION");
    //}
    //
    //public static List<PostgresqlRoutineParam> listProcedureParam(Connection connection, String dbName, String procedureName) throws Exception {
    //    return listRoutineParam(connection, dbName, procedureName, "PROCEDURE");
    //}

    //    public static String getProcedureDefiner(Connection connection, String procedureName) throws Exception {
    //        // PostgreSQL通过ALL_PROCEDURES查询定义者，而非SHOW CREATE PROCEDURE
    //        String sql = "SELECT OWNER FROM ALL_PROCEDURES WHERE OBJECT_NAME = ? AND OWNER = (SELECT USERNAME FROM USER_USERS)";
    //        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    //            stmt.setString(1, procedureName);
    //            ResultSet rs = stmt.executeQuery();
    //            if (rs.next()) {
    //                return rs.getString(1);
    //            }
    //        }
    //        return null;
    //    }

    /**
     * 视图是否可更新
     *
     * @param connection 数据库连接
     * @param schema     模式名称
     * @param viewName   视图名称
     * @return 结果
     * @throws Exception 异常
     */
    public static boolean isViewUpdatable(Connection connection, String schema, String viewName) throws Exception {
        try {
            String sql = """
                        SELECT
                            IS_UPDATABLE
                        FROM
                            INFORMATION_SCHEMA.VIEWS
                        WHERE
                            TABLE_SCHEMA = ?
                        AND
                            TABLE_NAME = ?
                    """;
            DBUtil.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, viewName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            String isUpdatable = null;
            if (resultSet.next()) {
                isUpdatable = resultSet.getString("IS_UPDATABLE");
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return StringUtil.equalsIgnoreCase(isUpdatable, "YES");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 获取视图只读属性
     *
     * @param connection 数据库连接
     * @param schema     模式名称
     * @param viewName   视图名称
     * @return 结果
     * @throws Exception 异常
     */
    public static String getViewReadOnly(Connection connection, String schema, String viewName) throws Exception {
        try {
            String sql = """
                        SELECT
                            CASE WHEN IS_UPDATABLE = 'YES' THEN 'N' ELSE 'Y' END AS READ_ONLY
                        FROM
                            INFORMATION_SCHEMA.VIEWS
                        WHERE
                            TABLE_SCHEMA = ?
                        AND
                            TABLE_NAME = ?
                    """;
            DBUtil.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, viewName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            String readOnly = null;
            if (resultSet.next()) {
                readOnly = resultSet.getString("READ_ONLY");
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return readOnly;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //    public static Map<String, String> getViewInfo(Connection connection, String dbName, String viewName) throws Exception {
    //        String sql = """
    //                SELECT
    //                    NULL AS UPDATABLE,
    //                    NULL AS CHECK_OPTION,
    //                    TEXT AS DEFINITION,
    //                    NULL AS SECURITY_TYPE
    //                FROM
    //                    USER_VIEWS
    //                WHERE
    //                    VIEW_NAME = ?
    //                """;
    //        PreparedStatement statement = connection.prepareStatement(sql);
    //        statement.setString(1, viewName);
    //        // 执行SQL查询并获取结果集
    //        ResultSet resultSet = statement.executeQuery();
    //        Map<String, String> info = new HashMap<>();
    //        while (resultSet.next()) {
    //            info.put("UPDATABLE", resultSet.getString("UPDATABLE"));
    //            info.put("DEFINITION", resultSet.getString("DEFINITION"));
    //            info.put("CHECK_OPTION", resultSet.getString("CHECK_OPTION"));
    //            info.put("SECURITY_TYPE", resultSet.getString("SECURITY_TYPE"));
    //        }
    //        String createView = showCreateView(connection, viewName);
    //        String[] arr = createView.split(" ");
    //        for (String string : arr) {
    //            if (StringUtil.startWithIgnoreCase(string, "DEFINER=")) {
    //                info.put("DEFINER", string.substring(8));
    //            }
    //            if (StringUtil.startWithIgnoreCase(string, "ALGORITHM=")) {
    //                info.put("ALGORITHM", string.substring(10));
    //            }
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(statement);
    //        return info;
    //    }
    //
    //    public static String getGeometryString(Connection connection, Object val) throws Exception {
    //        String value = null;
    //        if (val != null) {
    //            String sql = "SELECT ST_AsText(?);";
    //            PreparedStatement statement = connection.prepareStatement(sql);
    //            statement.setObject(1, val);
    //            // 执行SQL查询并获取结果集
    //            ResultSet resultSet = statement.executeQuery();
    //            if (resultSet.next()) {
    //                value = resultSet.getString(1);
    //            }
    //            DBUtil.close(resultSet);
    //            DBUtil.close(statement);
    //        }
    //        return value;
    //    }
    //
    //    public static String showCreateTable(Connection connection, String tableName) throws Exception {
    //        String sql = "SELECT DBMS_METADATA.GET_DDL('TABLE', " + DBUtil.wrapData(tableName) + ") FROM DUAL";
    //        Statement stmt = connection.createStatement();
    //        ResultSet resultSet = stmt.executeQuery(sql);
    //        String definition = "";
    //        if (resultSet.next()) {
    //            definition = resultSet.getString(1);
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(stmt);
    //        return definition;
    //    }
    //
    //    public static boolean hasPrimaryKey(Connection connection, String dbName, String tableName) throws Exception {
    //        String sql = "SELECT COUNT(*) FROM ALL_CONSTRAINTS WHERE OWNER = ? AND TABLE_NAME = ? AND CONSTRAINT_TYPE = 'P'";
    //        PreparedStatement stmt = connection.prepareStatement(sql);
    //        stmt.setString(1, dbName);
    //        stmt.setString(2, tableName);
    //        ResultSet resultSet = stmt.executeQuery();
    //        Long count = null;
    //        if (resultSet.next()) {
    //            count = resultSet.getLong(1);
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(stmt);
    //        return count != null && count > 0;
    //    }
    //
    //    public static boolean isZeroFill(String showTableDefinition, String columnName) throws SQLException {
    //        // Postgresql does not support ZEROFILL; always return false
    //        return false;
    //    }
    //
    //    public static Integer getKeySize(String showTableDefinition, String columnName) throws SQLException {
    //        if (StringUtil.isNotBlank(showTableDefinition)) {
    //            String[] arr = showTableDefinition.split("PRIMARY KEY ");
    //            if (arr.length < 2) {
    //                return null;
    //            }
    //            // Find the parenthesized column list after PRIMARY KEY
    //            int startIdx = arr[1].indexOf("(");
    //            if (startIdx < 0) {
    //                return null;
    //            }
    //            int endIdx = arr[1].indexOf(")", startIdx);
    //            if (endIdx < 0) {
    //                return null;
    //            }
    //            String pkColumns = arr[1].substring(startIdx + 1, endIdx);
    //            String[] colArr = pkColumns.split(",");
    //            for (String s : colArr) {
    //                s = s.trim();
    //                // Check against Postgresql double-quoted identifier
    //                if (StringUtil.containsIgnoreCase(s, DBUtil.wrap(columnName))
    //                        || StringUtil.containsIgnoreCase(s, columnName)) {
    //                    // Check for key size like "COL"(32)
    //                    int idx = s.indexOf("(");
    //                    if (idx >= 0) {
    //                        return Integer.parseInt(s.substring(idx + 1, s.indexOf(")", idx)));
    //                    }
    //                    return null;
    //                }
    //            }
    //        }
    //        return null;
    //    }

//    public static Long lastInsertId(Connection connection) throws Exception {
//        //        String sql = "SELECT IDENTITY_VAL_LOCAL();";
//        String sql = "SELECT @@IDENTITY FROM DUAL";
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

    //    public static String columnType(Connection connection, String dbName, String tableName, String columnName) throws Exception {
    //        String sql = "SELECT DATA_TYPE FROM ALL_TAB_COLUMNS WHERE OWNER = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?";
    //        PreparedStatement stmt = connection.prepareStatement(sql);
    //        stmt.setString(1, dbName);
    //        stmt.setString(2, tableName);
    //        stmt.setString(3, columnName);
    //        ResultSet resultSet = stmt.executeQuery();
    //        String colType = null;
    //        if (resultSet.next()) {
    //            colType = resultSet.getString(1);
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(stmt);
    //        return colType;
    //    }

    /**
     * 解析字段列表
     *
     * @param resultSet 结果集
     * @return 字段列表
     * @throws SQLException 异常
     */
    public static PostgresqlColumns parseColumns(ResultSet resultSet) throws SQLException {
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
    public static PostgresqlColumns parseColumns(ResultSet resultSet, List<String> excludes) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        PostgresqlColumns columns = new PostgresqlColumns();
        // 遍历结果集并输出列名
        for (int i = 1; i <= columnCount; i++) {
            // 获取列名
            String columnLabel = resultSetMetaData.getColumnLabel(i);
            if (excludes.contains(columnLabel)) {
                continue;
            }
            String columnTypeName = normalizeColumnType(resultSetMetaData.getColumnTypeName(i));
            int displaySize = resultSetMetaData.getColumnDisplaySize(i);
            int precision = resultSetMetaData.getPrecision(i);
            int scale = resultSetMetaData.getScale(i);
            boolean autoIncrement = resultSetMetaData.isAutoIncrement(i);
            int nullable = resultSetMetaData.isNullable(i);
            String tableName = resultSetMetaData.getTableName(i);
            String schemaName = resultSetMetaData.getSchemaName(i);

            PostgresqlColumn dbColumn = new PostgresqlColumn();
            dbColumn.setDigits(scale);
            dbColumn.setName(columnLabel);
            // PostgreSQL的text等类型displaySize会返回极大值，优先使用precision
            if (precision > 0) {
                dbColumn.setSize(precision);
            } else if (displaySize > 0 && displaySize < 1_000_000) {
                dbColumn.setSize(displaySize);
            }
            dbColumn.setSchema(schemaName);
            dbColumn.setTableName(tableName);
            dbColumn.setType(columnTypeName);
            dbColumn.setAutoIncrement(autoIncrement);
            dbColumn.setNullable(nullable == ResultSetMetaData.columnNullable);

            columns.add(dbColumn);
        }
        return columns;
    }

    /**
     * 规范化PostgreSQL字段类型名称
     *
     * @param typeName 类型名称
     * @return 规范化后的类型名称
     */
    private static String normalizeColumnType(String typeName) {
        if (typeName == null) {
            return null;
        }
        return switch (typeName.toLowerCase()) {
            case "int2", "smallserial" -> "SMALLINT";
            case "int4", "serial" -> "INTEGER";
            case "int8", "bigserial" -> "BIGINT";
            case "float4" -> "REAL";
            case "float8" -> "DOUBLE PRECISION";
            case "bool" -> "BOOLEAN";
            case "bpchar" -> "CHAR";
            case "varchar" -> "VARCHAR";
            case "timestamptz" -> "TIMESTAMP WITH TIME ZONE";
            case "timetz" -> "TIME WITH TIME ZONE";
            case "timestamp" -> "TIMESTAMP";
            case "time" -> "TIME";
            case "numeric" -> "NUMERIC";
            case "decimal" -> "DECIMAL";
            default -> typeName.toUpperCase();
        };
    }

    //    public static String showCreateView(Connection connection, String viewName) throws Exception {
    //        // PostgreSQL中 DBMS_METADATA.GET_DDL 可能因权限不足失败，直接从 USER_VIEWS 查询视图定义
    //        String sql = "SELECT TEXT FROM USER_VIEWS WHERE VIEW_NAME = ?";
    //        PreparedStatement statement = connection.prepareStatement(sql);
    //        statement.setString(1, viewName);
    //        ResultSet resultSet = statement.executeQuery();
    //        String createDefinition = "";
    //        if (resultSet.next()) {
    //            String text = resultSet.getString(1);
    //            createDefinition = "CREATE OR REPLACE VIEW " + viewName + " AS\n" + text;
    //        }
    //        DBUtil.close(resultSet);
    //        DBUtil.close(statement);
    //        return createDefinition;
    //    }

    /**
     * 修正触发器定义
     *
     * @param definition 定义
     * @return 结果
     */
    public static String fixTiggerDefinition(String definition) {
        if (StringUtil.isBlank(definition)) {
            return definition;
        }
        // PostgreSQL触发器定义可能是完整的函数定义，优先截取函数体
        if (StringUtil.containsIgnoreCase(definition, "CREATE ")
                && StringUtil.containsIgnoreCase(definition, "FUNCTION ")) {
            String body = parseFunctionBody(definition);
            if (StringUtil.isNotBlank(body)) {
                definition = body;
            }
        }
        return definition.trim();
    }

    /**
     * 规范化PostgreSQL触发器函数体，确保函数不会执行到末尾仍未返回
     *
     * @param definition 触发器定义
     * @param policy     触发器策略
     * @return 函数体
     */
    public static String normalizeTriggerBody(String definition, String policy) {
        String body = definition == null ? null : definition.trim();
        if (StringUtil.isBlank(body)) {
            return null;
        }
        String upper = body.toUpperCase();
        if (!upper.startsWith("BEGIN") && !upper.startsWith("DECLARE")) {
            if (!body.endsWith(";")) {
                body = body + ";";
            }
            body = "BEGIN " + body + " END;";
        } else if (!body.endsWith(";")) {
            body = body + ";";
        }
        // 避免 BEGIN 与后续语句粘连成 BEGINinsert 等非法标识符
        body = Pattern.compile("(?i)(^|[\\s;])BEGIN(?=\\S)")
                .matcher(body)
                .replaceAll("$1BEGIN ");
        int endIndex = body.toUpperCase().lastIndexOf("END");
        if (endIndex < 0) {
            return body;
        }
        String beforeEnd = body.substring(0, endIndex).stripTrailing();
        // 最后一条语句不是RETURN时补充默认返回路径
        if (!Pattern.compile("(?is)RETURN\\s+[^;]*;\\s*$").matcher(beforeEnd).find()) {
            String returnValue = StringUtil.containsIgnoreCase(policy, "DELETE") ? "OLD" : "NEW";
            beforeEnd = beforeEnd + " RETURN " + returnValue + ";";
        }
        body = beforeEnd + " " + body.substring(endIndex);
        return body;
    }

    /**
     * 解析函数定义中的函数体
     * 支持AS $$ ... $$、AS $tag$ ... $tag$以及AS '...'三种形式
     *
     * @param createDefinition 函数创建定义
     * @return 函数体
     */
    public static String parseFunctionBody(String createDefinition) {
        if (StringUtil.isBlank(createDefinition)) {
            return createDefinition;
        }
        // AS $tag$ ... $tag$
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?is)\\bAS\\s+\\$([A-Za-z_][A-Za-z0-9_]*)\\$(.*?)\\$\\1\\$")
                .matcher(createDefinition);
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        // AS $$ ... $$
        matcher = java.util.regex.Pattern
                .compile("(?is)\\bAS\\s+\\$\\$(.*?)\\$\\$")
                .matcher(createDefinition);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // AS '...'
        matcher = java.util.regex.Pattern
                .compile("(?is)\\bAS\\s+'((?:[^']|'')*)'")
                .matcher(createDefinition);
        if (matcher.find()) {
            return matcher.group(1).replace("''", "'").trim();
        }
        return createDefinition;
    }

    //    public static void parseJobExpr(String expr, PostgresqlEvent event){
    //        if (StringUtil.isNotBlank(expr)) {
    //            String[] arr = expr.split(";");
    //            for (String s : arr) {
    //                if (StringUtil.startWithIgnoreCase(s, "FREQ")) {
    //                    event.setIntervalField(s.split("=")[1]);
    //                }else if (StringUtil.startWithIgnoreCase(s, "INTERVAL")) {
    //                    event.setIntervalValue(Integer.valueOf(s.split("=")[1]));
    //                }
    //            }
    //        }
    //    }

    //    public static void parseJobAction(String action, PostgresqlEvent event){
    //        if (StringUtil.isNotBlank(action)) {
    //        }
    //    }

//    /**
//     * 是否自增列错误
//     *
//     * @param err 错误
//     * @return 结果
//     */
//    public static boolean isIdentityError(Throwable err) {
//        if (ExceptionUtil.hasMessage(err, "不存在IDENTITY列")) {
//            return true;
//        }
//        if (ExceptionUtil.hasMessage(err, "[")
//                && ExceptionUtil.hasMessage(err, "]")
//                && ExceptionUtil.hasMessage(err, "IDENTITY")) {
//            return true;
//        }
//        return false;
//    }

    /**
     * 默认环境变量
     */
    public static Map<String, String> DEFAULT_ENVIRONMENT = new HashMap<>();

    static {
        // 连接在 pg_stat_activity 中显示的名称
        DEFAULT_ENVIRONMENT.put("ApplicationName", "EasyShell");
        // TCP keepalive，减少长时间空闲连接被中间网络设备断开
        DEFAULT_ENVIRONMENT.put("tcpKeepAlive", "true");
        // 批量插入重写，提高导入/批量插入性能
        DEFAULT_ENVIRONMENT.put("reWriteBatchedInserts", "true");
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
