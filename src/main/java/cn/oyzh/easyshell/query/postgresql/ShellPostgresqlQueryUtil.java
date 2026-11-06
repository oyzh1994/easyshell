package cn.oyzh.easyshell.query.postgresql;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PostgreSQL查询工具类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlQueryUtil {

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
     * 模式（PostgreSQL中dbName实际为schema）
     */
    private static final List<PostgresqlSchema> DB_SCHEMAS = new ArrayList<>();

    /**
     * 表
     */
    private static final List<PostgresqlTable> DB_TABLES = new CopyOnWriteArrayList<>();

    /**
     * 视图
     */
    private static final List<PostgresqlView> DB_VIEWS = new CopyOnWriteArrayList<>();

    /**
     * 函数
     */
    private static final List<PostgresqlFunction> DB_FUNCTIONS = new CopyOnWriteArrayList<>();

    /**
     * 过程
     */
    private static final List<PostgresqlProcedure> DB_PROCEDURES = new CopyOnWriteArrayList<>();

    /**
     * 字段
     */
    private static final List<PostgresqlColumn> DB_COLUMNS = new CopyOnWriteArrayList<>();

    static {
        // dml
        DB_KEYWORDS.add("SELECT");
        DB_KEYWORDS.add("INSERT INTO");
        DB_KEYWORDS.add("UPDATE");
        DB_KEYWORDS.add("DELETE");
        DB_KEYWORDS.add("MERGE INTO");
        DB_KEYWORDS.add("FROM");
        DB_KEYWORDS.add("WHERE");
        DB_KEYWORDS.add("RETURNING");
        DB_KEYWORDS.add("VALUES");
        DB_KEYWORDS.add("DEFAULT VALUES");
        DB_KEYWORDS.add("ON CONFLICT");
        DB_KEYWORDS.add("DO NOTHING");
        DB_KEYWORDS.add("DO UPDATE SET");

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
        DB_KEYWORDS.add("CREATE MATERIALIZED VIEW");
        DB_KEYWORDS.add("REFRESH MATERIALIZED VIEW");
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
        DB_KEYWORDS.add("CREATE EXTENSION");
        DB_KEYWORDS.add("CREATE ROLE");
        DB_KEYWORDS.add("CREATE USER");
        DB_KEYWORDS.add("GRANT");
        DB_KEYWORDS.add("REVOKE");
        DB_KEYWORDS.add("COMMENT ON");
        DB_KEYWORDS.add("VACUUM");
        DB_KEYWORDS.add("ANALYZE");
        DB_KEYWORDS.add("EXPLAIN");
        DB_KEYWORDS.add("EXPLAIN ANALYZE");
        DB_KEYWORDS.add("COPY");
        DB_KEYWORDS.add("SHOW");
        DB_KEYWORDS.add("SET");
        DB_KEYWORDS.add("RESET");
        DB_KEYWORDS.add("BEGIN");
        DB_KEYWORDS.add("START TRANSACTION");
        DB_KEYWORDS.add("COMMIT");
        DB_KEYWORDS.add("ROLLBACK");
        DB_KEYWORDS.add("SAVEPOINT");
        DB_KEYWORDS.add("RELEASE SAVEPOINT");
        DB_KEYWORDS.add("DECLARE");
        DB_KEYWORDS.add("FETCH");
        DB_KEYWORDS.add("CLOSE");
        DB_KEYWORDS.add("CALL");
        DB_KEYWORDS.add("DO");
        DB_KEYWORDS.add("LISTEN");
        DB_KEYWORDS.add("NOTIFY");
        DB_KEYWORDS.add("UNLISTEN");
        DB_KEYWORDS.add("LOCK TABLE");

        // query
        DB_KEYWORDS.add("AS");
        DB_KEYWORDS.add("DISTINCT");
        DB_KEYWORDS.add("DISTINCT ON");
        DB_KEYWORDS.add("LIKE");
        DB_KEYWORDS.add("ILIKE");
        DB_KEYWORDS.add("SIMILAR TO");
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
        DB_KEYWORDS.add("LIMIT");
        DB_KEYWORDS.add("OFFSET");
        DB_KEYWORDS.add("FETCH FIRST");
        DB_KEYWORDS.add("HAVING");
        DB_KEYWORDS.add("ON");
        DB_KEYWORDS.add("USING");
        DB_KEYWORDS.add("JOIN");
        DB_KEYWORDS.add("LEFT JOIN");
        DB_KEYWORDS.add("RIGHT JOIN");
        DB_KEYWORDS.add("CROSS JOIN");
        DB_KEYWORDS.add("FULL JOIN");
        DB_KEYWORDS.add("INNER JOIN");
        DB_KEYWORDS.add("NATURAL JOIN");
        DB_KEYWORDS.add("LATERAL");
        DB_KEYWORDS.add("WITH");
        DB_KEYWORDS.add("RECURSIVE");
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
        DB_KEYWORDS.add("WINDOW");
        DB_KEYWORDS.add("FILTER");
        DB_KEYWORDS.add("FOR UPDATE");
        DB_KEYWORDS.add("FOR SHARE");
        DB_KEYWORDS.add("NULLS FIRST");
        DB_KEYWORDS.add("NULLS LAST");
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
        DB_KEYWORDS.add("LENGTH");
        DB_KEYWORDS.add("SUBSTRING");
        DB_KEYWORDS.add("UPPER");
        DB_KEYWORDS.add("LOWER");
        DB_KEYWORDS.add("TRIM");
        DB_KEYWORDS.add("ROUND");
        DB_KEYWORDS.add("NOW");
        DB_KEYWORDS.add("CURRENT_DATE");
        DB_KEYWORDS.add("CURRENT_TIME");
        DB_KEYWORDS.add("CURRENT_TIMESTAMP");
        DB_KEYWORDS.add("EXTRACT");
        DB_KEYWORDS.add("DATE_TRUNC");
        DB_KEYWORDS.add("TO_CHAR");
        DB_KEYWORDS.add("TO_DATE");
        DB_KEYWORDS.add("TO_TIMESTAMP");
        DB_KEYWORDS.add("GENERATE_SERIES");
        DB_KEYWORDS.add("ARRAY_AGG");
        DB_KEYWORDS.add("STRING_AGG");
        DB_KEYWORDS.add("JSONB_BUILD_OBJECT");
        DB_KEYWORDS.add("JSONB_AGG");
        DB_KEYWORDS.add("ROW_NUMBER");
        DB_KEYWORDS.add("RANK");
        DB_KEYWORDS.add("DENSE_RANK");
        DB_KEYWORDS.add("LAG");
        DB_KEYWORDS.add("LEAD");

        // 类型
        DB_KEYWORDS.add("INTEGER");
        DB_KEYWORDS.add("BIGINT");
        DB_KEYWORDS.add("SMALLINT");
        DB_KEYWORDS.add("SERIAL");
        DB_KEYWORDS.add("BIGSERIAL");
        DB_KEYWORDS.add("NUMERIC");
        DB_KEYWORDS.add("DECIMAL");
        DB_KEYWORDS.add("REAL");
        DB_KEYWORDS.add("DOUBLE PRECISION");
        DB_KEYWORDS.add("TEXT");
        DB_KEYWORDS.add("VARCHAR");
        DB_KEYWORDS.add("CHAR");
        DB_KEYWORDS.add("BOOLEAN");
        DB_KEYWORDS.add("DATE");
        DB_KEYWORDS.add("TIMESTAMP");
        DB_KEYWORDS.add("TIMESTAMPTZ");
        DB_KEYWORDS.add("TIME");
        DB_KEYWORDS.add("INTERVAL");
        DB_KEYWORDS.add("UUID");
        DB_KEYWORDS.add("JSON");
        DB_KEYWORDS.add("JSONB");
        DB_KEYWORDS.add("BYTEA");
        DB_KEYWORDS.add("ARRAY");

        // 约束
        DB_KEYWORDS.add("PRIMARY KEY");
        DB_KEYWORDS.add("FOREIGN KEY");
        DB_KEYWORDS.add("REFERENCES");
        DB_KEYWORDS.add("UNIQUE");
        DB_KEYWORDS.add("CHECK");
        DB_KEYWORDS.add("NOT NULL");
        DB_KEYWORDS.add("DEFAULT");
        DB_KEYWORDS.add("GENERATED ALWAYS AS IDENTITY");
        DB_KEYWORDS.add("CASCADE");
        DB_KEYWORDS.add("RESTRICT");
        DB_KEYWORDS.add("IF EXISTS");
        DB_KEYWORDS.add("IF NOT EXISTS");

        // 通用关键字，排除MySQL/MariaDB专用关键字
        for (String keyword : DBUtil.SQL_KEYWORDS) {
            if (!StringUtil.equalsAnyIgnoreCase(keyword, "AUTO_INCREMENT", "ENUM", "DATETIME")) {
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
    public static List<PostgresqlSchema> getSchemas() {
        return DB_SCHEMAS;
    }

    /**
     * 获取表列表
     *
     * @return 表列表
     */
    public static List<PostgresqlTable> getTables() {
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
    public static List<PostgresqlView> getViews() {
        return DB_VIEWS;
    }

    /**
     * 获取函数列表
     *
     * @return 函数列表
     */
    public static List<PostgresqlFunction> getFunctions() {
        return DB_FUNCTIONS;
    }

    /**
     * 获取存储过程列表
     *
     * @return 存储过程列表
     */
    public static List<PostgresqlProcedure> getProcedures() {
        return DB_PROCEDURES;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public static List<PostgresqlColumn> getColumns() {
        return DB_COLUMNS;
    }

    /**
     * 是否系统模式
     * <p>
     * PostgreSQL的系统信息保存在pg_catalog、information_schema等模式中，
     * 这些模式下的对象不参与提示。
     *
     * @param schema 模式名称
     * @return 结果
     */
    private static boolean isSystemSchema(String schema) {
        if (schema == null) {
            return true;
        }
        return StringUtil.equalsAnyIgnoreCase(schema, "pg_catalog", "information_schema", "pg_toast")
                || StringUtil.startWithIgnoreCase(schema, "pg_temp_")
                || StringUtil.startWithIgnoreCase(schema, "pg_toast_temp_");
    }

    /**
     * 更新索引
     *
     * @param client 客户端
     */
    public static void updateIndex(ShellPostgresqlClient client) {
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
                    List<PostgresqlSchema> schemas = client.selectSchemas();
                    DB_SCHEMAS.addAll(schemas);
                    List<Runnable> tasks = new ArrayList<>();
                    // 更新表索引
                    for (PostgresqlSchema schema : DB_SCHEMAS) {
                        if (!isSystemSchema(schema.getName())) {
                            tasks.add(() -> {
                                List<PostgresqlTable> tables = client.selectTablesSimple(schema.getName());
                                DB_TABLES.addAll(tables);
                            });
                        }
                    }
                    // 更新视图索引
                    for (PostgresqlSchema schema : DB_SCHEMAS) {
                        if (!isSystemSchema(schema.getName())) {
                            tasks.add(() -> {
                                List<PostgresqlView> views = client.selectViewsSimple(schema.getName());
                                DB_VIEWS.addAll(views);
                            });
                        }
                    }
                    // 更新函数索引
                    for (PostgresqlSchema schema : DB_SCHEMAS) {
                        if (!isSystemSchema(schema.getName())) {
                            tasks.add(() -> {
                                List<PostgresqlFunction> functions = client.selectFunctionsSimple(schema.getName());
                                DB_FUNCTIONS.addAll(functions);
                            });
                        }
                    }
                    // 更新过程索引
                    for (PostgresqlSchema schema : DB_SCHEMAS) {
                        if (!isSystemSchema(schema.getName())) {
                            tasks.add(() -> {
                                List<PostgresqlProcedure> procedures = client.selectProceduresSimple(schema.getName());
                                DB_PROCEDURES.addAll(procedures);
                            });
                        }
                    }
                    // 异步执行
                    ThreadUtil.submit(tasks);
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
