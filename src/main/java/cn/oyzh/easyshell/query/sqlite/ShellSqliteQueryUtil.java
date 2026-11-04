package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

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

    private static final Set<String> COLUMNS = new CopyOnWriteArraySet<>();

    private static final List<SQLiteColumn> COLUMN_ITEMS = new CopyOnWriteArrayList<>();

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

    public static Set<String> getColumns() {
        return Set.copyOf(COLUMNS);
    }

    /**
     * 获取字段及其所属表信息。
     *
     * @return 字段列表
     */
    public static List<SQLiteColumn> getColumnItems() {
        return List.copyOf(COLUMN_ITEMS);
    }

    /**
     * 更新对象与字段索引。
     *
     * @param client SQLite客户端
     */
    public static void updateIndex(ShellSqliteClient client) {
        DATABASES.clear();
        TABLES.clear();
        VIEWS.clear();
        COLUMNS.clear();
        COLUMN_ITEMS.clear();
        DATABASES.addAll(client.databaseNames());
        for (SQLiteTable table : client.selectTables()) {
            TABLES.add(table.getName());
            indexColumns(client, table.getName());
        }
        for (SQLiteView view : client.selectViews()) {
            VIEWS.add(view.getName());
            indexColumns(client, view.getName());
        }
    }

    /**
     * 更新对象字段索引，单个对象失效时不影响其他对象。
     *
     * @param client     SQLite客户端
     * @param objectName 表或视图名称
     */
    private static void indexColumns(ShellSqliteClient client, String objectName) {
        try {
            for (SQLiteColumn column : client.selectColumns(objectName)) {
                column.setDbName("main");
                column.setTableName(objectName);
                COLUMNS.add(column.getName());
                COLUMN_ITEMS.add(column);
            }
        } catch (Exception ex) {
            JulLog.warn("Load sqlite columns for object [{}] failed: {}", objectName, ex.getMessage());
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
