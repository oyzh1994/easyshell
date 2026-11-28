package cn.oyzh.easyshell.event.h2;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.h2.query.ShellH2QueryChangedEvent;
import cn.oyzh.easyshell.event.h2.query.ShellH2QueryDeletedEvent;
import cn.oyzh.easyshell.event.h2.query.ShellH2QueryOpenEvent;
import cn.oyzh.easyshell.event.h2.query.ShellH2QueryRenamedEvent;
import cn.oyzh.easyshell.event.h2.table.ShellH2TableChangedEvent;
import cn.oyzh.easyshell.event.h2.table.ShellH2TableDesignEvent;
import cn.oyzh.easyshell.event.h2.table.ShellH2TableOpenEvent;
import cn.oyzh.easyshell.event.h2.terminal.ShellH2TerminalOpenEvent;
import cn.oyzh.easyshell.event.h2.view.ShellH2ViewChangedEvent;
import cn.oyzh.easyshell.event.h2.view.ShellH2ViewDesignEvent;
import cn.oyzh.easyshell.event.h2.view.ShellH2ViewOpenEvent;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.event.EventUtil;

/**
 * H2 事件工具。
 *
 * @since 2026-11-28
 */
public final class ShellH2EventUtil {

    private ShellH2EventUtil() {
    }

    public static void tableOpen(ShellH2Client client, String tableName) {
        tableOpen(client, client.schema(), tableName);
    }

    public static void tableOpen(ShellH2Client client, String schema, String tableName) {
        ShellH2TableOpenEvent event = new ShellH2TableOpenEvent();
        event.setClient(client);
        event.setSchema(schema);
        event.data(tableName);
        EventUtil.post(event);
    }

    public static void designTable(ShellH2Client client, String tableName) {
        designTable(client, client.schema(), tableName);
    }

    public static void designTable(ShellH2Client client, String schema, String tableName) {
        ShellH2TableDesignEvent event = new ShellH2TableDesignEvent();
        event.setClient(client);
        event.setSchema(schema);
        event.data(tableName);
        EventUtil.post(event);
    }

    public static void tableChanged(ShellH2Client client, String originalName, String tableName) {
        tableChanged(client, client.schema(), originalName, tableName);
    }

    public static void tableChanged(ShellH2Client client, String schema,
                                    String originalName, String tableName) {
        ShellH2TableChangedEvent event = new ShellH2TableChangedEvent();
        event.setClient(client);
        event.setSchema(schema);
        event.setOriginalName(originalName);
        event.data(tableName);
        EventUtil.post(event);
    }

    public static void viewOpen(ShellH2Client client, String viewName) {
        viewOpen(client, client.schema(), viewName);
    }

    public static void viewOpen(ShellH2Client client, String schema, String viewName) {
        ShellH2ViewOpenEvent event = new ShellH2ViewOpenEvent();
        event.setClient(client);
        event.setSchema(schema);
        event.data(viewName);
        EventUtil.post(event);
    }

    public static void designView(ShellH2Client client, String viewName) {
        designView(client, client.schema(), viewName);
    }

    public static void designView(ShellH2Client client, String schema, String viewName) {
        ShellH2ViewDesignEvent event = new ShellH2ViewDesignEvent();
        event.setClient(client);
        event.setSchema(schema);
        event.data(viewName);
        EventUtil.post(event);
    }

    public static void viewChanged(ShellH2Client client, String originalName, String viewName) {
        viewChanged(client, client.schema(), originalName, viewName);
    }

    public static void viewChanged(ShellH2Client client, String schema,
                                   String originalName, String viewName) {
        ShellH2ViewChangedEvent event = new ShellH2ViewChangedEvent();
        event.setClient(client);
        event.setSchema(schema);
        event.setOriginalName(originalName);
        event.data(viewName);
        EventUtil.post(event);
    }

    public static void queryOpen(ShellH2Client client, ShellQuery query) {
        ShellH2QueryOpenEvent event = new ShellH2QueryOpenEvent();
        event.setClient(client);
        event.data(query == null ? new ShellQuery() : query);
        EventUtil.post(event);
    }

    public static void queryChanged(ShellH2Client client, ShellQuery query) {
        ShellH2QueryChangedEvent event = new ShellH2QueryChangedEvent();
        event.setClient(client);
        event.data(query);
        EventUtil.post(event);
    }

    public static void queryDeleted(ShellH2Client client, ShellQuery query) {
        ShellH2QueryDeletedEvent event = new ShellH2QueryDeletedEvent();
        event.setClient(client);
        event.data(query);
        EventUtil.post(event);
    }

    public static void queryRenamed(ShellH2Client client, ShellQuery query, String oldName) {
        ShellH2QueryRenamedEvent event = new ShellH2QueryRenamedEvent();
        event.setClient(client);
        event.setOldName(oldName);
        event.data(query);
        EventUtil.post(event);
    }

    public static void terminalOpen(ShellH2Client client) {
        terminalOpen(client, client.schema());
    }

    public static void terminalOpen(ShellH2Client client, String schema) {
        ShellH2TerminalOpenEvent event = new ShellH2TerminalOpenEvent();
        event.data(client);
        event.setSchema(schema);
        EventUtil.post(event);
    }
}
