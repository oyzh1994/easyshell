package cn.oyzh.easyshell.query.oracle;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.util.oracle.ShellOracleUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Oracle查询工具类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleQueryUtil {

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
     * 数据库
     */
    private static final List<OracleSchema> DB_SCHEMAS = new ArrayList<>();

    /**
     * 表
     */
    private static final List<OracleTable> DB_TABLES = new CopyOnWriteArrayList<>();

    /**
     * 视图
     */
    private static final List<OracleView> DB_VIEWS = new CopyOnWriteArrayList<>();

    /**
     * 函数
     */
    private static final List<OracleFunction> DB_FUNCTIONS = new CopyOnWriteArrayList<>();

    /**
     * 过程
     */
    private static final List<OracleProcedure> DB_PROCEDURES = new CopyOnWriteArrayList<>();

    /**
     * 字段
     */
    private static final List<OracleColumn> DB_COLUMNS = new CopyOnWriteArrayList<>();

    static {
        // dml
        DB_KEYWORDS.add("SELECT");
        DB_KEYWORDS.add("UPDATE");
        DB_KEYWORDS.add("DELETE");
        DB_KEYWORDS.add("FROM");
        DB_KEYWORDS.add("WHERE");
        DB_KEYWORDS.add("WHEN");
        DB_KEYWORDS.add("INSERT INTO");

        // ddl
        DB_KEYWORDS.add("ALTER TABLE");
        DB_KEYWORDS.add("CREATE TABLE");
        DB_KEYWORDS.add("DROP TABLE");
        DB_KEYWORDS.add("CREATE VIEW");
        DB_KEYWORDS.add("CREATE SEQUENCE");
        DB_KEYWORDS.add("CREATE TRIGGER");
        DB_KEYWORDS.add("CREATE PROCEDURE");
        DB_KEYWORDS.add("CREATE FUNCTION");
        DB_KEYWORDS.add("TABLESPACE");
        DB_KEYWORDS.add("COLUMN");
        DB_KEYWORDS.add("COMMENT");

        // query
        DB_KEYWORDS.add("AS");
        DB_KEYWORDS.add("LIKE");
        DB_KEYWORDS.add("IN");
        DB_KEYWORDS.add("BETWEEN");
        DB_KEYWORDS.add("AND");
        DB_KEYWORDS.add("OR");
        DB_KEYWORDS.add("NOT");
        DB_KEYWORDS.add("NULL");
        DB_KEYWORDS.add("IS");
        DB_KEYWORDS.add("CASE");
        DB_KEYWORDS.add("THEN");
        DB_KEYWORDS.add("ELSE");
        DB_KEYWORDS.add("END");
        DB_KEYWORDS.add("GROUP BY");
        DB_KEYWORDS.add("ORDER BY");
        DB_KEYWORDS.add("OFFSET");
        DB_KEYWORDS.add("FETCH");
        DB_KEYWORDS.add("ROWNUM");
        DB_KEYWORDS.add("CONNECT BY");
        DB_KEYWORDS.add("START WITH");
        DB_KEYWORDS.add("PRIOR");
        DB_KEYWORDS.add("HAVING");
        DB_KEYWORDS.add("ON");
        DB_KEYWORDS.add("JOIN");
        DB_KEYWORDS.add("LEFT JOIN");
        DB_KEYWORDS.add("RIGHT JOIN");
        DB_KEYWORDS.add("CROSS JOIN");
        DB_KEYWORDS.add("FULL JOIN");
        DB_KEYWORDS.add("INNER JOIN");
        DB_KEYWORDS.add("INTERSECT");
        DB_KEYWORDS.add("UNION");
        DB_KEYWORDS.add("UNION ALL");
        DB_KEYWORDS.add("MINUS");
        DB_KEYWORDS.add("COUNT");
        DB_KEYWORDS.add("SUM");
        DB_KEYWORDS.add("MAX");
        DB_KEYWORDS.add("MIN");
        DB_KEYWORDS.add("AVG");
        DB_KEYWORDS.add("DISTINCT");
        DB_KEYWORDS.add("ASC");
        DB_KEYWORDS.add("DESC");
        DB_KEYWORDS.add("EXISTS");
        DB_KEYWORDS.add("ANY");
        DB_KEYWORDS.add("ALL");

        // 函数
        DB_KEYWORDS.add("LENGTH");
        DB_KEYWORDS.add("SUBSTR");
        DB_KEYWORDS.add("UPPER");
        DB_KEYWORDS.add("LOWER");
        DB_KEYWORDS.add("TRIM");
        DB_KEYWORDS.add("ROUND");

        DB_KEYWORDS.addAll(List.of(DBUtil.SQL_KEYWORDS));
        DB_KEYWORDS.remove("LIMIT");
        DB_KEYWORDS.remove("EXCEPT");
        DB_KEYWORDS.remove("DATABASE");
        DB_KEYWORDS.remove("DATABASES");
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
    public static List<OracleSchema> getSchemas() {
        return DB_SCHEMAS;
    }

    /**
     * 获取表列表
     *
     * @return 表列表
     */
    public static List<OracleTable> getTables() {
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
    public static List<OracleView> getViews() {
        return DB_VIEWS;
    }

    /**
     * 获取函数列表
     *
     * @return 函数列表
     */
    public static List<OracleFunction> getFunctions() {
        return DB_FUNCTIONS;
    }

    /**
     * 获取存储过程列表
     *
     * @return 存储过程列表
     */
    public static List<OracleProcedure> getProcedures() {
        return DB_PROCEDURES;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public static List<OracleColumn> getColumns() {
        return DB_COLUMNS;
    }

    /**
     * 更新索引
     *
     * @param client 客户端
     */
    public static void updateIndex(ShellOracleClient client) {
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
                    // 更新库索引
                    List<OracleSchema> databases = client.selectSchemas();
                    DB_SCHEMAS.addAll(databases);
                    List<Runnable> tasks = new ArrayList<>();
                    // 更新表索引
                    for (OracleSchema schema : DB_SCHEMAS) {
                        if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                            tasks.add(() -> {
                                List<OracleTable> tables = client.selectTablesSimple(schema.getName());
                                DB_TABLES.addAll(tables);
                            });
                        }
                    }
                    // 更新视图索引
                    for (OracleSchema schema : DB_SCHEMAS) {
                        if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                            tasks.add(() -> {
                                List<OracleView> views = client.selectViewsSimple(schema.getName());
                                DB_VIEWS.addAll(views);
                            });
                        }
                    }
                    // 更新函数索引
                    for (OracleSchema schema : DB_SCHEMAS) {
                        if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                            tasks.add(() -> {
                                List<OracleFunction> functions = client.selectFunctionsSimple(schema.getName());
                                DB_FUNCTIONS.addAll(functions);
                            });
                        }
                    }
                    // 更新过程索引
                    for (OracleSchema schema : DB_SCHEMAS) {
                        if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                            tasks.add(() -> {
                                List<OracleProcedure> procedures = client.selectProceduresSimple(schema.getName());
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
