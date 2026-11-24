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

/**
 * SQLite 查询索引。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class ShellSqliteQueryUtil {

    /**
     * 0未初始化
     * 1 初始化中
     * 2 已初始化
     */
    private static volatile int indexStatus = 0;

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
        if (client == null || !client.isConnected() || indexStatus != 0) {
            return;
        }
        indexStatus = 1;
        ThreadUtil.start(() -> {
            try {
                DATABASES.clear();
                TABLES.clear();
                VIEWS.clear();
                DATABASES.addAll(client.databaseNames());
                for (SQLiteTable table : client.selectTables()) {
                    TABLES.add(table.getName());
                }
                for (SQLiteView view : client.selectViews()) {
                    VIEWS.add(view.getName());
                }
                indexStatus = 2;
            } catch (Exception ex) {
                indexStatus = 0;
                JulLog.warn("Update sqlite query index failed", ex);
            }
        });
    }

    /**
     * 刷新对象索引。
     *
     * @param client SQLite客户端
     */
    public static void refreshIndex(ShellSqliteClient client) {
        if (indexStatus != 1) {
            indexStatus = 0;
        }
        updateIndex(client);
    }
}
