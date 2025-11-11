package cn.oyzh.easyshell.event.mysql;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseAddedEvent;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseUpdatedEvent;
import cn.oyzh.easyshell.event.mysql.event.ShellMysqlEventDesignEvent;
import cn.oyzh.easyshell.event.mysql.event.ShellMysqlEventDroppedEvent;
import cn.oyzh.easyshell.event.mysql.event.ShellMysqlEventRenamedEvent;
import cn.oyzh.easyshell.event.mysql.function.ShellMysqlFunctionDesignEvent;
import cn.oyzh.easyshell.event.mysql.function.ShellMysqlFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mysql.procedure.ShellMysqlProcedureDesignEvent;
import cn.oyzh.easyshell.event.mysql.procedure.ShellMysqlProcedureDroppedEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryAddEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryDeletedEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryOpenEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryRenamedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableAlertedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableClearedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableDesignEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableDroppedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableOpenEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableRenamedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableTruncatedEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewAlertedEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewDesignEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewDroppedEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewOpenEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewRenamedEvent;
import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.MysqlEventTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.MysqlFunctionTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.MysqlProcedureTreeItem;
import cn.oyzh.easyshell.trees.mysql.query.MysqlQueryTreeItem;
import cn.oyzh.easyshell.trees.mysql.root.MysqlRootTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.MysqlTableTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.MysqlViewTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * redis事件工具
 *
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellMysqlEventUtil {

    public static void tableOpen(MysqlTableTreeItem item, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableOpenEvent event = new ShellMysqlTableOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void tableAlerted(String tableName, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableAlertedEvent event = new ShellMysqlTableAlertedEvent();
        event.data(tableName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void tableRenamed(MysqlTableTreeItem tableItem, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableRenamedEvent event = new ShellMysqlTableRenamedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void viewRenamed(MysqlViewTreeItem viewItem, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlViewRenamedEvent event = new ShellMysqlViewRenamedEvent();
        event.setDbItem(dbItem);
        event.data(viewItem);
        EventUtil.post(event);
    }

    public static void eventRenamed(MysqlEventTreeItem viewItem, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlEventRenamedEvent event = new ShellMysqlEventRenamedEvent();
        event.setDbItem(dbItem);
        event.data(viewItem);
        EventUtil.post(event);
    }

    public static void tableCleared(MysqlTableTreeItem tableItem, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableClearedEvent event = new ShellMysqlTableClearedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void tableTruncated(MysqlTableTreeItem tableItem, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableTruncatedEvent event = new ShellMysqlTableTruncatedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void tableDropped(MysqlTableTreeItem tableItem, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableDroppedEvent event = new ShellMysqlTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void databaseClosed(MysqlDatabaseTreeItem dbItem) {
        ShellMysqlDatabaseClosedEvent event = new ShellMysqlDatabaseClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void databaseAdded(MysqlRootTreeItem connectItem, MysqlDatabase database) {
        ShellMysqlDatabaseAddedEvent event = new ShellMysqlDatabaseAddedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void databaseUpdated(MysqlRootTreeItem connectItem, MysqlDatabase database) {
        ShellMysqlDatabaseUpdatedEvent event = new ShellMysqlDatabaseUpdatedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void databaseDropped(MysqlDatabaseTreeItem dbItem) {
        ShellMysqlDatabaseDroppedEvent event = new ShellMysqlDatabaseDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void queryAdd(MysqlDatabaseTreeItem item) {
        ShellMysqlQueryAddEvent event = new ShellMysqlQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryDeleted(MysqlQueryTreeItem item) {
        ShellMysqlQueryDeletedEvent event = new ShellMysqlQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryOpen(ShellQuery query, MysqlDatabaseTreeItem item) {
        ShellMysqlQueryOpenEvent event = new ShellMysqlQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryRenamed(ShellQuery query, MysqlDatabaseTreeItem item) {
        ShellMysqlQueryRenamedEvent event = new ShellMysqlQueryRenamedEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void viewOpen(MysqlViewTreeItem item, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlViewOpenEvent event = new ShellMysqlViewOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designFunction(MysqlFunction function, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlFunctionDesignEvent event = new ShellMysqlFunctionDesignEvent();
        event.data(function);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designProcedure(MysqlProcedure procedure, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlProcedureDesignEvent event = new ShellMysqlProcedureDesignEvent();
        event.data(procedure);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designEvent(MysqlEvent event, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlEventDesignEvent event1 = new ShellMysqlEventDesignEvent();
        event1.data(event);
        event1.setDbItem(dbItem);
        EventUtil.post(event1);
    }

    public static void viewAlerted(String viewName, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlViewAlertedEvent event = new ShellMysqlViewAlertedEvent();
        event.data(viewName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designView(MysqlView dbView, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlViewDesignEvent event = new ShellMysqlViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void dropView(MysqlViewTreeItem treeItem) {
        ShellMysqlViewDroppedEvent event = new ShellMysqlViewDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void dropFunction(MysqlFunctionTreeItem treeItem) {
        ShellMysqlFunctionDroppedEvent event = new ShellMysqlFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void dropProcedure(MysqlProcedureTreeItem treeItem) {
        ShellMysqlProcedureDroppedEvent event = new ShellMysqlProcedureDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void dropEvent(MysqlEventTreeItem treeItem) {
        ShellMysqlEventDroppedEvent event = new ShellMysqlEventDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void designTable(MysqlTable table, MysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableDesignEvent event = new ShellMysqlTableDesignEvent();
        event.data(table);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

}
