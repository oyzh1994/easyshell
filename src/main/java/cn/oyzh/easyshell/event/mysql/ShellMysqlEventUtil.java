package cn.oyzh.easyshell.event.mysql;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseAddedEvent;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseUpdatedEvent;
import cn.oyzh.easyshell.event.mysql.event.ShellMysqlEventDesignEvent;
import cn.oyzh.easyshell.event.mysql.event.ShellMysqlEventDroppedEvent;
import cn.oyzh.easyshell.event.mysql.event.ShellMysqlEventRenamedEvent;
import cn.oyzh.easyshell.event.mysql.function.ShellMysqlFunctionDesignEvent;
import cn.oyzh.easyshell.event.mysql.function.ShellMysqlFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mysql.function.ShellMysqlFunctionRenamedEvent;
import cn.oyzh.easyshell.event.mysql.procedure.ShellMysqlProcedureDesignEvent;
import cn.oyzh.easyshell.event.mysql.procedure.ShellMysqlProcedureDroppedEvent;
import cn.oyzh.easyshell.event.mysql.procedure.ShellMysqlProcedureRenamedEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryAddEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryDeletedEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryOpenEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryRenamedEvent;
import cn.oyzh.easyshell.event.mysql.sql.ShellPrintSqlEvent;
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
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.ShellMysqlEventTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.ShellMysqlFunctionTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.ShellMysqlProcedureTreeItem;
import cn.oyzh.easyshell.trees.mysql.query.ShellMysqlQueryTreeItem;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTableTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * redis事件工具
 *
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellMysqlEventUtil {

    public static void tableOpen(ShellMysqlTableTreeItem item, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableOpenEvent event = new ShellMysqlTableOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void tableAlerted(String tableName, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableAlertedEvent event = new ShellMysqlTableAlertedEvent();
        event.data(tableName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void tableRenamed(String tableName, String newTableName, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableRenamedEvent event = new ShellMysqlTableRenamedEvent();
        event.setDbItem(dbItem);
        event.data(tableName);
        event.setNewTableName(newTableName);
        EventUtil.post(event);
    }

    public static void viewRenamed(String viewName, String newViewName, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlViewRenamedEvent event = new ShellMysqlViewRenamedEvent();
        event.setDbItem(dbItem);
        event.data(viewName);
        event.setNewViewName(newViewName);
        EventUtil.post(event);
    }

    public static void eventRenamed(String eventName, String newEventName, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlEventRenamedEvent event = new ShellMysqlEventRenamedEvent();
        event.setDbItem(dbItem);
        event.data(eventName);
        event.setNewEventName(newEventName);
        EventUtil.post(event);
    }

    public static void functionRenamed(String functionName, String newFunctionName, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlFunctionRenamedEvent event = new ShellMysqlFunctionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(functionName);
        event.setNewFunctionName(newFunctionName);
        EventUtil.post(event);
    }

    public static void procedureRenamed(String procedureName, String newProcedureName, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlProcedureRenamedEvent event = new ShellMysqlProcedureRenamedEvent();
        event.setDbItem(dbItem);
        event.data(procedureName);
        event.setNewProcedureName(newProcedureName);
        EventUtil.post(event);
    }

    public static void tableCleared(ShellMysqlTableTreeItem tableItem, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableClearedEvent event = new ShellMysqlTableClearedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void tableTruncated(ShellMysqlTableTreeItem tableItem, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableTruncatedEvent event = new ShellMysqlTableTruncatedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void tableDropped(ShellMysqlTableTreeItem tableItem, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableDroppedEvent event = new ShellMysqlTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void databaseClosed(ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlDatabaseClosedEvent event = new ShellMysqlDatabaseClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void databaseAdded(ShellMysqlRootTreeItem connectItem, MysqlDatabase database) {
        ShellMysqlDatabaseAddedEvent event = new ShellMysqlDatabaseAddedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void databaseUpdated(ShellMysqlRootTreeItem connectItem, MysqlDatabase database) {
        ShellMysqlDatabaseUpdatedEvent event = new ShellMysqlDatabaseUpdatedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void databaseDropped(ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlDatabaseDroppedEvent event = new ShellMysqlDatabaseDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void queryAdd(ShellMysqlDatabaseTreeItem item) {
        ShellMysqlQueryAddEvent event = new ShellMysqlQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryDeleted(ShellMysqlQueryTreeItem item) {
        ShellMysqlQueryDeletedEvent event = new ShellMysqlQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryOpen(ShellQuery query, ShellMysqlDatabaseTreeItem item) {
        ShellMysqlQueryOpenEvent event = new ShellMysqlQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryRenamed(ShellQuery query, ShellMysqlDatabaseTreeItem item) {
        ShellMysqlQueryRenamedEvent event = new ShellMysqlQueryRenamedEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void viewOpen(ShellMysqlViewTreeItem item, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlViewOpenEvent event = new ShellMysqlViewOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designFunction(MysqlFunction function, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlFunctionDesignEvent event = new ShellMysqlFunctionDesignEvent();
        event.data(function);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designProcedure(MysqlProcedure procedure, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlProcedureDesignEvent event = new ShellMysqlProcedureDesignEvent();
        event.data(procedure);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designEvent(MysqlEvent event, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlEventDesignEvent event1 = new ShellMysqlEventDesignEvent();
        event1.data(event);
        event1.setDbItem(dbItem);
        EventUtil.post(event1);
    }

    public static void viewAlerted(String viewName, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlViewAlertedEvent event = new ShellMysqlViewAlertedEvent();
        event.data(viewName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designView(MysqlView dbView, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlViewDesignEvent event = new ShellMysqlViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void dropView(ShellMysqlViewTreeItem treeItem) {
        ShellMysqlViewDroppedEvent event = new ShellMysqlViewDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void dropFunction(ShellMysqlFunctionTreeItem treeItem) {
        ShellMysqlFunctionDroppedEvent event = new ShellMysqlFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void dropProcedure(ShellMysqlProcedureTreeItem treeItem) {
        ShellMysqlProcedureDroppedEvent event = new ShellMysqlProcedureDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void dropEvent(ShellMysqlEventTreeItem treeItem) {
        ShellMysqlEventDroppedEvent event = new ShellMysqlEventDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void designTable(MysqlTable table, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlTableDesignEvent event = new ShellMysqlTableDesignEvent();
        event.data(table);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void printSql(String sql, ShellConnect connect) {
        ShellPrintSqlEvent event = new ShellPrintSqlEvent();
        event.data(sql);
        event.setConnect(connect);
        EventUtil.post(event);
    }

}
