package cn.oyzh.easyshell.oracle;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.db.sql.DBOraclePagination;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Oracle工具类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleHelper {

    private static final Pattern VIEW_READ_ONLY_PATTERN = Pattern.compile(
            "(?is)\\s+WITH\\s+READ\\s+ONLY\\s*;?\\s*$");

    private static final Pattern OBJECT_NOT_EXISTS_PATTERN = Pattern.compile(
            "ORA-(00942|02289|04043|04080)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern TRIGGER_ALTER_SUFFIX_PATTERN = Pattern.compile(
            "(?is)\\s+ALTER\\s+TRIGGER\\s+.+?\\s+(?:ENABLE|DISABLE)\\s*;?\\s*$");

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

    //public static List<OracleRoutineParam> listRoutineParam(Connection connection, String dbName, String routineName, String routineType) throws Exception {
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
    //    List<OracleRoutineParam> params = new ArrayList<>();
    //    PreparedStatement statement = connection.prepareStatement(sql);
    //    statement.setString(1, dbName);
    //    statement.setString(2, routineName);
    //    // 执行SQL查询并获取结果集
    //    ResultSet resultSet = statement.executeQuery();
    //    while (resultSet.next()) {
    //        OracleRoutineParam param = new OracleRoutineParam();
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
    //public static List<OracleRoutineParam> listFunctionParam(Connection connection, String dbName, String functionName) throws Exception {
    //    return listRoutineParam(connection, dbName, functionName, "FUNCTION");
    //}
    //
    //public static List<OracleRoutineParam> listProcedureParam(Connection connection, String dbName, String procedureName) throws Exception {
    //    return listRoutineParam(connection, dbName, procedureName, "PROCEDURE");
    //}

    //    public static String getProcedureDefiner(Connection connection, String procedureName) throws Exception {
    //        // Oracle通过ALL_PROCEDURES查询定义者，而非SHOW CREATE PROCEDURE
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
                    SELECT CASE WHEN COUNT(*) > 0 THEN 'Y' ELSE 'N' END AS IS_UPDATABLE
                    FROM ALL_UPDATABLE_COLUMNS
                    WHERE OWNER = ? AND TABLE_NAME = ?
                    """;
            DBUtil.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, viewName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            String isUpdatable = "";
            if (resultSet.next()) {
                isUpdatable = resultSet.getString("IS_UPDATABLE");
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return StringUtil.equalsIgnoreCase(isUpdatable, "Y");
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
                    SELECT CASE WHEN COUNT(*) > 0 THEN 'Y' ELSE 'N' END AS READ_ONLY
                    FROM ALL_UPDATABLE_COLUMNS
                    WHERE OWNER = ? AND TABLE_NAME = ?
                    AND UPDATABLE = 'YES'
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
                readOnly = "Y".equalsIgnoreCase(resultSet.getString("READ_ONLY")) ? "N" : "Y";
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
    //        // Oracle does not support ZEROFILL; always return false
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
    //                // Check against Oracle double-quoted identifier
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
    public static OracleColumns parseColumns(ResultSet resultSet) throws SQLException {
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
    public static OracleColumns parseColumns(ResultSet resultSet, List<String> excludes) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        OracleColumns columns = new OracleColumns();
        // 遍历结果集并输出列名
        for (int i = 1; i <= columnCount; i++) {
            // 获取列名
//            String columnName = resultSetMetaData.getColumnLabel(i);
            String columnLabel = resultSetMetaData.getColumnLabel(i);
            if (excludes.contains(columnLabel)
                    || DBOraclePagination.ROW_NUMBER_ALIAS.equalsIgnoreCase(columnLabel)) {
                continue;
            }
            int columnType = resultSetMetaData.getColumnType(i);
            String columnTypeName = resultSetMetaData.getColumnTypeName(i);
            int displaySize = resultSetMetaData.getColumnDisplaySize(i);
            boolean autoIncrement = resultSetMetaData.isAutoIncrement(i);
            int nullable = resultSetMetaData.isNullable(i);
            int scale = resultSetMetaData.getScale(i);
            String tableName = resultSetMetaData.getTableName(i);
            String schemaName = resultSetMetaData.getSchemaName(i);
            OracleColumn dbColumn = new OracleColumn();
            dbColumn.setDigits(scale);
            dbColumn.setName(columnLabel);
            dbColumn.setSize(displaySize);
            dbColumn.setSchema(schemaName);
            dbColumn.setTableName(tableName);
            dbColumn.setType(columnTypeName);
            dbColumn.setAutoIncrement(autoIncrement);
            dbColumn.setNullable(nullable == ResultSetMetaData.columnNullable);
            if ("ANYDATA".equalsIgnoreCase(columnTypeName)) {
                dbColumn.setTypeCode("ANYDATA");
                dbColumn.setTypeOwner(schemaName);
            } else if (!"XMLTYPE".equalsIgnoreCase(columnTypeName)
                    && columnType == Types.STRUCT) {
                dbColumn.setTypeCode("OBJECT");
                dbColumn.setTypeOwner(schemaName);
            } else if (columnType == Types.ARRAY) {
                dbColumn.setTypeCode("COLLECTION");
                dbColumn.setTypeOwner(schemaName);
            }

            columns.add(dbColumn);
        }
        return columns;
    }

    //    public static String showCreateView(Connection connection, String viewName) throws Exception {
    //        // Oracle中 DBMS_METADATA.GET_DDL 可能因权限不足失败，直接从 USER_VIEWS 查询视图定义
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
        return normalizeTriggerBody(definition);
    }

    /**
     * 将完整触发器DDL、PL/SQL块或裸PL/SQL语句统一为可嵌入触发器的主体。
     */
    public static String normalizeTriggerBody(String definition) {
        if (StringUtil.isBlank(definition)) {
            return "NULL;";
        }
        String body = definition.strip();
        if (body.endsWith("/")) {
            body = body.substring(0, body.length() - 1).stripTrailing();
        }
        if (body.matches("(?is)^CREATE(\\s+OR\\s+REPLACE)?\\s+TRIGGER\\s+.*")) {
            int beginIndex = indexOfKeyword(body, "BEGIN");
            int declareIndex = indexOfKeyword(body, "DECLARE");
            int startIndex = Math.min(beginIndex < 0 ? Integer.MAX_VALUE : beginIndex,
                    declareIndex < 0 ? Integer.MAX_VALUE : declareIndex);
            if (startIndex == Integer.MAX_VALUE) {
                return "NULL;";
            }
            body = body.substring(startIndex);
        }
        if (!body.endsWith(";")) {
            body += ";";
        }
        if (body.matches("(?is)^BEGIN\\b.*") || body.matches("(?is)^DECLARE\\b.*")) {
            return body;
        }
        return "BEGIN " + body + " END;";
    }

    public static String renameRoutineDefinition(String definition, String routineType, String oldName, String newName) {
        if (StringUtil.isBlank(definition)) {
            throw new IllegalArgumentException("Routine definition is empty");
        }
        if (StringUtil.isBlank(oldName) || StringUtil.isBlank(newName)) {
            throw new IllegalArgumentException("Routine name is empty");
        }

        String result = removeSqlPlusTerminator(definition);
        String identifier = "(?:\"(?:[^\"]|\"\")*\"|[A-Za-z][A-Za-z0-9_$#]*)";
        Pattern pattern = Pattern.compile(
                "(?is)(\\bCREATE\\s+(?:OR\\s+REPLACE\\s+)?(?:(?:NON)?EDITIONABLE\\s+)?"
                        + Pattern.quote(routineType) + "\\s+)((?:" + identifier + "\\s*\\.\\s*)?)(" + identifier + ")");
        Matcher matcher = pattern.matcher(result);
        while (matcher.find()) {
            if (identifierEquals(matcher.group(3), oldName)) {
                String replacement = matcher.group(1) + matcher.group(2)
                        + DBUtil.wrap(newName, DBDialect.ORACLE);
                return result.substring(0, matcher.start()) + replacement + result.substring(matcher.end());
            }
        }
        throw new IllegalArgumentException("Unable to locate " + routineType.toLowerCase(Locale.ROOT)
                + " definition name: " + oldName);
    }

    public static String removeSqlPlusTerminator(String definition) {
        String result = definition.strip();
        return result.endsWith("/") ? result.substring(0, result.length() - 1).stripTrailing() : result;
    }

    private static boolean identifierEquals(String value, String expected) {
        String normalizedValue = unwrapIdentifier(value);
        String normalizedExpected = unwrapIdentifier(expected);
        return value.startsWith("\"") || expected.startsWith("\"")
                ? normalizedValue.equals(normalizedExpected)
                : normalizedValue.equalsIgnoreCase(normalizedExpected);
    }

    private static String unwrapIdentifier(String value) {
        String result = value.strip();
        if (result.length() > 1 && result.startsWith("\"") && result.endsWith("\"")) {
            return result.substring(1, result.length() - 1).replace("\"\"", "\"");
        }
        return result;
    }

    public static String getTableDefinition(Connection connection, String schema, String tableName) throws SQLException {
        try {
            String definition = getMetadataDefinition(connection, schema, "TABLE", tableName);
            return StringUtil.isBlank(definition) ? buildTableDefinition(connection, schema, tableName) : definition;
        } catch (SQLException ex) {
            return buildTableDefinition(connection, schema, tableName);
        }
    }

    public static String getViewDefinition(Connection connection, String schema, String viewName) throws SQLException {
        try {
            String definition = getMetadataDefinition(connection, schema, "VIEW", viewName);
            return StringUtil.isBlank(definition) ? buildViewDefinition(connection, schema, viewName) : definition;
        } catch (SQLException ex) {
            return buildViewDefinition(connection, schema, viewName);
        }
    }

    /**
     * 移除视图查询末尾的WITH READ ONLY子句。
     *
     * @param definition 视图查询
     * @return 不包含只读子句的查询
     */
    public static String stripViewReadOnlyClause(String definition) {
        if (StringUtil.isBlank(definition)) {
            return definition;
        }
        String result = definition.strip();
        Matcher matcher = VIEW_READ_ONLY_PATTERN.matcher(result);
        if (matcher.find()) {
            result = result.substring(0, matcher.start()).stripTrailing();
        }
        return result;
    }

    public static String getFunctionDefinition(Connection connection, String schema, String functionName) throws SQLException {
        try {
            String definition = getMetadataDefinition(connection, schema, "FUNCTION", functionName);
            return StringUtil.isBlank(definition) ? buildRoutineDefinition(connection, schema, functionName, "FUNCTION") : definition;
        } catch (SQLException ex) {
            return buildRoutineDefinition(connection, schema, functionName, "FUNCTION");
        }
    }

    public static String getProcedureDefinition(Connection connection, String schema, String procedureName) throws SQLException {
        try {
            String definition = getMetadataDefinition(connection, schema, "PROCEDURE", procedureName);
            return StringUtil.isBlank(definition) ? buildRoutineDefinition(connection, schema, procedureName, "PROCEDURE") : definition;
        } catch (SQLException ex) {
            return buildRoutineDefinition(connection, schema, procedureName, "PROCEDURE");
        }
    }

    public static String getTriggerDefinition(Connection connection, String schema, String triggerName) throws SQLException {
        try {
            String definition = getMetadataDefinition(connection, schema, "TRIGGER", triggerName);
            return StringUtil.isBlank(definition)
                    ? normalizeTriggerDefinition(buildTriggerDefinition(connection, schema, triggerName))
                    : normalizeTriggerDefinition(definition);
        } catch (SQLException ex) {
            return normalizeTriggerDefinition(buildTriggerDefinition(connection, schema, triggerName));
        }
    }

    public static String getSequenceDefinition(Connection connection, String schema, String sequenceName) throws SQLException {
        try {
            String definition = getMetadataDefinition(connection, schema, "SEQUENCE", sequenceName);
            return StringUtil.isBlank(definition) ? buildSequenceDefinition(connection, schema, sequenceName) : definition;
        } catch (SQLException ex) {
            return buildSequenceDefinition(connection, schema, sequenceName);
        }
    }

    private static String getMetadataDefinition(Connection connection, String schema, String objectType, String objectName) throws SQLException {
        String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) FROM DUAL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, objectType);
            statement.setString(2, objectName);
            statement.setString(3, schema);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : "";
            }
        }
    }

    private static String buildViewDefinition(Connection connection, String schema, String viewName) throws SQLException {
        String sql = "SELECT TEXT FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, viewName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return "";
                }
                String name = DBUtil.wrap(schema, viewName, DBDialect.ORACLE);
                return "CREATE OR REPLACE VIEW " + name + " AS\n" + resultSet.getString(1);
            }
        }
    }

    private static String buildRoutineDefinition(Connection connection, String schema, String routineName, String routineType) throws SQLException {
        String sql = """
                SELECT TEXT
                FROM ALL_SOURCE
                WHERE OWNER = ?
                AND NAME = ?
                AND TYPE = ?
                ORDER BY LINE
                """;
        StringBuilder source = new StringBuilder();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, routineName);
            statement.setString(3, routineType);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    source.append(resultSet.getString(1));
                }
            }
        }
        if (source.isEmpty()) {
            return "";
        }
        String definition = source.toString().strip();
        String routinePrefix = "^CREATE(?:\\s+OR\\s+REPLACE)?(?:\\s+(?:NON)?EDITIONABLE)?\\s+"
                + Pattern.quote(routineType) + "\\b";
        return definition.matches("(?is)" + routinePrefix)
                ? definition
                : "CREATE OR REPLACE " + definition;
    }

    private static String buildTriggerDefinition(Connection connection, String schema, String triggerName) throws SQLException {
        String sql = """
                SELECT DESCRIPTION, TRIGGER_BODY, WHEN_CLAUSE
                FROM ALL_TRIGGERS
                WHERE OWNER = ?
                AND TRIGGER_NAME = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, triggerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return "";
                }
                String description = resultSet.getString(1);
                String body = resultSet.getString(2);
                String whenClause = resultSet.getString(3);
                if (StringUtil.isBlank(description)) {
                    description = DBUtil.wrap(schema, triggerName, DBDialect.ORACLE);
                }
                StringBuilder definition = new StringBuilder("CREATE OR REPLACE TRIGGER ")
                        .append(description.strip());
                if (StringUtil.isNotBlank(whenClause)) {
                    definition.append(" WHEN ").append(whenClause.strip());
                }
                return definition.append('\n').append(normalizeTriggerBody(body)).toString();
            }
        }
    }

    /**
     * 规范触发器 WHEN 条件，Oracle 要求 WHEN 后的条件使用括号包裹。
     *
     * @param definition 触发器定义
     * @return 规范后的触发器定义
     */
    public static String normalizeTriggerDefinition(String definition) {
        if (StringUtil.isBlank(definition)) {
            return definition;
        }
        definition = TRIGGER_ALTER_SUFFIX_PATTERN.matcher(definition).replaceFirst("");
        int whenIndex = indexOfKeyword(definition, "WHEN");
        if (whenIndex < 0) {
            return definition;
        }
        int conditionStart = whenIndex + "WHEN".length();
        while (conditionStart < definition.length() && Character.isWhitespace(definition.charAt(conditionStart))) {
            conditionStart++;
        }
        if (conditionStart < definition.length() && definition.charAt(conditionStart) == '(') {
            return definition;
        }
        int beginIndex = indexOfKeyword(definition, "BEGIN", conditionStart);
        if (beginIndex < conditionStart) {
            return definition;
        }
        String condition = definition.substring(conditionStart, beginIndex).strip();
        if (condition.isEmpty()) {
            return definition;
        }
        return definition.substring(0, conditionStart)
                + "(" + condition + ") "
                + definition.substring(beginIndex);
    }

    /**
     * 构造可重复执行的 Oracle 删除语句。对象不存在时忽略，其他错误继续抛出。
     *
     * @param objectType 对象类型
     * @param objectName 对象名称
     * @return 删除语句块
     */
    public static String buildDropIfExistsSql(String objectType, String objectName) {
        return buildDropIfExistsSql(objectType, objectName, "");
    }

    /**
     * 构造可重复执行的 Oracle 删除语句。
     *
     * @param objectType 对象类型
     * @param objectName 对象名称
     * @param options    删除选项
     * @return 删除语句块
     */
    public static String buildDropIfExistsSql(String objectType, String objectName, String options) {
        String dropSql = "DROP " + objectType + " " + DBUtil.wrap(objectName, DBDialect.ORACLE);
        if (StringUtil.isNotBlank(options)) {
            dropSql += " " + options.strip();
        }
        return """
                BEGIN
                    EXECUTE IMMEDIATE '%s';
                EXCEPTION
                    WHEN OTHERS THEN
                        IF SQLCODE NOT IN (-942, -2289, -4043, -4080, -32794) THEN
                            RAISE;
                        END IF;
                END;
                /
                """.formatted(dropSql.replace("'", "''"));
    }

    /**
     * 判断异常是否为 Oracle 对象不存在。
     *
     * @param throwable 异常
     * @return 结果
     */
    public static boolean isObjectNotExists(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof SQLException) {
                int errorCode = ((SQLException) cause).getErrorCode();
                if (errorCode == 942 || errorCode == 2289 || errorCode == 4043 || errorCode == 4080) {
                    return true;
                }
            }
            String message = cause.getMessage();
            if (StringUtil.isNotBlank(message) && OBJECT_NOT_EXISTS_PATTERN.matcher(message).find()) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * 判断是否为表空间不可用错误。
     *
     * @param throwable 异常
     * @return 结果
     */
    public static boolean isTablespaceUnavailable(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof SQLException && ((SQLException) cause).getErrorCode() == 1129) {
                return true;
            }
            String message = cause.getMessage();
            if (StringUtil.isNotBlank(message) && message.contains("ORA-01129")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * 判断是否为可忽略的删除对象不存在错误。
     *
     * @param sql SQL语句
     * @param ex  异常
     * @return 结果
     */
    public static boolean isIgnorableDropError(String sql, Throwable ex) {
        return StringUtil.isNotBlank(sql)
                && StringUtil.startWithIgnoreCase(stripLeadingComments(sql), "DROP ")
                && isObjectNotExists(ex);
    }

    /**
     * 去除SQL前导空白与注释。
     *
     * @param sql SQL语句
     * @return 去除后的SQL
     */
    public static String stripLeadingComments(String sql) {
        if (sql == null) {
            return "";
        }
        String result = sql.stripLeading();
        while (!result.isEmpty()) {
            if (result.startsWith("/*")) {
                int end = result.indexOf("*/", 2);
                if (end < 0) {
                    return result;
                }
                result = result.substring(end + 2).stripLeading();
            } else if (result.startsWith("--")) {
                int end = result.indexOf('\n');
                if (end < 0) {
                    return "";
                }
                result = result.substring(end + 1).stripLeading();
            } else {
                break;
            }
        }
        return result;
    }

    private static String buildSequenceDefinition(Connection connection, String schema, String sequenceName) throws SQLException {
        String sql = """
                SELECT MIN_VALUE, MAX_VALUE, INCREMENT_BY, LAST_NUMBER, CACHE_SIZE, CYCLE_FLAG, ORDER_FLAG
                FROM ALL_SEQUENCES
                WHERE SEQUENCE_OWNER = ?
                AND SEQUENCE_NAME = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, sequenceName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return "";
                }
                BigDecimal minValue = resultSet.getBigDecimal(1);
                BigDecimal maxValue = resultSet.getBigDecimal(2);
                BigDecimal incrementBy = resultSet.getBigDecimal(3);
                BigDecimal current = resultSet.getBigDecimal(4);
                BigDecimal cacheSize = resultSet.getBigDecimal(5);
                boolean cycle = StringUtil.equalsIgnoreCase("Y", resultSet.getString(6));
                boolean ordered = StringUtil.equalsIgnoreCase("Y", resultSet.getString(7));
                return "CREATE SEQUENCE " + DBUtil.wrap(schema, sequenceName, DBDialect.ORACLE)
                        + " MINVALUE " + decimal(minValue, BigDecimal.ONE)
                        + " MAXVALUE " + decimal(maxValue, new BigDecimal("9999999999999999999999999999"))
                        + " INCREMENT BY " + decimal(incrementBy, BigDecimal.ONE)
                        + " START WITH " + decimal(current, BigDecimal.ONE)
                        + (cacheSize == null || cacheSize.signum() <= 0 ? " NOCACHE" : " CACHE " + cacheSize.toPlainString())
                        + (cycle ? " CYCLE" : " NOCYCLE")
                        + (ordered ? " ORDER" : " NOORDER");
            }
        }
    }

    private static String buildTableDefinition(Connection connection, String schema, String tableName) throws SQLException {
        String columnSql = """
                SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE,
                       DATA_DEFAULT, VIRTUAL_COLUMN
                FROM ALL_TAB_COLS
                WHERE OWNER = ?
                AND TABLE_NAME = ?
                AND HIDDEN_COLUMN = 'NO'
                ORDER BY COLUMN_ID
                """;
        List<String> definitions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(columnSql)) {
            statement.setString(1, schema);
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = DBUtil.wrap(resultSet.getString(1), DBDialect.ORACLE);
                    String dataType = resultSet.getString(2);
                    int dataLength = resultSet.getInt(3);
                    int precision = resultSet.getInt(4);
                    int scale = resultSet.getInt(5);
                    boolean nullable = StringUtil.equalsIgnoreCase("Y", resultSet.getString(6));
                    String defaultValue = resultSet.getString(7);
                    boolean virtual = StringUtil.equalsIgnoreCase("YES", resultSet.getString(8));
                    StringBuilder definition = new StringBuilder(name).append(' ').append(columnType(dataType, dataLength, precision, scale));
                    boolean computedVirtual = virtual && !StringUtil.equalsIgnoreCase(dataType, "XMLTYPE");
                    if (computedVirtual && StringUtil.isNotBlank(defaultValue)) {
                        definition.append(" GENERATED ALWAYS AS (").append(defaultValue.strip()).append(") VIRTUAL");
                    } else if (StringUtil.isNotBlank(defaultValue)) {
                        definition.append(" DEFAULT ").append(defaultValue.strip());
                    }
                    if (!computedVirtual && !nullable) {
                        definition.append(" NOT NULL");
                    }
                    definitions.add("  " + definition);
                }
            }
        }
        definitions.addAll(readTableConstraints(connection, schema, tableName));
        definitions.addAll(readForeignKeyConstraints(connection, schema, tableName));
        String tableNameDefinition = DBUtil.wrap(schema, tableName, DBDialect.ORACLE);
        String definition = "CREATE TABLE " + tableNameDefinition + " (\n" + String.join(",\n", definitions) + "\n)";
        String tableSpace = readTableSpace(connection, schema, tableName);
        return StringUtil.isBlank(tableSpace)
                ? definition
                : definition + "\nTABLESPACE " + DBUtil.wrap(tableSpace, DBDialect.ORACLE);
    }

    private static String readTableSpace(Connection connection, String schema, String tableName) throws SQLException {
        String sql = "SELECT TABLESPACE_NAME FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        }
    }

    private static List<String> readTableConstraints(Connection connection, String schema, String tableName) throws SQLException {
        String sql = """
                SELECT C.CONSTRAINT_NAME, C.CONSTRAINT_TYPE, CC.COLUMN_NAME
                FROM ALL_CONSTRAINTS C
                JOIN ALL_CONS_COLUMNS CC
                ON CC.OWNER = C.OWNER
                AND CC.CONSTRAINT_NAME = C.CONSTRAINT_NAME
                AND CC.TABLE_NAME = C.TABLE_NAME
                WHERE C.OWNER = ?
                AND C.TABLE_NAME = ?
                AND C.CONSTRAINT_TYPE IN ('P', 'U')
                ORDER BY C.CONSTRAINT_NAME, CC.POSITION
                """;
        Map<String, StringBuilder> columns = new LinkedHashMap<>();
        Map<String, String> types = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String constraintName = resultSet.getString(1);
                    String constraintType = resultSet.getString(2);
                    String columnName = DBUtil.wrap(resultSet.getString(3), DBDialect.ORACLE);
                    types.put(constraintName, constraintType);
                    StringBuilder values = columns.computeIfAbsent(constraintName, key -> new StringBuilder());
                    if (!values.isEmpty()) {
                        values.append(", ");
                    }
                    values.append(columnName);
                }
            }
        }
        List<String> definitions = new ArrayList<>();
        columns.forEach((constraintName, columnNames) -> {
            String type = StringUtil.equalsIgnoreCase("P", types.get(constraintName)) ? "PRIMARY KEY" : "UNIQUE";
            definitions.add("  CONSTRAINT " + DBUtil.wrap(constraintName, DBDialect.ORACLE)
                    + " " + type + " (" + columnNames + ")");
        });
        return definitions;
    }

    private static List<String> readForeignKeyConstraints(Connection connection, String schema, String tableName) throws SQLException {
        String sql = """
                SELECT C.CONSTRAINT_NAME, CC.COLUMN_NAME, RC.TABLE_NAME, RCC.COLUMN_NAME, C.DELETE_RULE
                FROM ALL_CONSTRAINTS C
                JOIN ALL_CONS_COLUMNS CC
                ON CC.OWNER = C.OWNER
                AND CC.CONSTRAINT_NAME = C.CONSTRAINT_NAME
                AND CC.TABLE_NAME = C.TABLE_NAME
                JOIN ALL_CONSTRAINTS RC
                ON RC.OWNER = C.R_OWNER
                AND RC.CONSTRAINT_NAME = C.R_CONSTRAINT_NAME
                JOIN ALL_CONS_COLUMNS RCC
                ON RCC.OWNER = RC.OWNER
                AND RCC.CONSTRAINT_NAME = RC.CONSTRAINT_NAME
                AND RCC.POSITION = CC.POSITION
                WHERE C.OWNER = ?
                AND C.TABLE_NAME = ?
                AND C.CONSTRAINT_TYPE = 'R'
                ORDER BY C.CONSTRAINT_NAME, CC.POSITION
                """;
        Map<String, ForeignKeyDefinition> constraints = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String constraintName = resultSet.getString(1);
                    String columnName = DBUtil.wrap(resultSet.getString(2), DBDialect.ORACLE);
                    String referencedTable = DBUtil.wrap(resultSet.getString(3), DBDialect.ORACLE);
                    String referencedColumn = DBUtil.wrap(resultSet.getString(4), DBDialect.ORACLE);
                    String deleteRule = resultSet.getString(5);
                    ForeignKeyDefinition definition = constraints.computeIfAbsent(
                            constraintName, key -> new ForeignKeyDefinition(referencedTable, deleteRule));
                    definition.columns.append(definition.columns.isEmpty() ? "" : ", ").append(columnName);
                    definition.referencedColumns.append(
                            definition.referencedColumns.isEmpty() ? "" : ", ").append(referencedColumn);
                }
            }
        }
        List<String> definitions = new ArrayList<>();
        constraints.forEach((constraintName, foreignKey) -> {
            StringBuilder definition = new StringBuilder("  CONSTRAINT ")
                    .append(DBUtil.wrap(constraintName, DBDialect.ORACLE))
                    .append(" FOREIGN KEY (").append(foreignKey.columns).append(")")
                    .append(" REFERENCES ").append(foreignKey.referencedTable)
                    .append(" (").append(foreignKey.referencedColumns).append(")");
            if (StringUtil.equalsIgnoreCase("CASCADE", foreignKey.deleteRule)) {
                definition.append(" ON DELETE CASCADE");
            } else if (StringUtil.equalsIgnoreCase("SET NULL", foreignKey.deleteRule)) {
                definition.append(" ON DELETE SET NULL");
            }
            definitions.add(definition.toString());
        });
        return definitions;
    }

    private static final class ForeignKeyDefinition {
        private final String referencedTable;
        private final String deleteRule;
        private final StringBuilder columns = new StringBuilder();
        private final StringBuilder referencedColumns = new StringBuilder();

        private ForeignKeyDefinition(String referencedTable, String deleteRule) {
            this.referencedTable = referencedTable;
            this.deleteRule = deleteRule;
        }
    }

    private static String columnType(String dataType, int dataLength, int precision, int scale) {
        String normalized = dataType.toUpperCase();
        if (normalized.equals("NUMBER") && precision > 0) {
            return scale > 0 ? "NUMBER(" + precision + "," + scale + ")" : "NUMBER(" + precision + ")";
        }
        if (normalized.contains("CHAR") || normalized.equals("RAW")) {
            return dataType + "(" + dataLength + ")";
        }
        return dataType;
    }

    private static String decimal(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue.toPlainString() : value.toPlainString();
    }

    private static int indexOfKeyword(String value, String keyword) {
        return indexOfKeyword(value, keyword, 0);
    }

    private static int indexOfKeyword(String value, String keyword, int fromIndex) {
        Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        return matcher.find(fromIndex) ? matcher.start() : -1;
    }

    //    public static void parseJobExpr(String expr, OracleEvent event){
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

    //    public static void parseJobAction(String action, OracleEvent event){
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
