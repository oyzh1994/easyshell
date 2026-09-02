package cn.oyzh.easyshell.event.dameng;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.dameng.function.ShellDamengFunctionDesignEvent;
import cn.oyzh.easyshell.event.dameng.function.ShellDamengFunctionDroppedEvent;
import cn.oyzh.easyshell.event.dameng.function.ShellDamengFunctionRenamedEvent;
import cn.oyzh.easyshell.event.dameng.procedure.ShellDamengProcedureDesignEvent;
import cn.oyzh.easyshell.event.dameng.procedure.ShellDamengProcedureDroppedEvent;
import cn.oyzh.easyshell.event.dameng.procedure.ShellDamengProcedureRenamedEvent;
import cn.oyzh.easyshell.event.dameng.query.ShellDamengQueryAddEvent;
import cn.oyzh.easyshell.event.dameng.query.ShellDamengQueryDeletedEvent;
import cn.oyzh.easyshell.event.dameng.query.ShellDamengQueryOpenEvent;
import cn.oyzh.easyshell.event.dameng.query.ShellDamengQueryRenamedEvent;
import cn.oyzh.easyshell.event.dameng.schema.ShellDamengSchemaAddedEvent;
import cn.oyzh.easyshell.event.dameng.schema.ShellDamengSchemaClosedEvent;
import cn.oyzh.easyshell.event.dameng.schema.ShellDamengSchemaDroppedEvent;
import cn.oyzh.easyshell.event.dameng.schema.ShellDamengSchemaUpdatedEvent;
import cn.oyzh.easyshell.event.dameng.sql.ShellPrintSqlEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableAlertedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableClearedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableDesignEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableDroppedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableOpenEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableRenamedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableTruncatedEvent;
import cn.oyzh.easyshell.event.dameng.terminal.ShellDamengTerminalOpenEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewAlertedEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewDesignEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewDroppedEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewOpenEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewRenamedEvent;
import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.ShellDamengProcedureTreeItem;
import cn.oyzh.easyshell.trees.dameng.query.ShellDamengQueryTreeItem;
import cn.oyzh.easyshell.trees.dameng.root.ShellDamengRootTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTableTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * redis事件工具
 *
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellDamengEventUtil {

    public static void tableOpen(ShellDamengTableTreeItem item, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableOpenEvent event = new ShellDamengTableOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void tableAlerted(String tableName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableAlertedEvent event = new ShellDamengTableAlertedEvent();
        event.data(tableName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void tableRenamed(String tableName, String newTableName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableRenamedEvent event = new ShellDamengTableRenamedEvent();
        event.setDbItem(dbItem);
        event.data(tableName);
        event.setNewTableName(newTableName);
        EventUtil.post(event);
    }

    public static void viewRenamed(String viewName, String newViewName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengViewRenamedEvent event = new ShellDamengViewRenamedEvent();
        event.setDbItem(dbItem);
        event.data(viewName);
        event.setNewViewName(newViewName);
        EventUtil.post(event);
    }

    public static void functionRenamed(String functionName, String newFunctionName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengFunctionRenamedEvent event = new ShellDamengFunctionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(functionName);
        event.setNewFunctionName(newFunctionName);
        EventUtil.post(event);
    }

    public static void procedureRenamed(String procedureName, String newProcedureName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengProcedureRenamedEvent event = new ShellDamengProcedureRenamedEvent();
        event.setDbItem(dbItem);
        event.data(procedureName);
        event.setNewProcedureName(newProcedureName);
        EventUtil.post(event);
    }

    public static void tableCleared(ShellDamengTableTreeItem tableItem, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableClearedEvent event = new ShellDamengTableClearedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void tableTruncated(ShellDamengTableTreeItem tableItem, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableTruncatedEvent event = new ShellDamengTableTruncatedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void tableDropped(ShellDamengTableTreeItem tableItem, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableDroppedEvent event = new ShellDamengTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    public static void schemaClosed(ShellDamengSchemaTreeItem dbItem) {
        ShellDamengSchemaClosedEvent event = new ShellDamengSchemaClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void schemaAdded(ShellDamengRootTreeItem connectItem, DamengSchema schema) {
        ShellDamengSchemaAddedEvent event = new ShellDamengSchemaAddedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void schemaUpdated(ShellDamengRootTreeItem connectItem, DamengSchema schema) {
        ShellDamengSchemaUpdatedEvent event = new ShellDamengSchemaUpdatedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    public static void schemaDropped(ShellDamengSchemaTreeItem dbItem) {
        ShellDamengSchemaDroppedEvent event = new ShellDamengSchemaDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void queryAdd(ShellDamengSchemaTreeItem item) {
        ShellDamengQueryAddEvent event = new ShellDamengQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryDeleted(ShellDamengQueryTreeItem item) {
        ShellDamengQueryDeletedEvent event = new ShellDamengQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    public static void queryOpen(ShellQuery query, ShellDamengSchemaTreeItem item) {
        ShellDamengQueryOpenEvent event = new ShellDamengQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void queryRenamed(String queryId, String queryName, String newQueryName, ShellDamengSchemaTreeItem item) {
        ShellDamengQueryRenamedEvent event = new ShellDamengQueryRenamedEvent();
        event.data(queryId);
        event.data(queryName);
        event.data(newQueryName);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    public static void viewOpen(ShellDamengViewTreeItem item, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengViewOpenEvent event = new ShellDamengViewOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designFunction(DamengFunction function, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengFunctionDesignEvent event = new ShellDamengFunctionDesignEvent();
        event.data(function);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designProcedure(DamengProcedure procedure, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengProcedureDesignEvent event = new ShellDamengProcedureDesignEvent();
        event.data(procedure);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void viewAlerted(String viewName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengViewAlertedEvent event = new ShellDamengViewAlertedEvent();
        event.data(viewName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void designView(DamengView dbView, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengViewDesignEvent event = new ShellDamengViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void dropView(ShellDamengViewTreeItem treeItem) {
        ShellDamengViewDroppedEvent event = new ShellDamengViewDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void dropFunction(ShellDamengFunctionTreeItem treeItem) {
        ShellDamengFunctionDroppedEvent event = new ShellDamengFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void dropProcedure(ShellDamengProcedureTreeItem treeItem) {
        ShellDamengProcedureDroppedEvent event = new ShellDamengProcedureDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void designTable(DamengTable table, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableDesignEvent event = new ShellDamengTableDesignEvent();
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

    public static void terminalOpen(ShellDamengClient client, String dbName) {
        ShellDamengTerminalOpenEvent event = new ShellDamengTerminalOpenEvent();
        event.data(client);
        event.setSchema(dbName);
        EventUtil.post(event);
    }

}
