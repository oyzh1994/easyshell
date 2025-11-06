package cn.oyzh.easyshell.util.mysql;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.mysql.DBDialect;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.record.MysqlInsertRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordData;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

import java.sql.Connection;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * db工具类
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class DBUtil {

    public static boolean ENABLE_PRINT_METADATA = true;

    /**
     * 是否内部库
     *
     * @param dbName 数据库名称
     * @return 结果
     */
    public static boolean isInternalDatabase(String dbName) {
        return StringUtil.equalsAnyIgnoreCase(dbName, "mysql", "information_schema", "performance_schema");
    }

    /**
     * 检查表类型
     *
     * @param resultSet 结果集
     * @param dbName    数据库名称
     * @return 结果
     * @throws SQLException 异常
     */
    public static boolean checkTableType(ResultSet resultSet, String dbName) throws SQLException {
        String tableCat = resultSet.getString("TABLE_CAT");
        String tableType = resultSet.getString("TABLE_TYPE");
        return StringUtil.equalsIgnoreCase(tableCat, dbName) && !StringUtil.equalsIgnoreCase("VIEW", tableType);
    }

    /**
     * 检查视图类型
     *
     * @param resultSet 结果集
     * @param dbName    数据库名称
     * @return 结果
     * @throws SQLException 异常
     */
    public static boolean checkViewType(ResultSet resultSet, String dbName) throws SQLException {
        String tableCat = resultSet.getString("TABLE_CAT");
        String tableType = resultSet.getString("TABLE_TYPE");
        return StringUtil.equalsIgnoreCase(tableCat, dbName) && StringUtil.equalsIgnoreCase("VIEW", tableType);
    }

    /**
     * 检查数据库是否相同
     *
     * @param resultSet 结果集
     * @param dbName    数据库名称
     * @return 结果
     * @throws SQLException 异常
     */
    public static boolean checkTableCat(ResultSet resultSet, String dbName, String tableName) throws SQLException {
        String tableCat = resultSet.getString("TABLE_CAT");
        String tableName1 = resultSet.getString("TABLE_NAME");
        return Objects.equals(tableCat, dbName) && Objects.equals(tableName1, tableName);
    }

    /**
     * 检查数据库是否相同
     *
     * @param resultSet 结果集
     * @param dbName    数据库名称
     * @return 结果
     * @throws SQLException 异常
     */
    @Deprecated
    public static boolean checkProcedureType(ResultSet resultSet, String dbName) throws SQLException {
        String tableCat = resultSet.getString("PROCEDURE_CAT");
        String type = resultSet.getString("PROCEDURE_TYPE");
        return StringUtil.equals(tableCat, dbName) && "1".equals(type);
    }

    /**
     * 检查数据库是否相同
     *
     * @param resultSet 结果集
     * @param dbName    数据库名称
     * @return 结果
     * @throws SQLException 异常
     */
    @Deprecated
    public static boolean checkFunctionType(ResultSet resultSet, String dbName) throws SQLException {
        String functionCat = resultSet.getString("FUNCTION_CAT");
        String type = resultSet.getString("FUNCTION_TYPE");
        return StringUtil.equalsIgnoreCase(functionCat, dbName) && "1".equals(type);
    }

    /**
     * 检查数据库是否相同
     *
     * @param resultSet 结果集
     * @param dbName    数据库名称
     * @return 结果
     * @throws SQLException 异常
     */
    public static boolean checkFunctionType(ResultSet resultSet, String dbName, String schema) throws SQLException {
        String functionCat = resultSet.getString("FUNCTION_CAT");
        if (!StringUtil.equalsIgnoreCase(functionCat, dbName)) {
            return false;
        }
        if (schema == null) {
            return true;
        }
        String functionSchem = resultSet.getString("FUNCTION_SCHEM");
        if (functionSchem != null) {
            return StringUtil.equalsIgnoreCase(functionSchem, schema);
        }
        return true;
    }

    /**
     * 打印元数据
     *
     * @param resultSet 结果集
     * @throws SQLException 异常
     */
    public static void printMetaData(ResultSet resultSet) throws SQLException {
        if (ENABLE_PRINT_METADATA) {
            // 获取结果集元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            // 获取列数
            int columnCount = metaData.getColumnCount();
            // 遍历结果集并输出列名
            for (int i = 1; i <= columnCount; i++) {
                // 获取列名
                String columnName = metaData.getColumnName(i);
                JulLog.info("Column Name: {}", columnName);
            }
            JulLog.info("printMetaData======================>");
        }
    }

    /**
     * 打印sql
     *
     * @param sql sql语句
     */
    public static void printSql(String sql) {
        JulLog.info("\n" + sql);
        // JulLog.info("printSql======================>");
    }

    /**
     * 打印sql
     *
     * @param sqlList sql列表
     */
    public static void printSql(List<String> sqlList) {
        JulLog.info("\n" + Arrays.toString(sqlList.toArray()));
        // JulLog.info("printSql======================>");
    }

    /**
     * 打印数据
     *
     * @param data 数据
     */
    public static void printData(MysqlRecordData data) {
        if (data != null) {
            for (Map.Entry<MysqlColumn, Object> entry : data.entries()) {
                JulLog.info(entry.getKey().getName() + "=" + entry.getValue());
            }
            JulLog.info("printData======================>");
        }
    }

    /**
     * 打印信息
     *
     * @param sql  sql
     * @param data 数据
     */
    public static void printInfo(String sql, MysqlRecordData data) {
        printSql(sql);
        printData(data);
    }

    // @Deprecated
    // public static String wrap(String name) {
    //     if (name == null) {
    //         return "";
    //     }
    //     StringBuilder builder = new StringBuilder();
    //     if (!name.startsWith("`")) {
    //         builder.append("`");
    //     }
    //     builder.append(name);
    //     if (!name.endsWith("`")) {
    //         builder.append("`");
    //     }
    //     return builder.toString();
    // }

    public static String wrap(String name, DBDialect dialect) {
        StringBuilder builder = new StringBuilder();
        if (dialect == DBDialect.MYSQL) {
            if (!name.startsWith("`")) {
                builder.append("`");
            }
            builder.append(name);
            if (!name.endsWith("`")) {
                builder.append("`");
            }
        }
        return builder.toString();
    }

    // @Deprecated
    // public static String wrap(String dbName, String tableName) {
    //     return wrap(dbName) + "." + wrap(tableName);
    // }

    public static String wrap(String dbName, String tableName, DBDialect dialect) {
        if (dialect == DBDialect.MYSQL) {
            return wrap(dbName, dialect) + "." + wrap(tableName, dialect);
        }
        return null;
    }

    public static Object wrapData(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return val;
        }
        if (val instanceof CharSequence v) {
            String v1 = v.toString();
            if (v1.isEmpty()) {
                return "''";
            }
            if (!v1.startsWith("'") && !v1.startsWith("\"")) {
                v1 = "'" + v1;
            }
            if (!v1.endsWith("'") && !v1.endsWith("\"")) {
                v1 = v1 + "'";
            }
            return v1;
        }
        if (val instanceof LocalDateTime) {
            return "'" + val + "'";
        }
        return val;
    }

    public static Object unwrapData(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof CharSequence v) {
            String v1 = v.toString();
            if (v1.isEmpty()) {
                return null;
            }
            if (v1.startsWith("'") || v1.startsWith("\"")) {
                v1 = v1.substring(1);
            }
            if (v1.endsWith("'") || v1.endsWith("\"")) {
                v1 = v1.substring(0, v1.length() - 1);
            }
            return v1;
        }
        return val;
    }

    public static void setVal(PreparedStatement statement, Object val, int index) throws SQLException {
        if (val == null) {
            statement.setNull(index, JDBCType.NULL.ordinal());
        } else if (val instanceof Byte x) {
            statement.setByte(index, x);
        } else if (val instanceof Short x) {
            statement.setShort(index, x);
        } else if (val instanceof Integer x) {
            statement.setInt(index, x);
        } else if (val instanceof Long x) {
            statement.setLong(index, x);
        } else if (val instanceof Float x) {
            statement.setFloat(index, x);
        } else if (val instanceof Double x) {
            statement.setDouble(index, x);
        } else if (val instanceof CharSequence x) {
            statement.setString(index, x.toString());
        } else if (val instanceof Date x) {
            statement.setDate(index, x);
        } else if (val instanceof Timestamp x) {
            statement.setTimestamp(index, x);
        } else if (val instanceof java.util.Date x) {
            statement.setDate(index, new Date(x.getTime()));
        } else if (val instanceof LocalDate x) {
            statement.setDate(index, Date.valueOf(x));
        } else if (val instanceof LocalDateTime x) {
            statement.setTimestamp(index, Timestamp.valueOf(x));
        } else if (val instanceof Object x) {
            statement.setObject(index, x);
        }
    }

    public static boolean isSameVal(Object val, Object nVal) {
        if (val == nVal) {
            return true;
        }
        if (Objects.equals(val, nVal)) {
            return true;
        }
        if (val instanceof Number n1 && nVal instanceof Number n2) {
            if (n1.doubleValue() == n2.doubleValue()) {
                return true;
            }
        }
        if (val instanceof byte[] b1 && nVal instanceof byte[] b2) {
            if (StringUtil.equals(new String(b1), new String(b2))) {
                return true;
            }
        }
        return false;
    }

    public static void rollback(Connection connection) {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            throw new ShellException(ex);
        }
    }

    public static int executeUpdate(PreparedStatement statement) throws SQLException {
        int result = statement.executeUpdate();
        statement.close();
        return result;
    }

    public static void close(AutoCloseable o) throws Exception {
        if (o instanceof ResultSet resultSet) {
            resultSet.close();
        } else if (o instanceof Statement statement) {
            statement.close();
        } else if (o instanceof Connection connection) {
            connection.close();
        } else if (o != null) {
            o.close();
        }
    }

    /**
     * 生成索引名称
     *
     * @return 索引名称
     */
    public static String genIndexName() {
        return "index_" + UUIDUtil.uuidSimple().substring(0, 5);
    }

    /**
     * 生成检查名称
     *
     * @return 检查名称
     */
    public static String genCheckName() {
        return "check_" + UUIDUtil.uuidSimple().substring(0, 5);
    }

    /**
     * 生成触发器名称
     *
     * @return 触发器名称
     */
    public static String genTriggerName() {
        return "trigger_" + UUIDUtil.uuidSimple().substring(0, 5);
    }

    /**
     * 生成外键名称
     *
     * @return 外键名称
     */
    public static String genForeignKeyName() {
        return "fk_" + UUIDUtil.uuidSimple().substring(0, 5);
    }

    /**
     * 生成复制名称
     *
     * @return 复制名称
     */
    public static String genCopyName() {
        return "_copy_" + UUIDUtil.uuidSimple().substring(0, 5);
    }

    /**
     * 生成克隆名称
     *
     * @return 复制名称
     */
    public static String genCloneName() {
        return "_clone_" + UUIDUtil.uuidSimple().substring(0, 5);
    }

    /**
     * 转换为插入参数
     *
     * @param columns 字段列表
     * @param record  记录
     */
    public static MysqlInsertRecordParam toInsertRecordParam(MysqlColumns columns, MysqlRecord record) {
        MysqlColumn column = columns.getFirst();
        MysqlRecordData recordData = record.getRecordData();
        MysqlRecordPrimaryKey primaryKey = initPrimaryKey(columns, record);
        MysqlInsertRecordParam insertRecordParam = new MysqlInsertRecordParam();
        insertRecordParam.setRecord(recordData);
        insertRecordParam.setPrimaryKey(primaryKey);
        insertRecordParam.setDbName(column.getDbName());
        insertRecordParam.setTableName(column.getTableName());
        return insertRecordParam;
    }

    /**
     * 初始化主键
     *
     * @param columns 字段列表
     * @param record  记录
     * @return 主键
     */
    public static MysqlRecordPrimaryKey initPrimaryKey(MysqlColumns columns, MysqlRecord record) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        MysqlColumn column = CollectionUtil.getFirst(columns.primaryKeys());
        if (column == null) {
            return null;
        }
        MysqlRecordPrimaryKey pk = new MysqlRecordPrimaryKey();
        pk.init(column, record);
        return pk;
    }

    /**
     * 移除注释
     *
     * @param sql sql
     * @return 结果
     */
    public static String removeComment(String sql) {
        StringBuilder builder = new StringBuilder();
        AtomicBoolean commentFlag = new AtomicBoolean(false);
        sql.lines().forEach(line -> {
            // 单行注释1
            if (line.stripLeading().startsWith("-- ")) {
                return;
            }
            // 单行注释2
            if (line.stripLeading().startsWith("#")) {
                return;
            }
            // 多行注释开始
            if (line.stripLeading().startsWith("/*")) {
                commentFlag.set(true);
            }
            // 多行注释结束
            if (line.stripTrailing().endsWith("*/")) {
                commentFlag.set(false);
                return;
            }
            // 正常行
            if (!commentFlag.get() && StringUtil.isNotBlank(line)) {
                builder.append(line).append("\n");
            }
        });
        return builder.toString();
    }

    /**
     * 是否查询全部字段
     *
     * @param sql    sql
     * @param dbType 数据库类型
     * @return 结果
     */
    public static boolean isFullColumn(String sql, DbType dbType) {
        sql = removeComment(sql);
        // druid无法解析这些语句，直接返回
        if (StringUtil.startWithAnyIgnoreCase(sql,
                "SHOW VARIABLES LIKE",
                "SHOW CREATE EVENT"
        )) {
            return false;
        }
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, dbType, SQLParserFeature.SkipComments);
        SQLStatement statement = sqlStatements.getFirst();
        SchemaStatVisitor visitor = new SchemaStatVisitor(dbType);
        statement.accept(visitor);
        Collection<TableStat.Column> columns = visitor.getColumns();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (TableStat.Column column : columns) {
                if (StringUtil.equals("*", column.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
