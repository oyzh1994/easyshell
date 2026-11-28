package cn.oyzh.easyshell.query.h2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static cn.oyzh.easyshell.util.ShellClientUtil.isAvailable;

/**
 * H2 查询索引。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class ShellH2QueryUtil {

    private static final Set<String> KEYWORDS = new CopyOnWriteArraySet<>(Arrays.asList(DBUtil.SQL_KEYWORDS));

    private static final Set<String> DATABASES = new CopyOnWriteArraySet<>();

    private static final Set<String> TABLES = new CopyOnWriteArraySet<>();

    private static final Set<String> VIEWS = new CopyOnWriteArraySet<>();

    private ShellH2QueryUtil() {
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
     * @param client H2客户端
     */
    public static void updateIndex(ShellH2Client client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                Set<String> databases = new CopyOnWriteArraySet<>(client.databaseNames());
                Set<String> tables = new CopyOnWriteArraySet<>();
                Set<String> views = new CopyOnWriteArraySet<>();
                for (H2Table table : client.selectTables()) {
                    tables.add(table.getName());
                }
                for (H2View view : client.selectViews()) {
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
                logRefreshError("Update h2 query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新对象索引。
     *
     * @param client H2客户端
     */
    public static void refreshIndex(ShellH2Client client) {
        updateIndex(client);
    }

    /**
     * 刷新数据库索引。
     *
     * @param client H2客户端
     */
    public static void refreshDatabases(ShellH2Client client) {
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
                logRefreshError("Refresh h2 database query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新表索引。
     *
     * @param client H2客户端
     */
    public static void refreshTables(ShellH2Client client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                Set<String> tables = new CopyOnWriteArraySet<>();
                for (H2Table table : client.selectTables()) {
                    tables.add(table.getName());
                }
                if (!isAvailable(client)) {
                    return;
                }
                TABLES.clear();
                TABLES.addAll(tables);
            } catch (Exception ex) {
                logRefreshError("Refresh h2 table query index failed", client, ex);
            }
        });
    }

    /**
     * 刷新视图索引。
     *
     * @param client H2客户端
     */
    public static void refreshViews(ShellH2Client client) {
        if (!isAvailable(client)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                Set<String> views = new CopyOnWriteArraySet<>();
                for (H2View view : client.selectViews()) {
                    views.add(view.getName());
                }
                if (!isAvailable(client)) {
                    return;
                }
                VIEWS.clear();
                VIEWS.addAll(views);
            } catch (Exception ex) {
                logRefreshError("Refresh h2 view query index failed", client, ex);
            }
        });
    }

    private static void logRefreshError(String message, ShellH2Client client, Exception ex) {
        if (isAvailable(client)) {
            JulLog.warn(message, ex);
        } else {
            JulLog.debug(message);
        }
    }
}
