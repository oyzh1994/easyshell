package cn.oyzh.easyshell.event.sqlserver;

import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.sqlserver.function.ShellSqlServerFunctionDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.function.ShellSqlServerFunctionDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.function.ShellSqlServerFunctionRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.procedure.ShellSqlServerProcedureDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.procedure.ShellSqlServerProcedureDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.procedure.ShellSqlServerProcedureRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.query.ShellSqlServerQueryAddEvent;
import cn.oyzh.easyshell.event.sqlserver.query.ShellSqlServerQueryDeletedEvent;
import cn.oyzh.easyshell.event.sqlserver.query.ShellSqlServerQueryOpenEvent;
import cn.oyzh.easyshell.event.sqlserver.query.ShellSqlServerQueryRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.schema.ShellSqlServerSchemaAddedEvent;
import cn.oyzh.easyshell.event.sqlserver.schema.ShellSqlServerSchemaClosedEvent;
import cn.oyzh.easyshell.event.sqlserver.schema.ShellSqlServerSchemaDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.schema.ShellSqlServerSchemaUpdatedEvent;
import cn.oyzh.easyshell.event.sqlserver.sequence.ShellSqlServerSequenceDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.sequence.ShellSqlServerSequenceDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.sequence.ShellSqlServerSequenceRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableAlertedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableClearedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableOpenEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableTruncatedEvent;
import cn.oyzh.easyshell.event.sqlserver.terminal.ShellSqlServerTerminalOpenEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewAlertedEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewOpenEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewRenamedEvent;
import cn.oyzh.easyshell.trees.sqlserver.function.ShellSqlServerFunctionTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.procedure.ShellSqlServerProcedureTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.query.ShellSqlServerQueryTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.root.ShellSqlServerRootTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.sequence.ShellSqlServerSequenceTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.table.ShellSqlServerTableTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * SqlServer数据库事件工具类，用于发布SqlServer相关事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerEventUtil {

    /**
     * 表打开事件
     *
     * @param item   表节点
     * @param dbItem 数据库节点
     */
    public static void tableOpen(ShellSqlServerTableTreeItem item, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerTableOpenEvent event = new ShellSqlServerTableOpenEvent();
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
    public static void tableAlerted(String tableName, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerTableAlertedEvent event = new ShellSqlServerTableAlertedEvent();
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
    public static void tableRenamed(String tableName, String newTableName, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerTableRenamedEvent event = new ShellSqlServerTableRenamedEvent();
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
    public static void viewRenamed(String viewName, String newViewName, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerViewRenamedEvent event = new ShellSqlServerViewRenamedEvent();
        event.setDbItem(dbItem);
        event.data(viewName);
        event.setNewViewName(newViewName);
        EventUtil.post(event);
    }

    /**
     * 函数重命名事件
     *
     * @param functionName    函数名称
     * @param newFunctionName 新函数名称
     * @param dbItem          数据库节点
     */
    public static void functionRenamed(String functionName, String newFunctionName, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerFunctionRenamedEvent event = new ShellSqlServerFunctionRenamedEvent();
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
    public static void procedureRenamed(String procedureName, String newProcedureName, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerProcedureRenamedEvent event = new ShellSqlServerProcedureRenamedEvent();
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
    public static void tableCleared(ShellSqlServerTableTreeItem tableItem, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerTableClearedEvent event = new ShellSqlServerTableClearedEvent();
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
    public static void tableTruncated(ShellSqlServerTableTreeItem tableItem, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerTableTruncatedEvent event = new ShellSqlServerTableTruncatedEvent();
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
    public static void tableDropped(ShellSqlServerTableTreeItem tableItem, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerTableDroppedEvent event = new ShellSqlServerTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    /**
     * 模式关闭事件
     *
     * @param dbItem 数据库节点
     */
    public static void schemaClosed(ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerSchemaClosedEvent event = new ShellSqlServerSchemaClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 模式新增事件
     *
     * @param connectItem 连接节点
     * @param schema      模式
     */
    public static void schemaAdded(ShellSqlServerRootTreeItem connectItem, SqlServerSchema schema) {
        ShellSqlServerSchemaAddedEvent event = new ShellSqlServerSchemaAddedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 模式更新事件
     *
     * @param connectItem 连接节点
     * @param schema      模式
     */
    public static void schemaUpdated(ShellSqlServerRootTreeItem connectItem, SqlServerSchema schema) {
        ShellSqlServerSchemaUpdatedEvent event = new ShellSqlServerSchemaUpdatedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 模式删除事件
     *
     * @param dbItem 数据库节点
     */
    public static void schemaDropped(ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerSchemaDroppedEvent event = new ShellSqlServerSchemaDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 查询新增事件
     *
     * @param item 数据库节点
     */
    public static void queryAdd(ShellSqlServerSchemaTreeItem item) {
        ShellSqlServerQueryAddEvent event = new ShellSqlServerQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询删除事件
     *
     * @param item 查询节点
     */
    public static void queryDeleted(ShellSqlServerQueryTreeItem item) {
        ShellSqlServerQueryDeletedEvent event = new ShellSqlServerQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询打开事件
     *
     * @param query 查询
     * @param item  数据库节点
     */
    public static void queryOpen(ShellQuery query, ShellSqlServerSchemaTreeItem item) {
        ShellSqlServerQueryOpenEvent event = new ShellSqlServerQueryOpenEvent();
        event.data(query);
        event.setDbItem(item);
        EventUtil.post(event);
    }

    /**
     * 查询重命名事件
     *
     * @param item    查询阶段
     * @param queryName 查询名称
     */
    public static void queryRenamed(ShellSqlServerQueryTreeItem item, String queryName) {
        ShellSqlServerQueryRenamedEvent event = new ShellSqlServerQueryRenamedEvent();
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
    public static void viewOpen(ShellSqlServerViewTreeItem item, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerViewOpenEvent event = new ShellSqlServerViewOpenEvent();
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
    public static void designFunction(SqlServerFunction function, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerFunctionDesignEvent event = new ShellSqlServerFunctionDesignEvent();
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
    public static void designProcedure(SqlServerProcedure procedure, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerProcedureDesignEvent event = new ShellSqlServerProcedureDesignEvent();
        event.data(procedure);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 视图变更事件
     *
     * @param viewName 视图名称
     * @param dbItem   数据库节点
     */
    public static void viewAlerted(String viewName, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerViewAlertedEvent event = new ShellSqlServerViewAlertedEvent();
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
    public static void designView(SqlServerView dbView, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerViewDesignEvent event = new ShellSqlServerViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 视图删除事件
     *
     * @param treeItem 视图节点
     */
    public static void dropView(ShellSqlServerViewTreeItem treeItem) {
        ShellSqlServerViewDroppedEvent event = new ShellSqlServerViewDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 函数删除事件
     *
     * @param treeItem 函数节点
     */
    public static void dropFunction(ShellSqlServerFunctionTreeItem treeItem) {
        ShellSqlServerFunctionDroppedEvent event = new ShellSqlServerFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 存储过程删除事件
     *
     * @param treeItem 存储过程节点
     */
    public static void dropProcedure(ShellSqlServerProcedureTreeItem treeItem) {
        ShellSqlServerProcedureDroppedEvent event = new ShellSqlServerProcedureDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 表设计事件
     *
     * @param table  表
     * @param dbItem 数据库节点
     */
    public static void designTable(SqlServerTable table, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerTableDesignEvent event = new ShellSqlServerTableDesignEvent();
        event.data(table);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 终端打开事件
     *
     * @param dbItem 数据库节点
     */
    public static void terminalOpen(ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerTerminalOpenEvent event = new ShellSqlServerTerminalOpenEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void designSequence(SqlServerSequence sequence, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerSequenceDesignEvent event = new ShellSqlServerSequenceDesignEvent();
        event.data(sequence);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void dropSequence(ShellSqlServerSequenceTreeItem treeItem) {
        ShellSqlServerSequenceDroppedEvent event = new ShellSqlServerSequenceDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void sequenceRenamed(String oldSequenceName, String newSequenceName, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerSequenceRenamedEvent event = new ShellSqlServerSequenceRenamedEvent();
        event.setDbItem(dbItem);
        event.data(oldSequenceName);
        event.setNewSequenceName(newSequenceName);
        EventUtil.post(event);
    }
}
