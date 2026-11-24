package cn.oyzh.easyshell.query.mariadb;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.oyzh.easyshell.util.ShellClientUtil.isAvailable;

/**
 * MariaDB查询工具类
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryUtil {

    /**
     * 关键字
     */
    private static final Set<String> DB_KEYWORDS = new HashSet<>();

    /**
     * 数据库
     */
    private static final List<MariadbDatabase> DB_DATABASES = new CopyOnWriteArrayList<>();

    /**
     * 表
     */
    private static final List<MariadbTable> DB_TABLES = new CopyOnWriteArrayList<>();

    /**
     * 视图
     */
    private static final List<MariadbView> DB_VIEWS = new CopyOnWriteArrayList<>();

    /**
     * 函数
     */
    private static final List<MariadbFunction> DB_FUNCTIONS = new CopyOnWriteArrayList<>();

    /**
     * 过程
     */
    private static final List<MariadbProcedure> DB_PROCEDURES = new CopyOnWriteArrayList<>();

    /**
     * 事件
     */
    private static final List<MariadbEvent> DB_EVENTS = new CopyOnWriteArrayList<>();

    /**
     * 字段
     */
    private static final List<MariadbColumn> DB_COLUMNS = new CopyOnWriteArrayList<>();

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
    public static List<MariadbDatabase> getDatabases() {
        return DB_DATABASES;
    }

    /**
     * 获取表列表
     *
     * @return 表列表
     */
    public static List<MariadbTable> getTables() {
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
    public static List<MariadbView> getViews() {
        return DB_VIEWS;
    }

    /**
     * 获取函数列表
     *
     * @return 函数列表
     */
    public static List<MariadbFunction> getFunctions() {
        return DB_FUNCTIONS;
    }

    /**
     * 获取存储过程列表
     *
     * @return 存储过程列表
     */
    public static List<MariadbProcedure> getProcedures() {
        return DB_PROCEDURES;
    }

    /**
     * 获取事件列表
     *
     * @return 事件列表
     */
    public static List<MariadbEvent> getEvents() {
        return DB_EVENTS;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public static List<MariadbColumn> getColumns() {
        return DB_COLUMNS;
    }

    /**
     * 更新索引
     *
     * @param client 客户端
     */
    public static void updateIndex(ShellMariadbClient client) {
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
    public static void refreshIndex(ShellMariadbClient client) {
        updateIndex(client);
    }

    /**
     * 刷新数据库索引。
     *
     * @param client 客户端
     */
    public static void refreshDatabases(ShellMariadbClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MariadbDatabase> databases = client.databases();
                if (!isAvailable(client)) {
                    return;
                }
                DB_DATABASES.clear();
                DB_DATABASES.addAll(databases);
            } catch (Exception ex) {
                logRefreshError("Refresh mariadb database query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新表索引。
     *
     * @param client 客户端
     */
    public static void refreshTables(ShellMariadbClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MariadbDatabase> databases = client.databases();
                List<MariadbTable> tables = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MariadbDatabase database : databases) {
                    if (!ShellMariadbUtil.isInternalDatabase(database.getName())) {
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
                logRefreshError("Refresh mariadb table query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新视图索引。
     *
     * @param client 客户端
     */
    public static void refreshViews(ShellMariadbClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MariadbDatabase> databases = client.databases();
                List<MariadbView> views = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MariadbDatabase database : databases) {
                    if (!ShellMariadbUtil.isInternalDatabase(database.getName())) {
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
                logRefreshError("Refresh mariadb view query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新函数索引。
     *
     * @param client 客户端
     */
    public static void refreshFunctions(ShellMariadbClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MariadbDatabase> databases = client.databases();
                List<MariadbFunction> functions = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MariadbDatabase database : databases) {
                    if (!ShellMariadbUtil.isInternalDatabase(database.getName())) {
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
                logRefreshError("Refresh mariadb function query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新过程索引。
     *
     * @param client 客户端
     */
    public static void refreshProcedures(ShellMariadbClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MariadbDatabase> databases = client.databases();
                List<MariadbProcedure> procedures = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MariadbDatabase database : databases) {
                    if (!ShellMariadbUtil.isInternalDatabase(database.getName())) {
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
                logRefreshError("Refresh mariadb procedure query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新事件索引。
     *
     * @param client 客户端
     */
    public static void refreshEvents(ShellMariadbClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<MariadbDatabase> databases = client.databases();
                List<MariadbEvent> events = new CopyOnWriteArrayList<>();
                List<Runnable> tasks = new ArrayList<>();
                for (MariadbDatabase database : databases) {
                    if (!ShellMariadbUtil.isInternalDatabase(database.getName())) {
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
                logRefreshError("Refresh mariadb event query index failed", client, ex);
            }
        });
    }

    private static void refreshAll(ShellMariadbClient client) {
        try {
            List<MariadbDatabase> databases = client.databases();
            List<MariadbTable> tables = new CopyOnWriteArrayList<>();
            List<MariadbView> views = new CopyOnWriteArrayList<>();
            List<MariadbFunction> functions = new CopyOnWriteArrayList<>();
            List<MariadbProcedure> procedures = new CopyOnWriteArrayList<>();
            List<MariadbEvent> events = new CopyOnWriteArrayList<>();
            List<Runnable> tasks = new ArrayList<>();
            for (MariadbDatabase database : databases) {
                if (!ShellMariadbUtil.isInternalDatabase(database.getName())) {
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
            logRefreshError("Update mariadb query index failed", client, ex);
        }
    }

    private static void logRefreshError(String message, ShellMariadbClient client, Exception ex) {
        if (isAvailable(client)) {
            JulLog.warn(message, ex);
        } else {
            JulLog.debug(message);
        }
    }
}
