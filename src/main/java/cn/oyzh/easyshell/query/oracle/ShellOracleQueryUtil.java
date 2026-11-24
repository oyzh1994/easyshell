package cn.oyzh.easyshell.query.oracle;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.util.oracle.ShellOracleUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.oyzh.easyshell.util.ShellClientUtil.isAvailable;

/**
 * Oracle查询工具类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleQueryUtil {

    /**
     * 关键字
     */
    private static final Set<String> DB_KEYWORDS = new HashSet<>();

    /**
     * 数据库
     */
    private static final List<OracleSchema> DB_SCHEMAS = new CopyOnWriteArrayList<>();

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
     * 序列
     */
    private static final List<OracleSequence> DB_SEQUENCES = new CopyOnWriteArrayList<>();

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
     * 获取序列列表
     *
     * @return 序列列表
     */
    public static List<OracleSequence> getSequences() {
        return DB_SEQUENCES;
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
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> refreshAll(client));
    }

    /**
     * 刷新查询索引。
     *
     * @param client 客户端
     */
    public static void refreshIndex(ShellOracleClient client) {
        updateIndex(client);
    }

    /**
     * 刷新模式索引。
     *
     * @param client 客户端
     */
    public static void refreshSchemas(ShellOracleClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<OracleSchema> schemas = client.selectSchemas();
                if (!isAvailable(client)) {
                    return;
                }
                DB_SCHEMAS.clear();
                DB_SCHEMAS.addAll(schemas);
            } catch (Exception ex) {
                logRefreshError("Refresh oracle schema query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新表索引。
     *
     * @param client 客户端
     */
    public static void refreshTables(ShellOracleClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<OracleSchema> schemas = client.selectSchemas();
                List<OracleTable> tables = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (OracleSchema schema : schemas) {
                    if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                        tasks.add(() -> tables.addAll(client.selectTablesSimple(schema.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_TABLES.clear();
                DB_TABLES.addAll(tables);
            } catch (Exception ex) {
                logRefreshError("Refresh oracle table query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新视图索引。
     *
     * @param client 客户端
     */
    public static void refreshViews(ShellOracleClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<OracleSchema> schemas = client.selectSchemas();
                List<OracleView> views = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (OracleSchema schema : schemas) {
                    if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                        tasks.add(() -> views.addAll(client.selectViewsSimple(schema.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_VIEWS.clear();
                DB_VIEWS.addAll(views);
            } catch (Exception ex) {
                logRefreshError("Refresh oracle view query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新函数索引。
     *
     * @param client 客户端
     */
    public static void refreshFunctions(ShellOracleClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<OracleSchema> schemas = client.selectSchemas();
                List<OracleFunction> functions = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (OracleSchema schema : schemas) {
                    if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                        tasks.add(() -> functions.addAll(client.selectFunctionsSimple(schema.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_FUNCTIONS.clear();
                DB_FUNCTIONS.addAll(functions);
            } catch (Exception ex) {
                logRefreshError("Refresh oracle function query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新过程索引。
     *
     * @param client 客户端
     */
    public static void refreshProcedures(ShellOracleClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<OracleSchema> schemas = client.selectSchemas();
                List<OracleProcedure> procedures = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (OracleSchema schema : schemas) {
                    if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                        tasks.add(() -> procedures.addAll(client.selectProceduresSimple(schema.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_PROCEDURES.clear();
                DB_PROCEDURES.addAll(procedures);
            } catch (Exception ex) {
                logRefreshError("Refresh oracle procedure query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新序列索引。
     *
     * @param client 客户端
     */
    public static void refreshSequences(ShellOracleClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<OracleSchema> schemas = client.selectSchemas();
                List<OracleSequence> sequences = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (OracleSchema schema : schemas) {
                    if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                        tasks.add(() -> {
                            try {
                                sequences.addAll(client.selectSequencesSimple(schema.getName()));
                            } catch (Exception ex) {
                                if (isAvailable(client)) {
                                    JulLog.warn("Load oracle sequence query index failed, schema: " + schema.getName(), ex);
                                }
                            }
                        });
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_SEQUENCES.clear();
                DB_SEQUENCES.addAll(sequences);
            } catch (Exception ex) {
                logRefreshError("Refresh oracle sequence query index failed", client, ex);
            }
        });
    }

    private static void refreshAll(ShellOracleClient client) {
        try {
            List<OracleSchema> schemas = client.selectSchemas();
            List<OracleTable> tables = new CopyOnWriteArrayList<>();
            List<OracleView> views = new CopyOnWriteArrayList<>();
            List<OracleFunction> functions = new CopyOnWriteArrayList<>();
            List<OracleProcedure> procedures = new CopyOnWriteArrayList<>();
            List<OracleSequence> sequences = new CopyOnWriteArrayList<>();
            List<Runnable> tasks = new ArrayList<>();
            for (OracleSchema schema : schemas) {
                if (!ShellOracleUtil.isInternalDatabase(schema.getName())) {
                    tasks.add(() -> tables.addAll(client.selectTablesSimple(schema.getName())));
                    tasks.add(() -> views.addAll(client.selectViewsSimple(schema.getName())));
                    tasks.add(() -> functions.addAll(client.selectFunctionsSimple(schema.getName())));
                    tasks.add(() -> procedures.addAll(client.selectProceduresSimple(schema.getName())));
                    tasks.add(() -> {
                        try {
                            sequences.addAll(client.selectSequencesSimple(schema.getName()));
                        } catch (Exception ex) {
                            if (isAvailable(client)) {
                                JulLog.warn("Load oracle sequence query index failed, schema: " + schema.getName(), ex);
                            }
                        }
                    });
                }
            }
            ThreadUtil.submit(tasks);
            if (!isAvailable(client)) {
                return;
            }
            DB_SCHEMAS.clear();
            DB_SCHEMAS.addAll(schemas);
            DB_TABLES.clear();
            DB_TABLES.addAll(tables);
            DB_VIEWS.clear();
            DB_VIEWS.addAll(views);
            DB_FUNCTIONS.clear();
            DB_FUNCTIONS.addAll(functions);
            DB_PROCEDURES.clear();
            DB_PROCEDURES.addAll(procedures);
            DB_SEQUENCES.clear();
            DB_SEQUENCES.addAll(sequences);
        } catch (Exception ex) {
            logRefreshError("Update oracle query index failed", client, ex);
        }
    }

    private static void logRefreshError(String message, ShellOracleClient client, Exception ex) {
        if (isAvailable(client)) {
            JulLog.warn(message, ex);
        } else {
            JulLog.debug(message);
        }
    }
}
