package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static cn.oyzh.easyshell.util.ShellClientUtil.isAvailable;

/**
 * SQLite 查询索引。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class ShellSqliteQueryUtil {

    private static final Set<String> KEYWORDS = new CopyOnWriteArraySet<>(Arrays.asList(DBUtil.SQL_KEYWORDS));

    private static final Set<String> DATABASES = new CopyOnWriteArraySet<>();

    private static final Set<String> TABLES = new CopyOnWriteArraySet<>();

    private static final Set<String> VIEWS = new CopyOnWriteArraySet<>();

    private ShellSqliteQueryUtil() {
    }

    public static Set<String> getKeywords() {
        return Set.copyOf(KEYWORDS);
    }

    public static Set<String> getDatabases() {
        return Set.copyOf(DATABASES);
    }

    public static Set<String> getTables() {
        return Set.copyOf(TABLES);
    }

    public static Set<String> getViews() {
        return Set.copyOf(VIEWS);
    }

    /**
     * 后台更新对象索引。
     *
     * @param client SQLite客户端
     */
    public static void updateIndex(ShellSqliteClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                Set<String> databases = new CopyOnWriteArraySet<>(client.databaseNames());
                Set<String> tables = new CopyOnWriteArraySet<>();
                Set<String> views = new CopyOnWriteArraySet<>();
                for (SQLiteTable table : client.selectTables()) {
                    tables.add(table.getName());
                }
                for (SQLiteView view : client.selectViews()) {
                    views.add(view.getName());
                }
                if (!isAvailable(client)) {
                    return;
                }
                DATABASES.clear();
                DATABASES.addAll(databases);
                TABLES.clear();
                TABLES.addAll(tables);
                VIEWS.clear();
                VIEWS.addAll(views);
            } catch (Exception ex) {
                logRefreshError("Update sqlite query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新对象索引。
     *
     * @param client SQLite客户端
     */
    public static void refreshIndex(ShellSqliteClient client) {
        updateIndex(client);
    }

    /**
     * 刷新数据库索引。
     *
     * @param client SQLite客户端
     */
    public static void refreshDatabases(ShellSqliteClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                Set<String> databases = new CopyOnWriteArraySet<>(client.databaseNames());
                if (!isAvailable(client)) {
                    return;
                }
                DATABASES.clear();
                DATABASES.addAll(databases);
            } catch (Exception ex) {
                logRefreshError("Refresh sqlite database query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新表索引。
     *
     * @param client SQLite客户端
     */
    public static void refreshTables(ShellSqliteClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                Set<String> tables = new CopyOnWriteArraySet<>();
                for (SQLiteTable table : client.selectTables()) {
                    tables.add(table.getName());
                }
                if (!isAvailable(client)) {
                    return;
                }
                TABLES.clear();
                TABLES.addAll(tables);
            } catch (Exception ex) {
                logRefreshError("Refresh sqlite table query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新视图索引。
     *
     * @param client SQLite客户端
     */
    public static void refreshViews(ShellSqliteClient client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                Set<String> views = new CopyOnWriteArraySet<>();
                for (SQLiteView view : client.selectViews()) {
                    views.add(view.getName());
                }
                if (!isAvailable(client)) {
                    return;
                }
                VIEWS.clear();
                VIEWS.addAll(views);
            } catch (Exception ex) {
                logRefreshError("Refresh sqlite view query index failed", client, ex);
            }
        });
    }

    private static void logRefreshError(String message, ShellSqliteClient client, Exception ex) {
        if (isAvailable(client)) {
            JulLog.warn(message, ex);
        } else {
            JulLog.debug(message);
        }
    }
}
