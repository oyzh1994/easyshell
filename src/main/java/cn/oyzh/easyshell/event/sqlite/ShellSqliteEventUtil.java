package cn.oyzh.easyshell.event.sqlite;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.sqlite.function.ShellSqliteFunctionChangedEvent;
import cn.oyzh.easyshell.event.sqlite.function.ShellSqliteFunctionDesignEvent;
import cn.oyzh.easyshell.event.sqlite.query.ShellSqliteQueryChangedEvent;
import cn.oyzh.easyshell.event.sqlite.query.ShellSqliteQueryDeletedEvent;
import cn.oyzh.easyshell.event.sqlite.query.ShellSqliteQueryOpenEvent;
import cn.oyzh.easyshell.event.sqlite.query.ShellSqliteQueryRenamedEvent;
import cn.oyzh.easyshell.event.sqlite.table.ShellSqliteTableChangedEvent;
import cn.oyzh.easyshell.event.sqlite.table.ShellSqliteTableDesignEvent;
import cn.oyzh.easyshell.event.sqlite.table.ShellSqliteTableOpenEvent;
import cn.oyzh.easyshell.event.sqlite.terminal.ShellSqliteTerminalOpenEvent;
import cn.oyzh.easyshell.event.sqlite.view.ShellSqliteViewChangedEvent;
import cn.oyzh.easyshell.event.sqlite.view.ShellSqliteViewDesignEvent;
import cn.oyzh.easyshell.event.sqlite.view.ShellSqliteViewOpenEvent;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.event.EventUtil;

/**
 * SQLite 事件工具。
 *
 * @since 2026-11-02
 */
public final class ShellSqliteEventUtil {

    private ShellSqliteEventUtil() {
    }

    public static void tableOpen(ShellSqliteClient client, String tableName) {
        ShellSqliteTableOpenEvent event = new ShellSqliteTableOpenEvent();
        event.setClient(client);
        event.data(tableName);
        EventUtil.post(event);
    }

    public static void designTable(ShellSqliteClient client, String tableName) {
        ShellSqliteTableDesignEvent event = new ShellSqliteTableDesignEvent();
        event.setClient(client);
        event.data(tableName);
        EventUtil.post(event);
    }

    public static void tableChanged(ShellSqliteClient client, String originalName, String tableName) {
        ShellSqliteTableChangedEvent event = new ShellSqliteTableChangedEvent();
        event.setClient(client);
        event.setOriginalName(originalName);
        event.data(tableName);
        EventUtil.post(event);
    }

    public static void viewOpen(ShellSqliteClient client, String viewName) {
        ShellSqliteViewOpenEvent event = new ShellSqliteViewOpenEvent();
        event.setClient(client);
        event.data(viewName);
        EventUtil.post(event);
    }

    public static void designView(ShellSqliteClient client, String viewName) {
        ShellSqliteViewDesignEvent event = new ShellSqliteViewDesignEvent();
        event.setClient(client);
        event.data(viewName);
        EventUtil.post(event);
    }

    public static void viewChanged(ShellSqliteClient client, String originalName, String viewName) {
        ShellSqliteViewChangedEvent event = new ShellSqliteViewChangedEvent();
        event.setClient(client);
        event.setOriginalName(originalName);
        event.data(viewName);
        EventUtil.post(event);
    }

    public static void designFunction(ShellSqliteClient client, SQLiteFunction function) {
        ShellSqliteFunctionDesignEvent event = new ShellSqliteFunctionDesignEvent();
        event.setClient(client);
        event.data(function);
        EventUtil.post(event);
    }

    public static void functionChanged(ShellSqliteClient client, String originalName, SQLiteFunction function) {
        ShellSqliteFunctionChangedEvent event = new ShellSqliteFunctionChangedEvent();
        event.setClient(client);
        event.setOriginalName(originalName);
        event.data(function);
        EventUtil.post(event);
    }

    public static void queryOpen(ShellSqliteClient client, ShellQuery query) {
        ShellSqliteQueryOpenEvent event = new ShellSqliteQueryOpenEvent();
        event.setClient(client);
        event.data(query == null ? new ShellQuery() : query);
        EventUtil.post(event);
    }

    public static void queryChanged(ShellSqliteClient client, ShellQuery query) {
        ShellSqliteQueryChangedEvent event = new ShellSqliteQueryChangedEvent();
        event.setClient(client);
        event.data(query);
        EventUtil.post(event);
    }

    public static void queryDeleted(ShellSqliteClient client, ShellQuery query) {
        ShellSqliteQueryDeletedEvent event = new ShellSqliteQueryDeletedEvent();
        event.setClient(client);
        event.data(query);
        EventUtil.post(event);
    }

    public static void queryRenamed(ShellSqliteClient client, ShellQuery query, String oldName) {
        ShellSqliteQueryRenamedEvent event = new ShellSqliteQueryRenamedEvent();
        event.setClient(client);
        event.setOldName(oldName);
        event.data(query);
        EventUtil.post(event);
    }

    public static void terminalOpen(ShellSqliteClient client) {
        ShellSqliteTerminalOpenEvent event = new ShellSqliteTerminalOpenEvent();
        event.data(client);
        EventUtil.post(event);
    }
}
