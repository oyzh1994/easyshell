package cn.oyzh.easyshell.query.mysql;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.database.MysqlDatabase;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.oyzh.easyshell.util.ShellClientUtil.isAvailable;

/**
 * mysql查询工具类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlQueryUtil {

    /**
     * 关键字
     */
    private static final Set<String> DB_KEYWORDS = new HashSet<>();

    /**
     * 数据库
     */
    private static final List<MysqlDatabase> DB_DATABASES = new CopyOnWriteArrayList<>();

    /**
     * 表
     */
    private static final List<MysqlTable> DB_TABLES = new CopyOnWriteArrayList<>();

    /**
     * 视图
     */
    private static final List<MysqlView> DB_VIEWS = new CopyOnWriteArrayList<>();

    /**
     * 函数
     */
    private static final List<MysqlFunction> DB_FUNCTIONS = new CopyOnWriteArrayList<>();

    /**
     * 过程
     */
    private static final List<MysqlProcedure> DB_PROCEDURES = new CopyOnWriteArrayList<>();

    /**
     * 事件
     */
    private static final List<MysqlEvent> DB_EVENTS = new CopyOnWriteArrayList<>();

    /**
     * 字段
     */
    private static final List<MysqlColumn> DB_COLUMNS = new CopyOnWriteArrayList<>();

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
     * 获取数据库列表
     *
     * @return 数据库列表
     */
    public static List<MysqlDatabase> getDatabases() {
        return DB_DATABASES;
    }

    /**
     * 获取表列表
     *
     * @return 表列表
     */
    public static List<MysqlTable> getTables() {
        return DB_TABLES;
    }

    /**
     * 获取关键字列表
     *
     * @return 结果
     */
    public static Set<String> getDbKeywords() {
        Set<String> list = new HashSet<>(getKeywords());
//        for (DBName name : DB_DATABASES) {
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
//        for (DBName name : DB_EVENTS) {
//            list.add(name.getName());
//        }
        return list;
    }

    /**
     * 获取视图列表
     *
     * @return 视图列表
     */
    public static List<MysqlView> getViews() {
        return DB_VIEWS;
    }

    /**
     * 获取函数列表
     *
     * @return 函数列表
     */
    public static List<MysqlFunction> getFunctions() {
        return DB_FUNCTIONS;
    }

    /**
     * 获取存储过程列表
     *
     * @return 存储过程列表
     */
    public static List<MysqlProcedure> getProcedures() {
        return DB_PROCEDURES;
    }

    /**
     * 获取事件列表
     *
     * @return 事件列表
     */
    public static List<MysqlEvent> getEvents() {
        return DB_EVENTS;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public static List<MysqlColumn> getColumns() {
        return DB_COLUMNS;
    }

    /**
     * 更新索引
     *
     * @param client 客户端
     */
    public static void updateIndex(ShellMysqlClient client) {
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
    public static void refreshIndex(ShellMysqlClient client) {
        updateIndex(client);
    }

    /**
     * 刷新数据库索引。
     *
     * @param client 客户端
     */
    public static void refreshDatabases(ShellMysqlClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MysqlDatabase> databases = client.databases();
                if (!isAvailable(client)) {
                    return;
                }
                DB_DATABASES.clear();
                DB_DATABASES.addAll(databases);
            } catch (Exception ex) {
                logRefreshError("Refresh mysql database query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新表索引。
     *
     * @param client 客户端
     */
    public static void refreshTables(ShellMysqlClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MysqlDatabase> databases = client.databases();
                List<MysqlTable> tables = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MysqlDatabase database : databases) {
                    if (!ShellMysqlUtil.isInternalDatabase(database.getName())) {
                        tasks.add(() -> tables.addAll(client.selectTablesSimple(database.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_TABLES.clear();
                DB_TABLES.addAll(tables);
            } catch (Exception ex) {
                logRefreshError("Refresh mysql table query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新视图索引。
     *
     * @param client 客户端
     */
    public static void refreshViews(ShellMysqlClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MysqlDatabase> databases = client.databases();
                List<MysqlView> views = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MysqlDatabase database : databases) {
                    if (!ShellMysqlUtil.isInternalDatabase(database.getName())) {
                        tasks.add(() -> views.addAll(client.selectViewsSimple(database.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_VIEWS.clear();
                DB_VIEWS.addAll(views);
            } catch (Exception ex) {
                logRefreshError("Refresh mysql view query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新函数索引。
     *
     * @param client 客户端
     */
    public static void refreshFunctions(ShellMysqlClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MysqlDatabase> databases = client.databases();
                List<MysqlFunction> functions = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MysqlDatabase database : databases) {
                    if (!ShellMysqlUtil.isInternalDatabase(database.getName())) {
                        tasks.add(() -> functions.addAll(client.selectFunctionsSimple(database.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_FUNCTIONS.clear();
                DB_FUNCTIONS.addAll(functions);
            } catch (Exception ex) {
                logRefreshError("Refresh mysql function query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新过程索引。
     *
     * @param client 客户端
     */
    public static void refreshProcedures(ShellMysqlClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MysqlDatabase> databases = client.databases();
                List<MysqlProcedure> procedures = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MysqlDatabase database : databases) {
                    if (!ShellMysqlUtil.isInternalDatabase(database.getName())) {
                        tasks.add(() -> procedures.addAll(client.selectProceduresSimple(database.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_PROCEDURES.clear();
                DB_PROCEDURES.addAll(procedures);
            } catch (Exception ex) {
                logRefreshError("Refresh mysql procedure query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新事件索引。
     *
     * @param client 客户端
     */
    public static void refreshEvents(ShellMysqlClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MysqlDatabase> databases = client.databases();
                List<MysqlEvent> events = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MysqlDatabase database : databases) {
                    if (!ShellMysqlUtil.isInternalDatabase(database.getName())) {
                        tasks.add(() -> events.addAll(client.selectEventsSimple(database.getName())));
                    }
                }
                ThreadUtil.submit(tasks);
                if (!isAvailable(client)) {
                    return;
                }
                DB_EVENTS.clear();
                DB_EVENTS.addAll(events);
            } catch (Exception ex) {
                logRefreshError("Refresh mysql event query index failed", client, ex);
            }
        });
    }

    private static void refreshAll(ShellMysqlClient client) {
        try {
            List<MysqlDatabase> databases = client.databases();
            List<MysqlTable> tables = new CopyOnWriteArrayList<>();
            List<MysqlView> views = new CopyOnWriteArrayList<>();
            List<MysqlFunction> functions = new CopyOnWriteArrayList<>();
            List<MysqlProcedure> procedures = new CopyOnWriteArrayList<>();
            List<MysqlEvent> events = new CopyOnWriteArrayList<>();
            List<Runnable> tasks = new ArrayList<>();
            for (MysqlDatabase database : databases) {
                if (!ShellMysqlUtil.isInternalDatabase(database.getName())) {
                    tasks.add(() -> tables.addAll(client.selectTablesSimple(database.getName())));
                    tasks.add(() -> views.addAll(client.selectViewsSimple(database.getName())));
                    tasks.add(() -> functions.addAll(client.selectFunctionsSimple(database.getName())));
                    tasks.add(() -> procedures.addAll(client.selectProceduresSimple(database.getName())));
                    tasks.add(() -> events.addAll(client.selectEventsSimple(database.getName())));
                }
            }
            ThreadUtil.submit(tasks);
            if (!isAvailable(client)) {
                return;
            }
            DB_DATABASES.clear();
            DB_DATABASES.addAll(databases);
            DB_TABLES.clear();
            DB_TABLES.addAll(tables);
            DB_VIEWS.clear();
            DB_VIEWS.addAll(views);
            DB_FUNCTIONS.clear();
            DB_FUNCTIONS.addAll(functions);
            DB_PROCEDURES.clear();
            DB_PROCEDURES.addAll(procedures);
            DB_EVENTS.clear();
            DB_EVENTS.addAll(events);
        } catch (Exception ex) {
            logRefreshError("Update mysql query index failed", client, ex);
        }
    }

    private static void logRefreshError(String message, ShellMysqlClient client, Exception ex) {
        if (isAvailable(client)) {
            JulLog.warn(message, ex);
        } else {
            JulLog.debug(message);
        }
    }
}
