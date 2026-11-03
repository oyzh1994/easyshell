package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
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

    private static final Set<String> KEYWORDS = new CopyOnWriteArraySet<>(Arrays.asList(DBUtil.SQL_KEYWORDS));

    private static final Set<String> TABLES = new CopyOnWriteArraySet<>();

    private static final Set<String> VIEWS = new CopyOnWriteArraySet<>();

    private static final Set<String> COLUMNS = new CopyOnWriteArraySet<>();

    private ShellSqliteQueryUtil() {
    }

    public static Set<String> getKeywords() {
        return Set.copyOf(KEYWORDS);
    }

    public static Set<String> getTables() {
        return Set.copyOf(TABLES);
    }

    public static Set<String> getViews() {
        return Set.copyOf(VIEWS);
    }

    public static Set<String> getColumns() {
        return Set.copyOf(COLUMNS);
    }

    /**
     * 更新对象与字段索引。
     *
     * @param client SQLite客户端
     */
    public static void updateIndex(ShellSqliteClient client) {
        TABLES.clear();
        VIEWS.clear();
        COLUMNS.clear();
        for (SQLiteTable table : client.selectTables()) {
            TABLES.add(table.getName());
            for (SQLiteColumn column : client.selectColumns(table.getName())) {
                COLUMNS.add(column.getName());
            }
        }
        for (SQLiteView view : client.selectViews()) {
            VIEWS.add(view.getName());
            for (SQLiteColumn column : client.selectColumns(view.getName())) {
                COLUMNS.add(column.getName());
            }
        }
    }

    /**
     * 后台更新对象与字段索引。
     *
     * @param client SQLite客户端
     */
    public static void updateIndexAsync(ShellSqliteClient client) {
        ThreadUtil.start(() -> {
            try {
                if (client != null && client.isConnected()) {
                    updateIndex(client);
                }
            } catch (Exception ex) {
                JulLog.debug("Update sqlite query index failed", ex);
            }
        });
    }
}
