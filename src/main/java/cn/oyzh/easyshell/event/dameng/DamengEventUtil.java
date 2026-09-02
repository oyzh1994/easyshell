package cn.oyzh.easyshell.event.dameng;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.dameng.connect.DBAddConnectEvent;
import cn.oyzh.easyshell.event.dameng.connect.DBConnectionClosedEvent;
import cn.oyzh.easyshell.event.dameng.connect.DBConnectionConnectedEvent;
import cn.oyzh.easyshell.event.dameng.connect.DamengConnectAddedEvent;
import cn.oyzh.easyshell.event.dameng.connect.DamengConnectDeletedEvent;
import cn.oyzh.easyshell.event.dameng.connect.DamengConnectUpdatedEvent;
import cn.oyzh.easyshell.event.dameng.function.DamengFunctionAddedEvent;
import cn.oyzh.easyshell.event.dameng.function.DamengFunctionAlertedEvent;
import cn.oyzh.easyshell.event.dameng.function.DamengFunctionDesignEvent;
import cn.oyzh.easyshell.event.dameng.group.DBAddGroupEvent;
import cn.oyzh.easyshell.event.dameng.procedure.DamengProcedureAddedEvent;
import cn.oyzh.easyshell.event.dameng.procedure.DamengProcedureAlertedEvent;
import cn.oyzh.easyshell.event.dameng.procedure.DamengProcedureDesignEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryAddEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryAddedEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryDeletedEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryOpenEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryRenamedEvent;
import cn.oyzh.easyshell.event.dameng.schema.DamengSchemaAddedEvent;
import cn.oyzh.easyshell.event.dameng.schema.DamengSchemaClosedEvent;
import cn.oyzh.easyshell.event.dameng.schema.DamengSchemaDroppedEvent;
import cn.oyzh.easyshell.event.dameng.schema.DamengSchemaUpdatedEvent;
import cn.oyzh.easyshell.event.dameng.sql.ShellPrintSqlEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableAddedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableAlertedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableClearedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableDesignEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableDroppedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableFilteredEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableOpenEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableRenamedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableTruncatedEvent;
import cn.oyzh.easyshell.event.dameng.terminal.DBTerminalCloseEvent;
import cn.oyzh.easyshell.event.dameng.terminal.DBTerminalOpenEvent;
import cn.oyzh.easyshell.event.dameng.tree.DamengTreeItemChangedEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewAddedEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewAlertedEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewDesignEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewFilteredEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewOpenEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewRenamedEvent;
import cn.oyzh.easyshell.event.dameng.window.ShellShowMessageEvent;
import cn.oyzh.easyshell.trees.dameng.query.DamengQueryTreeItem;
import cn.oyzh.easyshell.trees.dameng.root.DBRootTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.DamengTableTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.DamengViewTreeItem;
import cn.oyzh.event.EventUtil;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.plus.changelog.ChangelogEvent;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * redis事件工具
 *
 * @author oyzh
 * @since 2023/11/20
 */
public class DamengEventUtil {

    public static void tableOpen(DamengTableTreeItem item, DamengSchemaTreeItem dbItem) {
        DamengTableOpenEvent event = new DamengTableOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    // public static void recordDelete() {
    //     EventUtil.post(new DamengRecordDeleteEvent());
    // }

    public static void tableAdded(DamengSchemaTreeItem item) {
        DamengTableAddedEvent event = new DamengTableAddedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void tableAlerted(String tableName, DamengSchemaTreeItem dbItem) {
        DamengTableAlertedEvent event = new DamengTableAlertedEvent();
        event.data(tableName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void procedureAdded(DamengSchemaTreeItem dbItem) {
        DamengProcedureAddedEvent event = new DamengProcedureAddedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void procedureAlerted(String procedureName, DamengSchemaTreeItem dbItem) {
        DamengProcedureAlertedEvent event = new DamengProcedureAlertedEvent();
        event.data(procedureName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

//    public static void eventAdded(DamengSchemaTreeItem dbItem) {
//        DamengEventAddedEvent event = new DamengEventAddedEvent();
//        event.data(dbItem);
//        EventUtil.post(event);
//    }
//
//    public static void eventAlerted(String eventName, DamengSchemaTreeItem dbItem) {
//        DamengEventAlertedEvent event = new DamengEventAlertedEvent();
//        event.data(eventName);
//        event.setDbItem(dbItem);
//        EventUtil.post(event);
//    }

    public static void functionAdded(DamengSchemaTreeItem dbItem) {
        DamengFunctionAddedEvent event = new DamengFunctionAddedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void functionAlerted(String functionName, DamengSchemaTreeItem dbItem) {
        DamengFunctionAlertedEvent event = new DamengFunctionAlertedEvent();
        event.data(functionName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void tableRenamed(DamengTableTreeItem tableItem, DamengSchemaTreeItem dbItem) {
        DamengTableRenamedEvent event = new DamengTableRenamedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void viewRenamed(DamengViewTreeItem viewItem, DamengSchemaTreeItem dbItem) {
        DamengViewRenamedEvent event = new DamengViewRenamedEvent();
        event.setDbItem(dbItem);
        event.data(viewItem);
        EventUtil.post(event);
    }

    //    public static void eventRenamed(DamengEventTreeItem viewItem, DamengSchemaTreeItem dbItem) {
    //        DamengEventRenamedEvent event = new DamengEventRenamedEvent();
    //        event.setDbItem(dbItem);
    //        event.data(viewItem);
    //        EventUtil.post(event);
    //    }

    public static void tableCleared(DamengTableTreeItem tableItem, DamengSchemaTreeItem dbItem) {
        DamengTableClearedEvent event = new DamengTableClearedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void tableFiltered(DamengTableTreeItem item, List<DamengRecordFilter> filters) {
        DamengTableFilteredEvent event = new DamengTableFilteredEvent();
        event.data(item);
        event.setFilters(filters);
        EventUtil.post(event);
    }

    public static void viewFiltered(DamengViewTreeItem item, List<DamengRecordFilter> filters) {
        DamengViewFilteredEvent event = new DamengViewFilteredEvent();
        event.data(item);
        event.setFilters(filters);
        EventUtil.post(event);
    }

    public static void tableTruncated(DamengTableTreeItem tableItem, DamengSchemaTreeItem dbItem) {
        DamengTableTruncatedEvent event = new DamengTableTruncatedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void tableDropped(DamengTableTreeItem tableItem, DamengSchemaTreeItem dbItem) {
        DamengTableDroppedEvent event = new DamengTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void schemaClosed(DamengSchemaTreeItem dbItem) {
        DamengSchemaClosedEvent event = new DamengSchemaClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void schemaAdded(DBRootTreeItem connectItem, DamengSchema schema) {
        DamengSchemaAddedEvent event = new DamengSchemaAddedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void schemaUpdated(DBRootTreeItem connectItem, DamengSchema schema) {
        DamengSchemaUpdatedEvent event = new DamengSchemaUpdatedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void schemaDropped(DamengSchemaTreeItem dbItem) {
        DamengSchemaDroppedEvent event = new DamengSchemaDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void queryAdd(DamengSchemaTreeItem item) {
        DamengQueryAddEvent event = new DamengQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryAdded(ShellQuery query, DamengSchemaTreeItem item) {
        DamengQueryAddedEvent event = new DamengQueryAddedEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryDeleted(DamengQueryTreeItem item) {
        DamengQueryDeletedEvent event = new DamengQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryOpen(ShellQuery query, DamengSchemaTreeItem item) {
        DamengQueryOpenEvent event = new DamengQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryRenamed(ShellQuery query, DamengSchemaTreeItem item) {
        DamengQueryRenamedEvent event = new DamengQueryRenamedEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void viewOpen(DamengViewTreeItem item, DamengSchemaTreeItem dbItem) {
        DamengViewOpenEvent event = new DamengViewOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designFunction(DamengFunction function, DamengSchemaTreeItem dbItem) {
        DamengFunctionDesignEvent event = new DamengFunctionDesignEvent();
        event.data(function);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designProcedure(DamengProcedure procedure, DamengSchemaTreeItem dbItem) {
        DamengProcedureDesignEvent event = new DamengProcedureDesignEvent();
        event.data(procedure);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

//    public static void designEvent(DamengEvent event, DamengSchemaTreeItem dbItem) {
//        DamengEventDesignEvent event1 = new DamengEventDesignEvent();
//        event1.data(event);
//        event1.setDbItem(dbItem);
//        EventUtil.post(event1);
//    }

    public static void viewAlerted(String viewName, DamengSchemaTreeItem dbItem) {
        DamengViewAlertedEvent event = new DamengViewAlertedEvent();
        event.data(viewName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designView(DamengView dbView, DamengSchemaTreeItem dbItem) {
        DamengViewDesignEvent event = new DamengViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void viewAdded(DamengSchemaTreeItem dbItem) {
        DamengViewAddedEvent event = new DamengViewAddedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void infoDeleted(ShellConnect info) {
        DamengConnectDeletedEvent event = new DamengConnectDeletedEvent();
        event.data(info);
        EventUtil.post(event);
    }

    public static void designTable(DamengTable table, DamengSchemaTreeItem dbItem) {
        DamengTableDesignEvent event = new DamengTableDesignEvent();
        event.data(table);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 连接关闭事件
     *
     * @param client redis客户端
     */
    public static void connectionClosed(ShellDamengClient client) {
        DBConnectionClosedEvent event = new DBConnectionClosedEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 连接成功事件
     *
     * @param client redis客户端
     */
    public static void connectionConnected(ShellDamengClient client) {
        DBConnectionConnectedEvent event = new DBConnectionConnectedEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 终端打开事件
     */
    public static void terminalOpen(ShellDamengClient client, String schema) {
        DBTerminalOpenEvent event = new DBTerminalOpenEvent();
        event.data(client);
        event.setSchema(schema);
        EventUtil.post(event);
    }

    /**
     * 终端关闭事件
     *
     * @param client dameng客户端
     */
    public static void terminalClose(ShellDamengClient client) {
        DBTerminalCloseEvent event = new DBTerminalCloseEvent();
        event.data(client);
        EventUtil.post(event);
    }

    //    /**
    //     * 树节点变化事件
    //     */
    //    public static void treeChildChanged() {
    //        EventUtil.postDelay(new TreeChildChangedEvent(), 100);
    //    }

    // public static void recordDelete(DamengRecord record) {
    //     RecordDeleteEvent event = new RecordDeleteEvent();
    //     event.data(record);
    //     EventUtil.post(event);
    // }

    /**
     * 连接已修改事件
     *
     * @param connect DB信息
     */
    public static void connectUpdated(ShellConnect connect) {
        DamengConnectUpdatedEvent event = new DamengConnectUpdatedEvent();
        event.data(connect);
        EventUtil.post(event);
    }

    public static void addConnect() {
        EventUtil.post(new DBAddConnectEvent());
    }

    public static void addGroup() {
        EventUtil.post(new DBAddGroupEvent());
    }

    public static void changelog() {
        EventUtil.post(new ChangelogEvent());
    }

    public static void connectAdded(ShellConnect connect) {
        DamengConnectAddedEvent event = new DamengConnectAddedEvent();
        event.data(connect);
        EventUtil.post(event);
    }

    public static void connectDeleted(ShellConnect connect) {
        DamengConnectDeletedEvent event = new DamengConnectDeletedEvent();
        event.data(connect);
        EventUtil.post(event);
    }

    /**
     * 布局1
     */
    public static void layout1() {
        EventUtil.post(new Layout1Event());
    }

    /**
     * 布局2
     */
    public static void layout2() {
        EventUtil.post(new Layout2Event());
    }

    /**
     * 节点选中事件
     *
     * @param item 节点
     */
    public static void treeItemChanged(TreeItem<?> item) {
        DamengTreeItemChangedEvent event = new DamengTreeItemChangedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 显示消息页面
     */
    public static void showMessage() {
        EventUtil.post(new ShellShowMessageEvent());
    }

    public static void printSql(String sql, ShellConnect connect) {
        ShellPrintSqlEvent event = new ShellPrintSqlEvent();
        event.data(sql);
        event.setConnect(connect);
        EventUtil.post(event);
    }
}
