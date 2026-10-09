package cn.oyzh.easyshell.event.mariadb;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mariadb.database.ShellMariadbDatabaseAddedEvent;
import cn.oyzh.easyshell.event.mariadb.database.ShellMariadbDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mariadb.database.ShellMariadbDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.database.ShellMariadbDatabaseUpdatedEvent;
import cn.oyzh.easyshell.event.mariadb.event.ShellMariadbEventDesignEvent;
import cn.oyzh.easyshell.event.mariadb.event.ShellMariadbEventDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.event.ShellMariadbEventRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.function.ShellMariadbFunctionDesignEvent;
import cn.oyzh.easyshell.event.mariadb.function.ShellMariadbFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.function.ShellMariadbFunctionRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.procedure.ShellMariadbProcedureDesignEvent;
import cn.oyzh.easyshell.event.mariadb.procedure.ShellMariadbProcedureDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.procedure.ShellMariadbProcedureRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.query.ShellMariadbQueryAddEvent;
import cn.oyzh.easyshell.event.mariadb.query.ShellMariadbQueryDeletedEvent;
import cn.oyzh.easyshell.event.mariadb.query.ShellMariadbQueryOpenEvent;
import cn.oyzh.easyshell.event.mariadb.query.ShellMariadbQueryRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableAlertedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableClearedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableDesignEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableOpenEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableTruncatedEvent;
import cn.oyzh.easyshell.event.mariadb.terminal.ShellMariadbTerminalOpenEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewAlertedEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewDesignEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewOpenEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewRenamedEvent;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.event.ShellMariadbEventTreeItem;
import cn.oyzh.easyshell.trees.mariadb.function.ShellMariadbFunctionTreeItem;
import cn.oyzh.easyshell.trees.mariadb.procedure.ShellMariadbProcedureTreeItem;
import cn.oyzh.easyshell.trees.mariadb.query.ShellMariadbQueryTreeItem;
import cn.oyzh.easyshell.trees.mariadb.root.ShellMariadbRootTreeItem;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTableTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * MariaDB事件工具
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEventUtil {

    /**
     * 表打开事件
     *
     * @param item   表节点
     * @param dbItem 数据库节点
     */
    public static void tableOpen(ShellMariadbTableTreeItem item, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbTableOpenEvent event = new ShellMariadbTableOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 表结构变更事件
     *
     * @param tableName 表名称
     * @param dbItem    数据库节点
     */
    public static void tableAlerted(String tableName, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbTableAlertedEvent event = new ShellMariadbTableAlertedEvent();
        event.data(tableName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 表重命名事件
     *
     * @param tableName    表名称
     * @param newTableName 新表名称
     * @param dbItem       数据库节点
     */
    public static void tableRenamed(String tableName, String newTableName, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbTableRenamedEvent event = new ShellMariadbTableRenamedEvent();
        event.setDbItem(dbItem);
        event.data(tableName);
        event.setNewTableName(newTableName);
        EventUtil.post(event);
    }

    /**
     * 视图重命名事件
     *
     * @param viewName    视图名称
     * @param newViewName 新视图名称
     * @param dbItem      数据库节点
     */
    public static void viewRenamed(String viewName, String newViewName, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbViewRenamedEvent event = new ShellMariadbViewRenamedEvent();
        event.setDbItem(dbItem);
        event.data(viewName);
        event.setNewViewName(newViewName);
        EventUtil.post(event);
    }

    /**
     * 事件重命名事件
     *
     * @param eventName    事件名称
     * @param newEventName 新事件名称
     * @param dbItem       数据库节点
     */
    public static void eventRenamed(String eventName, String newEventName, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbEventRenamedEvent event = new ShellMariadbEventRenamedEvent();
        event.setDbItem(dbItem);
        event.data(eventName);
        event.setNewEventName(newEventName);
        EventUtil.post(event);
    }

    /**
     * 函数重命名事件
     *
     * @param functionName    函数名称
     * @param newFunctionName 新函数名称
     * @param dbItem          数据库节点
     */
    public static void functionRenamed(String functionName, String newFunctionName, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbFunctionRenamedEvent event = new ShellMariadbFunctionRenamedEvent();
        event.setDbItem(dbItem);
        event.data(functionName);
        event.setNewFunctionName(newFunctionName);
        EventUtil.post(event);
    }

    /**
     * 存储过程重命名事件
     *
     * @param procedureName    存储过程名称
     * @param newProcedureName 新存储过程名称
     * @param dbItem           数据库节点
     */
    public static void procedureRenamed(String procedureName, String newProcedureName, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbProcedureRenamedEvent event = new ShellMariadbProcedureRenamedEvent();
        event.setDbItem(dbItem);
        event.data(procedureName);
        event.setNewProcedureName(newProcedureName);
        EventUtil.post(event);
    }

    /**
     * 表清空事件
     *
     * @param tableItem 表节点
     * @param dbItem    数据库节点
     */
    public static void tableCleared(ShellMariadbTableTreeItem tableItem, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbTableClearedEvent event = new ShellMariadbTableClearedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    /**
     * 表截断事件
     *
     * @param tableItem 表节点
     * @param dbItem    数据库节点
     */
    public static void tableTruncated(ShellMariadbTableTreeItem tableItem, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbTableTruncatedEvent event = new ShellMariadbTableTruncatedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    /**
     * 表删除事件
     *
     * @param tableItem 表节点
     * @param dbItem    数据库节点
     */
    public static void tableDropped(ShellMariadbTableTreeItem tableItem, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbTableDroppedEvent event = new ShellMariadbTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    /**
     * 数据库关闭事件
     *
     * @param dbItem 数据库节点
     */
    public static void databaseClosed(ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbDatabaseClosedEvent event = new ShellMariadbDatabaseClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 数据库新增事件
     *
     * @param connectItem 连接节点
     * @param database    数据库
     */
    public static void databaseAdded(ShellMariadbRootTreeItem connectItem, MariadbDatabase database) {
        ShellMariadbDatabaseAddedEvent event = new ShellMariadbDatabaseAddedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 数据库更新事件
     *
     * @param connectItem 连接节点
     * @param database    数据库
     */
    public static void databaseUpdated(ShellMariadbRootTreeItem connectItem, MariadbDatabase database) {
        ShellMariadbDatabaseUpdatedEvent event = new ShellMariadbDatabaseUpdatedEvent();
        event.data(database);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 数据库删除事件
     *
     * @param dbItem 数据库节点
     */
    public static void databaseDropped(ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbDatabaseDroppedEvent event = new ShellMariadbDatabaseDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 查询新增事件
     *
     * @param item 数据库节点
     */
    public static void queryAdd(ShellMariadbDatabaseTreeItem item) {
        ShellMariadbQueryAddEvent event = new ShellMariadbQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询删除事件
     *
     * @param item 查询节点
     */
    public static void queryDeleted(ShellMariadbQueryTreeItem item) {
        ShellMariadbQueryDeletedEvent event = new ShellMariadbQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询打开事件
     *
     * @param query 查询
     * @param item  数据库节点
     */
    public static void queryOpen(ShellQuery query, ShellMariadbDatabaseTreeItem item) {
        ShellMariadbQueryOpenEvent event = new ShellMariadbQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    /**
     * 查询重命名事件
     *
     * @param item      查询节点
     * @param queryName 查询名称
     */
    public static void queryRenamed(ShellMariadbQueryTreeItem item, String queryName) {
        ShellMariadbQueryRenamedEvent event = new ShellMariadbQueryRenamedEvent();
        event.data(item);
        event.setQueryName(queryName);
        EventUtil.post(event);
    }

    /**
     * 视图打开事件
     *
     * @param item   视图节点
     * @param dbItem 数据库节点
     */
    public static void viewOpen(ShellMariadbViewTreeItem item, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbViewOpenEvent event = new ShellMariadbViewOpenEvent();
        event.data(item);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 函数设计事件
     *
     * @param function 函数
     * @param dbItem   数据库节点
     */
    public static void designFunction(MariadbFunction function, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbFunctionDesignEvent event = new ShellMariadbFunctionDesignEvent();
        event.data(function);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 存储过程设计事件
     *
     * @param procedure 存储过程
     * @param dbItem    数据库节点
     */
    public static void designProcedure(MariadbProcedure procedure, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbProcedureDesignEvent event = new ShellMariadbProcedureDesignEvent();
        event.data(procedure);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 事件设计事件
     *
     * @param event  事件
     * @param dbItem 数据库节点
     */
    public static void designEvent(MariadbEvent event, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbEventDesignEvent event1 = new ShellMariadbEventDesignEvent();
        event1.data(event);
        event1.setDbItem(dbItem);
        EventUtil.post(event1);
    }

    /**
     * 视图变更事件
     *
     * @param viewName 视图名称
     * @param dbItem   数据库节点
     */
    public static void viewAlerted(String viewName, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbViewAlertedEvent event = new ShellMariadbViewAlertedEvent();
        event.data(viewName);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 视图设计事件
     *
     * @param dbView 视图
     * @param dbItem 数据库节点
     */
    public static void designView(MariadbView dbView, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbViewDesignEvent event = new ShellMariadbViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 视图删除事件
     *
     * @param treeItem 视图节点
     */
    public static void dropView(ShellMariadbViewTreeItem treeItem) {
        ShellMariadbViewDroppedEvent event = new ShellMariadbViewDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 函数删除事件
     *
     * @param treeItem 函数节点
     */
    public static void dropFunction(ShellMariadbFunctionTreeItem treeItem) {
        ShellMariadbFunctionDroppedEvent event = new ShellMariadbFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 存储过程删除事件
     *
     * @param treeItem 存储过程节点
     */
    public static void dropProcedure(ShellMariadbProcedureTreeItem treeItem) {
        ShellMariadbProcedureDroppedEvent event = new ShellMariadbProcedureDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 事件删除事件
     *
     * @param treeItem 事件节点
     */
    public static void dropEvent(ShellMariadbEventTreeItem treeItem) {
        ShellMariadbEventDroppedEvent event = new ShellMariadbEventDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 表设计事件
     *
     * @param table  表
     * @param dbItem 数据库节点
     */
    public static void designTable(MariadbTable table, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbTableDesignEvent event = new ShellMariadbTableDesignEvent();
        event.data(table);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    //    public static void printSql(String sql, ShellConnect connect) {
    //        ShellPrintSqlEvent event = new ShellPrintSqlEvent();
    //        event.data(sql);
    //        event.setConnect(connect);
    //        EventUtil.post(event);
    //    }

    /**
     * 终端打开事件
     *
     * @param dbItem 数据库节点
     */
    public static void terminalOpen(ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbTerminalOpenEvent event = new ShellMariadbTerminalOpenEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

}
