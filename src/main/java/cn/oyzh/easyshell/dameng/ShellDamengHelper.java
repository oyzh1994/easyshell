package cn.oyzh.easyshell.dameng;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
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
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/7/1
 */
public class ShellDamengHelper {

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

    //public static List<DamengRoutineParam> listRoutineParam(Connection connection, String dbName, String routineName, String routineType) throws Exception {
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
    //    List<DamengRoutineParam> params = new ArrayList<>();
    //    PreparedStatement statement = connection.prepareStatement(sql);
    //    statement.setString(1, dbName);
    //    statement.setString(2, routineName);
    //    // 执行SQL查询并获取结果集
    //    ResultSet resultSet = statement.executeQuery();
    //    while (resultSet.next()) {
    //        DamengRoutineParam param = new DamengRoutineParam();
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
    //public static List<DamengRoutineParam> listFunctionParam(Connection connection, String dbName, String functionName) throws Exception {
    //    return listRoutineParam(connection, dbName, functionName, "FUNCTION");
    //}
    //
    //public static List<DamengRoutineParam> listProcedureParam(Connection connection, String dbName, String procedureName) throws Exception {
    //    return listRoutineParam(connection, dbName, procedureName, "PROCEDURE");
    //}

    //    public static String getProcedureDefiner(Connection connection, String procedureName) throws Exception {
    //        // 达梦通过ALL_PROCEDURES查询定义者，而非SHOW CREATE PROCEDURE
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

    public static boolean isViewUpdatable(Connection connection, String schema, String viewName) throws Exception {
        try {
            String sql = """
                        SELECT
                            READ_ONLY AS IS_UPDATABLE
                        FROM 
                            USER_VIEWS
                        WHERE 
                            VIEW_NAME = ?
                    """;
            DBUtil.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, viewName);
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
            return isUpdatable == null || StringUtil.equalsIgnoreCase(isUpdatable, "N");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
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
    //        // Dameng does not support ZEROFILL; always return false
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
    //                // Check against Dameng double-quoted identifier
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

    public static DamengColumns parseColumns(ResultSet resultSet) throws SQLException {
        return parseColumns(resultSet, Collections.emptyList());
    }

    public static DamengColumns parseColumns(ResultSet resultSet, List<String> excludes) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        DamengColumns columns = new DamengColumns();
        // 遍历结果集并输出列名
        for (int i = 1; i <= columnCount; i++) {
            // 获取列名
//            String columnName = resultSetMetaData.getColumnLabel(i);
            String columnLabel = resultSetMetaData.getColumnLabel(i);
            if (excludes.contains(columnLabel)) {
                continue;
            }
            int columnType = resultSetMetaData.getColumnType(i);
            String columnTypeName = resultSetMetaData.getColumnTypeName(i);
            int displaySize = resultSetMetaData.getColumnDisplaySize(i);
            boolean signed = resultSetMetaData.isSigned(i);
            boolean readOnly = resultSetMetaData.isReadOnly(i);
            boolean writable = resultSetMetaData.isWritable(i);
            boolean searchable = resultSetMetaData.isSearchable(i);
            int precision = resultSetMetaData.getPrecision(i);
            boolean caseSensitive = resultSetMetaData.isCaseSensitive(i);
            boolean currency = resultSetMetaData.isCurrency(i);
            boolean autoIncrement = resultSetMetaData.isAutoIncrement(i);
            boolean definitelyWritable = resultSetMetaData.isDefinitelyWritable(i);
            int nullable = resultSetMetaData.isNullable(i);
            int scale = resultSetMetaData.getScale(i);
            String tableName = resultSetMetaData.getTableName(i);
            String schemaName = resultSetMetaData.getSchemaName(i);
            String catalogName = resultSetMetaData.getCatalogName(i);
            String columnClassName = resultSetMetaData.getColumnClassName(i);

            System.out.println("columnType=" + columnType);
            System.out.println("columnTypeName=" + columnTypeName);
//            System.out.println("columnName=" + columnName);
            System.out.println("columnLabel=" + columnLabel);
            System.out.println("displaySize=" + displaySize);
            System.out.println("signed=" + signed);
            System.out.println("readOnly=" + readOnly);
            System.out.println("writable=" + writable);
            System.out.println("searchable=" + searchable);
            System.out.println("precision=" + precision);
            System.out.println("caseSensitive=" + caseSensitive);
            System.out.println("currency=" + currency);
            System.out.println("autoIncrement=" + autoIncrement);
            System.out.println("definitelyWritable=" + definitelyWritable);
            System.out.println("nullable=" + nullable);
            System.out.println("scale=" + scale);
            System.out.println("tableName=" + tableName);
            System.out.println("schemaName=" + schemaName);
            System.out.println("catalogName=" + catalogName);
            System.out.println("columnClassName=" + columnClassName);
            System.out.println("---------------");

            DamengColumn dbColumn = new DamengColumn();
            dbColumn.setDigits(scale);
            dbColumn.setName(columnLabel);
            dbColumn.setSize(displaySize);
            dbColumn.setSchema(schemaName);
            dbColumn.setTableName(tableName);
            dbColumn.setType(columnTypeName);
            dbColumn.setAutoIncrement(autoIncrement);
            dbColumn.setNullable(nullable == ResultSetMetaData.columnNullable);

            columns.add(dbColumn);
        }
        return columns;
    }

    //    public static String showCreateView(Connection connection, String viewName) throws Exception {
    //        // 达梦中 DBMS_METADATA.GET_DDL 可能因权限不足失败，直接从 USER_VIEWS 查询视图定义
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
        if (StringUtil.contains(definition, " ROW BEGIN ") && StringUtil.contains(definition, "END;")) {
            definition = definition.substring(definition.indexOf(" ROW BEGIN ") + 11, definition.lastIndexOf("END;"));
        }
        return definition;
    }

    //    public static void parseJobExpr(String expr, DamengEvent event){
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

    //    public static void parseJobAction(String action, DamengEvent event){
    //        if (StringUtil.isNotBlank(action)) {
    //        }
    //    }

    /**
     * 是否自增列错误
     *
     * @param err 错误
     * @return 结果
     */
    public static boolean isIdentityError(Throwable err) {
        if (ExceptionUtil.hasMessage(err, "不存在IDENTITY列")) {
            return true;
        }
        if (ExceptionUtil.hasMessage(err, "[")
                && ExceptionUtil.hasMessage(err, "]")
                && ExceptionUtil.hasMessage(err, "IDENTITY")) {
            return true;
        }
        return false;
    }

    public static Map<String, String> DEFAULT_ENVIRONMENT = new HashMap<>();

    static {
    }

    public static String defaultEnvironment() {
        StringBuilder sb = new StringBuilder();
        DEFAULT_ENVIRONMENT.forEach((key, value) -> {
            sb.append(key).append("=").append(value).append("\n");
        });
        return sb.toString();
    }
}
