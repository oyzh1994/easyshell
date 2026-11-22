package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SqlServer查询工具类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerQueryUtil {

    /**
     * 0未初始化
     * 1 初始化中
     * 2 已初始化
     */
    private static int indexStatus = 0;

    /**
     * 关键字
     */
    private static final Set<String> DB_KEYWORDS = new HashSet<>();

    /**
     * 模式（SqlServer中dbName实际为schema）
     */
    private static final List<SqlServerSchema> DB_SCHEMAS = new ArrayList<>();

    /**
     * 表
     */
    private static final List<SqlServerTable> DB_TABLES = new CopyOnWriteArrayList<>();

    /**
     * 视图
     */
    private static final List<SqlServerView> DB_VIEWS = new CopyOnWriteArrayList<>();

    /**
     * 函数
     */
    private static final List<SqlServerFunction> DB_FUNCTIONS = new CopyOnWriteArrayList<>();

    /**
     * 过程
     */
    private static final List<SqlServerProcedure> DB_PROCEDURES = new CopyOnWriteArrayList<>();

    /**
     * 字段
     */
    private static final List<SqlServerColumn> DB_COLUMNS = new CopyOnWriteArrayList<>();

    static {
        // dml
        DB_KEYWORDS.add("SELECT");
        DB_KEYWORDS.add("INSERT INTO");
        DB_KEYWORDS.add("UPDATE");
        DB_KEYWORDS.add("DELETE");
        DB_KEYWORDS.add("MERGE INTO");
        DB_KEYWORDS.add("FROM");
        DB_KEYWORDS.add("WHERE");
        DB_KEYWORDS.add("VALUES");
        DB_KEYWORDS.add("DEFAULT VALUES");

        // ddl
        DB_KEYWORDS.add("CREATE");
        DB_KEYWORDS.add("ALTER");
        DB_KEYWORDS.add("DROP");
        DB_KEYWORDS.add("TRUNCATE");
        DB_KEYWORDS.add("CREATE TABLE");
        DB_KEYWORDS.add("ALTER TABLE");
        DB_KEYWORDS.add("DROP TABLE");
        DB_KEYWORDS.add("TRUNCATE TABLE");
        DB_KEYWORDS.add("CREATE INDEX");
        DB_KEYWORDS.add("DROP INDEX");
        DB_KEYWORDS.add("CREATE VIEW");
        DB_KEYWORDS.add("ALTER VIEW");
        DB_KEYWORDS.add("DROP VIEW");
        DB_KEYWORDS.add("CREATE OR ALTER");
        DB_KEYWORDS.add("CREATE SCHEMA");
        DB_KEYWORDS.add("DROP SCHEMA");
        DB_KEYWORDS.add("CREATE DATABASE");
        DB_KEYWORDS.add("DROP DATABASE");
        DB_KEYWORDS.add("CREATE SEQUENCE");
        DB_KEYWORDS.add("ALTER SEQUENCE");
        DB_KEYWORDS.add("DROP SEQUENCE");
        DB_KEYWORDS.add("CREATE FUNCTION");
        DB_KEYWORDS.add("DROP FUNCTION");
        DB_KEYWORDS.add("CREATE PROCEDURE");
        DB_KEYWORDS.add("DROP PROCEDURE");
        DB_KEYWORDS.add("CREATE TRIGGER");
        DB_KEYWORDS.add("DROP TRIGGER");
        DB_KEYWORDS.add("CREATE ROLE");
        DB_KEYWORDS.add("CREATE USER");
        DB_KEYWORDS.add("GRANT");
        DB_KEYWORDS.add("REVOKE");
        DB_KEYWORDS.add("EXPLAIN");
        DB_KEYWORDS.add("SET");
        DB_KEYWORDS.add("BEGIN");
        DB_KEYWORDS.add("COMMIT");
        DB_KEYWORDS.add("ROLLBACK");
        DB_KEYWORDS.add("DECLARE");
        DB_KEYWORDS.add("FETCH");
        DB_KEYWORDS.add("CLOSE");
        DB_KEYWORDS.add("EXEC");
        DB_KEYWORDS.add("GO");

        // query
        DB_KEYWORDS.add("AS");
        DB_KEYWORDS.add("DISTINCT");
        DB_KEYWORDS.add("TOP");
        DB_KEYWORDS.add("LIKE");
        DB_KEYWORDS.add("IN");
        DB_KEYWORDS.add("BETWEEN");
        DB_KEYWORDS.add("AND");
        DB_KEYWORDS.add("OR");
        DB_KEYWORDS.add("NOT");
        DB_KEYWORDS.add("NULL");
        DB_KEYWORDS.add("IS");
        DB_KEYWORDS.add("IS NULL");
        DB_KEYWORDS.add("IS NOT NULL");
        DB_KEYWORDS.add("CASE");
        DB_KEYWORDS.add("WHEN");
        DB_KEYWORDS.add("THEN");
        DB_KEYWORDS.add("ELSE");
        DB_KEYWORDS.add("END");
        DB_KEYWORDS.add("GROUP BY");
        DB_KEYWORDS.add("ORDER BY");
        DB_KEYWORDS.add("OFFSET");
        DB_KEYWORDS.add("FETCH NEXT");
        DB_KEYWORDS.add("HAVING");
        DB_KEYWORDS.add("ON");
        DB_KEYWORDS.add("USING");
        DB_KEYWORDS.add("JOIN");
        DB_KEYWORDS.add("LEFT JOIN");
        DB_KEYWORDS.add("RIGHT JOIN");
        DB_KEYWORDS.add("CROSS JOIN");
        DB_KEYWORDS.add("FULL JOIN");
        DB_KEYWORDS.add("INNER JOIN");
        DB_KEYWORDS.add("CROSS APPLY");
        DB_KEYWORDS.add("OUTER APPLY");
        DB_KEYWORDS.add("PIVOT");
        DB_KEYWORDS.add("UNPIVOT");
        DB_KEYWORDS.add("WITH");
        DB_KEYWORDS.add("UNION");
        DB_KEYWORDS.add("UNION ALL");
        DB_KEYWORDS.add("INTERSECT");
        DB_KEYWORDS.add("EXCEPT");
        DB_KEYWORDS.add("EXISTS");
        DB_KEYWORDS.add("ANY");
        DB_KEYWORDS.add("ALL");
        DB_KEYWORDS.add("SOME");
        DB_KEYWORDS.add("OVER");
        DB_KEYWORDS.add("PARTITION BY");
        DB_KEYWORDS.add("ASC");
        DB_KEYWORDS.add("DESC");

        // 函数
        DB_KEYWORDS.add("COUNT");
        DB_KEYWORDS.add("SUM");
        DB_KEYWORDS.add("MAX");
        DB_KEYWORDS.add("MIN");
        DB_KEYWORDS.add("AVG");
        DB_KEYWORDS.add("COALESCE");
        DB_KEYWORDS.add("NULLIF");
        DB_KEYWORDS.add("GREATEST");
        DB_KEYWORDS.add("LEAST");
        DB_KEYWORDS.add("CONCAT");
        DB_KEYWORDS.add("LEN");
        DB_KEYWORDS.add("SUBSTRING");
        DB_KEYWORDS.add("UPPER");
        DB_KEYWORDS.add("LOWER");
        DB_KEYWORDS.add("TRIM");
        DB_KEYWORDS.add("ROUND");
        DB_KEYWORDS.add("CHARINDEX");
        DB_KEYWORDS.add("PATINDEX");
        DB_KEYWORDS.add("IIF");
        DB_KEYWORDS.add("ISNULL");
        DB_KEYWORDS.add("CONVERT");
        DB_KEYWORDS.add("TRY_CONVERT");
        DB_KEYWORDS.add("CAST");
        DB_KEYWORDS.add("GETDATE");
        DB_KEYWORDS.add("SYSDATETIME");
        DB_KEYWORDS.add("SYSUTCDATETIME");
        DB_KEYWORDS.add("CURRENT_DATE");
        DB_KEYWORDS.add("CURRENT_TIME");
        DB_KEYWORDS.add("CURRENT_TIMESTAMP");
        DB_KEYWORDS.add("DATEADD");
        DB_KEYWORDS.add("DATEDIFF");
        DB_KEYWORDS.add("FORMAT");
        DB_KEYWORDS.add("STRING_AGG");
        DB_KEYWORDS.add("JSON_VALUE");
        DB_KEYWORDS.add("OPENJSON");
        DB_KEYWORDS.add("ROW_NUMBER");
        DB_KEYWORDS.add("RANK");
        DB_KEYWORDS.add("DENSE_RANK");
        DB_KEYWORDS.add("LAG");
        DB_KEYWORDS.add("LEAD");

        // 类型
        DB_KEYWORDS.add("INTEGER");
        DB_KEYWORDS.add("BIGINT");
        DB_KEYWORDS.add("SMALLINT");
        DB_KEYWORDS.add("TINYINT");
        DB_KEYWORDS.add("BIT");
        DB_KEYWORDS.add("NUMERIC");
        DB_KEYWORDS.add("DECIMAL");
        DB_KEYWORDS.add("REAL");
        DB_KEYWORDS.add("TEXT");
        DB_KEYWORDS.add("VARCHAR");
        DB_KEYWORDS.add("CHAR");
        DB_KEYWORDS.add("DATE");
        DB_KEYWORDS.add("TIMESTAMP");
        DB_KEYWORDS.add("DATETIME");
        DB_KEYWORDS.add("DATETIME2");
        DB_KEYWORDS.add("DATETIMEOFFSET");
        DB_KEYWORDS.add("SMALLDATETIME");
        DB_KEYWORDS.add("TIME");
        DB_KEYWORDS.add("NVARCHAR");
        DB_KEYWORDS.add("NCHAR");
        DB_KEYWORDS.add("VARBINARY");
        DB_KEYWORDS.add("BINARY");
        DB_KEYWORDS.add("UNIQUEIDENTIFIER");
        DB_KEYWORDS.add("XML");
        DB_KEYWORDS.add("SQL_VARIANT");
        DB_KEYWORDS.add("MONEY");
        DB_KEYWORDS.add("SMALLMONEY");

        // 约束
        DB_KEYWORDS.add("PRIMARY KEY");
        DB_KEYWORDS.add("FOREIGN KEY");
        DB_KEYWORDS.add("REFERENCES");
        DB_KEYWORDS.add("UNIQUE");
        DB_KEYWORDS.add("CHECK");
        DB_KEYWORDS.add("NOT NULL");
        DB_KEYWORDS.add("DEFAULT");
        DB_KEYWORDS.add("IDENTITY");
        DB_KEYWORDS.add("ON DELETE CASCADE");
        DB_KEYWORDS.add("ON UPDATE CASCADE");
        DB_KEYWORDS.add("IF EXISTS");
        DB_KEYWORDS.add("IF NOT EXISTS");

        // 通用关键字，排除MySQL/MariaDB专用关键字
        for (String keyword : DBUtil.SQL_KEYWORDS) {
            if (!StringUtil.equalsAnyIgnoreCase(keyword, "AUTO_INCREMENT", "ENUM")) {
                DB_KEYWORDS.add(keyword);
            }
        }
    }

    /**
     * 获取关键字列表
     *
     * @return 关键字列表
     */
    public static Set<String> getKeywords() {
        return DB_KEYWORDS;
    }

    /**
     * 获取模式列表
     *
     * @return 模式列表
     */
    public static List<SqlServerSchema> getSchemas() {
        return DB_SCHEMAS;
    }

    /**
     * 获取表列表
     *
     * @return 表列表
     */
    public static List<SqlServerTable> getTables() {
        return DB_TABLES;
    }

    /**
     * 获取关键字列表
     *
     * @return 结果
     */
    public static Set<String> getDbKeywords() {
        Set<String> list = new HashSet<>(getKeywords());
//        for (DBName name : DB_SCHEMAS) {
//            list.add(name.getName());
//        }
//        for (DBName name : DB_TABLES) {
//            list.add(name.getName());
//        }
//        for (DBName name : DB_VIEWS) {
//            list.add(name.getName());
//        }
//        for (DBName name : DB_FUNCTIONS) {
//            list.add(name.getName());
//        }
//        for (DBName name : DB_PROCEDURES) {
//            list.add(name.getName());
//        }
        return list;
    }

    /**
     * 获取视图列表
     *
     * @return 视图列表
     */
    public static List<SqlServerView> getViews() {
        return DB_VIEWS;
    }

    /**
     * 获取函数列表
     *
     * @return 函数列表
     */
    public static List<SqlServerFunction> getFunctions() {
        return DB_FUNCTIONS;
    }

    /**
     * 获取存储过程列表
     *
     * @return 存储过程列表
     */
    public static List<SqlServerProcedure> getProcedures() {
        return DB_PROCEDURES;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public static List<SqlServerColumn> getColumns() {
        return DB_COLUMNS;
    }

    /**
     * 更新索引
     *
     * @param client 客户端
     */
    public static void updateIndex(ShellSqlServerClient client) {
        Runnable task = () -> {
            if (indexStatus == 0) {
                try {
                    indexStatus = 1;
                    DB_VIEWS.clear();
                    DB_TABLES.clear();
                    DB_COLUMNS.clear();
                    DB_FUNCTIONS.clear();
                    DB_PROCEDURES.clear();
                    DB_SCHEMAS.clear();
                    // 更新模式索引
                    List<SqlServerSchema> schemas = client.selectSchemas();
                    DB_SCHEMAS.addAll(schemas);
                    // 一次批量查询全部对象，避免按模式并发访问同一JDBC连接。
                    for (SqlServerQueryObject object : client.selectQueryObjects()) {
                        switch (object.type()) {
                            case "U" -> {
                                SqlServerTable table = new SqlServerTable();
                                table.setSchema(object.schema());
                                table.setName(object.name());
                                DB_TABLES.add(table);
                            }
                            case "V" -> {
                                SqlServerView view = new SqlServerView();
                                view.setSchema(object.schema());
                                view.setName(object.name());
                                DB_VIEWS.add(view);
                            }
                            case "FN", "IF", "TF", "FS", "FT", "AF" -> {
                                SqlServerFunction function = new SqlServerFunction();
                                function.setSchema(object.schema());
                                function.setName(object.name());
                                DB_FUNCTIONS.add(function);
                            }
                            case "P" -> {
                                SqlServerProcedure procedure = new SqlServerProcedure();
                                procedure.setSchema(object.schema());
                                procedure.setName(object.name());
                                DB_PROCEDURES.add(procedure);
                            }
                            default -> {
                            }
                        }
                    }
                    indexStatus = 2;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    indexStatus = 0;
                }
            }
        };
        ThreadUtil.start(task);
    }
}
