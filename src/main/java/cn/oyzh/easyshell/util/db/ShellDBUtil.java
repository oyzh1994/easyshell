package cn.oyzh.easyshell.util.db;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontUtil;

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
import java.util.Objects;

/**
 * db工具类
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class ShellDBUtil {

    /**
     * 是否开启打印元数据功能
     */
    public static boolean ENABLE_PRINT_METADATA = false;

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
    }

    /**
     * 设置值
     *
     * @param val   值
     * @param index 索引
     */
    public static void setVal(PreparedStatement statement, Object val, int index) throws SQLException {
        if (val == null) {
            statement.setNull(index, JDBCType.NULL.ordinal());
        } else if (val instanceof byte[] x) {
            statement.setBytes(index, x);
        } else if (val instanceof Boolean x) {
            statement.setBoolean(index, x);
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

    /**
     * 是否相同值
     *
     * @param val  值
     * @param nVal 新值
     */
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

    /**
     * 回滚
     *
     * @param connection 连接
     */
    public static void rollback(Connection connection) {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 执行更改
     *
     * @param statement 语句
     * @return 结果
     * @throws SQLException 异常
     */
    public static int executeUpdate(PreparedStatement statement) throws SQLException {
        int result = statement.executeUpdate();
        statement.close();
        return result;
    }

//    /**
//     * 关闭
//     *
//     * @param o 对象
//     * @throws SQLException 异常
//     */
//    public static void close(AutoCloseable o) throws Exception {
//        if (o instanceof ResultSet resultSet) {
//            resultSet.close();
//        } else if (o instanceof Statement statement) {
//            statement.close();
//        } else if (o instanceof Connection connection) {
//            connection.close();
//        } else if (o != null) {
//            o.close();
//        }
//    }

    /**
     * 包装
     *
     * @param name    名称
     * @param dialect 方言
     * @return 结果
     */
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

    /**
     * 包装
     *
     * @param dbName    库名称
     * @param tableName 表名称
     * @param dialect   方言
     * @return 结果
     */
    public static String wrap(String dbName, String tableName, DBDialect dialect) {
        if (dialect == DBDialect.MYSQL) {
            return wrap(dbName, dialect) + "." + wrap(tableName, dialect);
        }
        return null;
    }

    /**
     * 包装数据
     *
     * @param val     数据
     * @param dialect 方言
     * @return 结果
     */
    public static Object wrapData(Object val, DBDialect dialect) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return val;
        }
        if (dialect == DBDialect.MYSQL) {
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
        }

        return val;
    }

    /**
     * 取消包装数据
     *
     * @param val     数据
     * @param dialect 方言
     * @return 结果
     */
    public static Object unwrapData(Object val, DBDialect dialect) {
        if (val == null) {
            return null;
        }
        if (dialect == DBDialect.MYSQL) {
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
        }
        return val;
    }

    /**
     * 计算合适的字段宽
     *
     * @param column 字段
     * @return 结果
     */
    public static double suitableColumnWidth(DBColumn column) {
        String str1 = column.getName();
        String str2 = column.getType();
        if (column.supportSize() && column.getSize() != null) {
            str2 = column.getType() + "(" + column.getSize() + ")";
        }
        double w1 = FontUtil.textWidth(str1, FontManager.currentFont());
        double w2 = FontUtil.textWidth(str2, FontManager.currentFont());
        double w3 = Math.max(w1, w2);
        return w3 + 50;
    }

    /**
     * null背景内容
     *
     * @return 结果
     */
    public static String nullPromptText() {
        return "(Null)";
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

}
