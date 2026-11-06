package cn.oyzh.easyshell.event.postgresql;

import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlSequence;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.postgresql.function.ShellPostgresqlFunctionDesignEvent;
import cn.oyzh.easyshell.event.postgresql.function.ShellPostgresqlFunctionDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.function.ShellPostgresqlFunctionRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.procedure.ShellPostgresqlProcedureDesignEvent;
import cn.oyzh.easyshell.event.postgresql.procedure.ShellPostgresqlProcedureDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.procedure.ShellPostgresqlProcedureRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.query.ShellPostgresqlQueryAddEvent;
import cn.oyzh.easyshell.event.postgresql.query.ShellPostgresqlQueryDeletedEvent;
import cn.oyzh.easyshell.event.postgresql.query.ShellPostgresqlQueryOpenEvent;
import cn.oyzh.easyshell.event.postgresql.query.ShellPostgresqlQueryRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.schema.ShellPostgresqlSchemaAddedEvent;
import cn.oyzh.easyshell.event.postgresql.schema.ShellPostgresqlSchemaClosedEvent;
import cn.oyzh.easyshell.event.postgresql.schema.ShellPostgresqlSchemaDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.schema.ShellPostgresqlSchemaUpdatedEvent;
import cn.oyzh.easyshell.event.postgresql.sequence.ShellPostgresqlSequenceDesignEvent;
import cn.oyzh.easyshell.event.postgresql.sequence.ShellPostgresqlSequenceDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.sequence.ShellPostgresqlSequenceRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableAlertedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableClearedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableDesignEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableOpenEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableTruncatedEvent;
import cn.oyzh.easyshell.event.postgresql.terminal.ShellPostgresqlTerminalOpenEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewAlertedEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewDesignEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewOpenEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewRenamedEvent;
import cn.oyzh.easyshell.trees.postgresql.function.ShellPostgresqlFunctionTreeItem;
import cn.oyzh.easyshell.trees.postgresql.procedure.ShellPostgresqlProcedureTreeItem;
import cn.oyzh.easyshell.trees.postgresql.query.ShellPostgresqlQueryTreeItem;
import cn.oyzh.easyshell.trees.postgresql.root.ShellPostgresqlRootTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.trees.postgresql.sequence.ShellPostgresqlSequenceTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTableTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * PostgreSQL数据库事件工具类，用于发布PostgreSQL相关事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlEventUtil {

    /**
     * 表打开事件
     *
     * @param item   表节点
     * @param dbItem 数据库节点
     */
    public static void tableOpen(ShellPostgresqlTableTreeItem item, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlTableOpenEvent event = new ShellPostgresqlTableOpenEvent();
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
    public static void tableAlerted(String tableName, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlTableAlertedEvent event = new ShellPostgresqlTableAlertedEvent();
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
    public static void tableRenamed(String tableName, String newTableName, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlTableRenamedEvent event = new ShellPostgresqlTableRenamedEvent();
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
    public static void viewRenamed(String viewName, String newViewName, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlViewRenamedEvent event = new ShellPostgresqlViewRenamedEvent();
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
    public static void functionRenamed(String functionName, String newFunctionName, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlFunctionRenamedEvent event = new ShellPostgresqlFunctionRenamedEvent();
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
    public static void procedureRenamed(String procedureName, String newProcedureName, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlProcedureRenamedEvent event = new ShellPostgresqlProcedureRenamedEvent();
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
    public static void tableCleared(ShellPostgresqlTableTreeItem tableItem, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlTableClearedEvent event = new ShellPostgresqlTableClearedEvent();
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
    public static void tableTruncated(ShellPostgresqlTableTreeItem tableItem, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlTableTruncatedEvent event = new ShellPostgresqlTableTruncatedEvent();
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
    public static void tableDropped(ShellPostgresqlTableTreeItem tableItem, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlTableDroppedEvent event = new ShellPostgresqlTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    /**
     * 模式关闭事件
     *
     * @param dbItem 数据库节点
     */
    public static void schemaClosed(ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlSchemaClosedEvent event = new ShellPostgresqlSchemaClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 模式新增事件
     *
     * @param connectItem 连接节点
     * @param schema      模式
     */
    public static void schemaAdded(ShellPostgresqlRootTreeItem connectItem, PostgresqlSchema schema) {
        ShellPostgresqlSchemaAddedEvent event = new ShellPostgresqlSchemaAddedEvent();
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
    public static void schemaUpdated(ShellPostgresqlRootTreeItem connectItem, PostgresqlSchema schema) {
        ShellPostgresqlSchemaUpdatedEvent event = new ShellPostgresqlSchemaUpdatedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 模式删除事件
     *
     * @param dbItem 数据库节点
     */
    public static void schemaDropped(ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlSchemaDroppedEvent event = new ShellPostgresqlSchemaDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 查询新增事件
     *
     * @param item 数据库节点
     */
    public static void queryAdd(ShellPostgresqlSchemaTreeItem item) {
        ShellPostgresqlQueryAddEvent event = new ShellPostgresqlQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询删除事件
     *
     * @param item 查询节点
     */
    public static void queryDeleted(ShellPostgresqlQueryTreeItem item) {
        ShellPostgresqlQueryDeletedEvent event = new ShellPostgresqlQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询打开事件
     *
     * @param query 查询
     * @param item  数据库节点
     */
    public static void queryOpen(ShellQuery query, ShellPostgresqlSchemaTreeItem item) {
        ShellPostgresqlQueryOpenEvent event = new ShellPostgresqlQueryOpenEvent();
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
    public static void queryRenamed(ShellPostgresqlQueryTreeItem item, String queryName) {
        ShellPostgresqlQueryRenamedEvent event = new ShellPostgresqlQueryRenamedEvent();
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
    public static void viewOpen(ShellPostgresqlViewTreeItem item, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlViewOpenEvent event = new ShellPostgresqlViewOpenEvent();
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
    public static void designFunction(PostgresqlFunction function, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlFunctionDesignEvent event = new ShellPostgresqlFunctionDesignEvent();
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
    public static void designProcedure(PostgresqlProcedure procedure, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlProcedureDesignEvent event = new ShellPostgresqlProcedureDesignEvent();
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
    public static void viewAlerted(String viewName, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlViewAlertedEvent event = new ShellPostgresqlViewAlertedEvent();
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
    public static void designView(PostgresqlView dbView, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlViewDesignEvent event = new ShellPostgresqlViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 视图删除事件
     *
     * @param treeItem 视图节点
     */
    public static void dropView(ShellPostgresqlViewTreeItem treeItem) {
        ShellPostgresqlViewDroppedEvent event = new ShellPostgresqlViewDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 函数删除事件
     *
     * @param treeItem 函数节点
     */
    public static void dropFunction(ShellPostgresqlFunctionTreeItem treeItem) {
        ShellPostgresqlFunctionDroppedEvent event = new ShellPostgresqlFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 存储过程删除事件
     *
     * @param treeItem 存储过程节点
     */
    public static void dropProcedure(ShellPostgresqlProcedureTreeItem treeItem) {
        ShellPostgresqlProcedureDroppedEvent event = new ShellPostgresqlProcedureDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 表设计事件
     *
     * @param table  表
     * @param dbItem 数据库节点
     */
    public static void designTable(PostgresqlTable table, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlTableDesignEvent event = new ShellPostgresqlTableDesignEvent();
        event.data(table);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 终端打开事件
     *
     * @param dbItem 数据库节点
     */
    public static void terminalOpen(ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlTerminalOpenEvent event = new ShellPostgresqlTerminalOpenEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void designSequence(PostgresqlSequence sequence, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlSequenceDesignEvent event = new ShellPostgresqlSequenceDesignEvent();
        event.data(sequence);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void dropSequence(ShellPostgresqlSequenceTreeItem treeItem) {
        ShellPostgresqlSequenceDroppedEvent event = new ShellPostgresqlSequenceDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void sequenceRenamed(String oldSequenceName, String newSequenceName, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlSequenceRenamedEvent event = new ShellPostgresqlSequenceRenamedEvent();
        event.setDbItem(dbItem);
        event.data(oldSequenceName);
        event.setNewSequenceName(newSequenceName);
        EventUtil.post(event);
    }
}
