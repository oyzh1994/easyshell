package cn.oyzh.easyshell.query.dameng;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.dameng.sequence.DamengSequence;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.util.dameng.ShellDamengUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.oyzh.easyshell.util.ShellClientUtil.isAvailable;

/**
 * 达梦查询工具类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengQueryUtil {

    /**
     * 关键字
     */
    private static final Set<String> DB_KEYWORDS = new HashSet<>();

    /**
     * 数据库
     */
    private static final List<DamengSchema> DB_SCHEMAS = new CopyOnWriteArrayList<>();

    /**
     * 表
     */
    private static final List<DamengTable> DB_TABLES = new CopyOnWriteArrayList<>();

    /**
     * 视图
     */
    private static final List<DamengView> DB_VIEWS = new CopyOnWriteArrayList<>();

    /**
     * 函数
     */
    private static final List<DamengFunction> DB_FUNCTIONS = new CopyOnWriteArrayList<>();

    /**
     * 过程
     */
    private static final List<DamengProcedure> DB_PROCEDURES = new CopyOnWriteArrayList<>();

    /**
     * 序列
     */
    private static final List<DamengSequence> DB_SEQUENCES = new CopyOnWriteArrayList<>();

    /**
     * 字段
     */
    private static final List<DamengColumn> DB_COLUMNS = new CopyOnWriteArrayList<>();

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
        DB_KEYWORDS.add("DATABASE");
        DB_KEYWORDS.add("DATABASES");
        DB_KEYWORDS.add("SHOW");
        DB_KEYWORDS.add("ALTER TABLE");
        DB_KEYWORDS.add("CREATE TABLE");
        DB_KEYWORDS.add("DROP TABLE");
        DB_KEYWORDS.add("CHANGE");
        DB_KEYWORDS.add("COLUMN");
        DB_KEYWORDS.add("CHARACTER SET");
        DB_KEYWORDS.add("COMMENT");
        DB_KEYWORDS.add("FIRST");
        DB_KEYWORDS.add("COLLATE");

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
        DB_KEYWORDS.add("LIMIT");
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
        DB_KEYWORDS.add("EXCEPT");
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
        DB_KEYWORDS.add("CONCAT");
        DB_KEYWORDS.add("LENGTH");
        DB_KEYWORDS.add("SUBSTRING");
        DB_KEYWORDS.add("UPPER");
        DB_KEYWORDS.add("LOWER");
        DB_KEYWORDS.add("TRIM");
        DB_KEYWORDS.add("ROUND");

        DB_KEYWORDS.addAll(List.of(DBUtil.SQL_KEYWORDS));
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
    public static List<DamengSchema> getSchemas() {
        return DB_SCHEMAS;
    }

    /**
     * 获取表列表
     *
     * @return 表列表
     */
    public static List<DamengTable> getTables() {
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
    public static List<DamengView> getViews() {
        return DB_VIEWS;
    }

    /**
     * 获取函数列表
     *
     * @return 函数列表
     */
    public static List<DamengFunction> getFunctions() {
        return DB_FUNCTIONS;
    }

    /**
     * 获取存储过程列表
     *
     * @return 存储过程列表
     */
    public static List<DamengProcedure> getProcedures() {
        return DB_PROCEDURES;
    }

    /**
     * 获取序列列表
     *
     * @return 序列列表
     */
    public static List<DamengSequence> getSequences() {
        return DB_SEQUENCES;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public static List<DamengColumn> getColumns() {
        return DB_COLUMNS;
    }

    /**
     * 更新索引
     *
     * @param client 客户端
     */
    public static void updateIndex(ShellDamengClient client) {
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
    public static void refreshIndex(ShellDamengClient client) {
        updateIndex(client);
    }

    /**
     * 刷新模式索引。
     *
     * @param client 客户端
     */
    public static void refreshSchemas(ShellDamengClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<DamengSchema> schemas = client.selectSchemas();
                if (!isAvailable(client)) {
                    return;
                }
                DB_SCHEMAS.clear();
                DB_SCHEMAS.addAll(schemas);
            } catch (Exception ex) {
                logRefreshError("Refresh dameng schema query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新表索引。
     *
     * @param client 客户端
     */
    public static void refreshTables(ShellDamengClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<DamengSchema> schemas = client.selectSchemas();
                List<DamengTable> tables = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (DamengSchema schema : schemas) {
                    if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
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
                logRefreshError("Refresh dameng table query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新视图索引。
     *
     * @param client 客户端
     */
    public static void refreshViews(ShellDamengClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<DamengSchema> schemas = client.selectSchemas();
                List<DamengView> views = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (DamengSchema schema : schemas) {
                    if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
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
                logRefreshError("Refresh dameng view query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新函数索引。
     *
     * @param client 客户端
     */
    public static void refreshFunctions(ShellDamengClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<DamengSchema> schemas = client.selectSchemas();
                List<DamengFunction> functions = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (DamengSchema schema : schemas) {
                    if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
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
                logRefreshError("Refresh dameng function query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新过程索引。
     *
     * @param client 客户端
     */
    public static void refreshProcedures(ShellDamengClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<DamengSchema> schemas = client.selectSchemas();
                List<DamengProcedure> procedures = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (DamengSchema schema : schemas) {
                    if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
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
                logRefreshError("Refresh dameng procedure query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新序列索引。
     *
     * @param client 客户端
     */
    public static void refreshSequences(ShellDamengClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<DamengSchema> schemas = client.selectSchemas();
                List<DamengSequence> sequences = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (DamengSchema schema : schemas) {
                    if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
                        tasks.add(() -> {
                            try {
                                sequences.addAll(client.selectSequencesSimple(schema.getName()));
                            } catch (Exception ex) {
                                if (isAvailable(client)) {
                                    JulLog.warn("Load dameng sequence query index failed, schema: " + schema.getName(), ex);
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
                logRefreshError("Refresh dameng sequence query index failed", client, ex);
            }
        });
    }

    private static void refreshAll(ShellDamengClient client) {
        try {
            List<DamengSchema> schemas = client.selectSchemas();
            List<DamengTable> tables = new CopyOnWriteArrayList<>();
            List<DamengView> views = new CopyOnWriteArrayList<>();
            List<DamengFunction> functions = new CopyOnWriteArrayList<>();
            List<DamengProcedure> procedures = new CopyOnWriteArrayList<>();
            List<DamengSequence> sequences = new CopyOnWriteArrayList<>();
            List<Runnable> tasks = new ArrayList<>();
            for (DamengSchema schema : schemas) {
                if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
                    tasks.add(() -> tables.addAll(client.selectTablesSimple(schema.getName())));
                    tasks.add(() -> views.addAll(client.selectViewsSimple(schema.getName())));
                    tasks.add(() -> functions.addAll(client.selectFunctionsSimple(schema.getName())));
                    tasks.add(() -> procedures.addAll(client.selectProceduresSimple(schema.getName())));
                    tasks.add(() -> {
                        try {
                            sequences.addAll(client.selectSequencesSimple(schema.getName()));
                        } catch (Exception ex) {
                            if (isAvailable(client)) {
                                JulLog.warn("Load dameng sequence query index failed, schema: " + schema.getName(), ex);
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
            logRefreshError("Update dameng query index failed", client, ex);
        }
    }

    private static void logRefreshError(String message, ShellDamengClient client, Exception ex) {
        if (isAvailable(client)) {
            JulLog.warn(message, ex);
        } else {
            JulLog.debug(message);
        }
    }
}
