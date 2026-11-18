package cn.oyzh.easyshell.event.oracle;

import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.oracle.function.ShellOracleFunctionDesignEvent;
import cn.oyzh.easyshell.event.oracle.function.ShellOracleFunctionDroppedEvent;
import cn.oyzh.easyshell.event.oracle.function.ShellOracleFunctionRenamedEvent;
import cn.oyzh.easyshell.event.oracle.procedure.ShellOracleProcedureDesignEvent;
import cn.oyzh.easyshell.event.oracle.procedure.ShellOracleProcedureDroppedEvent;
import cn.oyzh.easyshell.event.oracle.procedure.ShellOracleProcedureRenamedEvent;
import cn.oyzh.easyshell.event.oracle.query.ShellOracleQueryAddEvent;
import cn.oyzh.easyshell.event.oracle.query.ShellOracleQueryDeletedEvent;
import cn.oyzh.easyshell.event.oracle.query.ShellOracleQueryOpenEvent;
import cn.oyzh.easyshell.event.oracle.query.ShellOracleQueryRenamedEvent;
import cn.oyzh.easyshell.event.oracle.schema.ShellOracleSchemaAddedEvent;
import cn.oyzh.easyshell.event.oracle.schema.ShellOracleSchemaClosedEvent;
import cn.oyzh.easyshell.event.oracle.schema.ShellOracleSchemaDroppedEvent;
import cn.oyzh.easyshell.event.oracle.schema.ShellOracleSchemaUpdatedEvent;
import cn.oyzh.easyshell.event.oracle.sequence.ShellOracleSequenceDesignEvent;
import cn.oyzh.easyshell.event.oracle.sequence.ShellOracleSequenceDroppedEvent;
import cn.oyzh.easyshell.event.oracle.sequence.ShellOracleSequenceRenamedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableAlertedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableClearedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableDesignEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableDroppedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableOpenEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableRenamedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableTruncatedEvent;
import cn.oyzh.easyshell.event.oracle.terminal.ShellOracleTerminalOpenEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewAlertedEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewDesignEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewDroppedEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewOpenEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewRenamedEvent;
import cn.oyzh.easyshell.trees.oracle.function.ShellOracleFunctionTreeItem;
import cn.oyzh.easyshell.trees.oracle.procedure.ShellOracleProcedureTreeItem;
import cn.oyzh.easyshell.trees.oracle.query.ShellOracleQueryTreeItem;
import cn.oyzh.easyshell.trees.oracle.root.ShellOracleRootTreeItem;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.sequence.ShellOracleSequenceTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTableTreeItem;
import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewTreeItem;
import cn.oyzh.event.EventUtil;

/**
 * Oracle数据库事件工具类，用于发布Oracle相关事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleEventUtil {

    /**
     * 表打开事件
     *
     * @param item   表节点
     * @param dbItem 数据库节点
     */
    public static void tableOpen(ShellOracleTableTreeItem item, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleTableOpenEvent event = new ShellOracleTableOpenEvent();
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
    public static void tableAlerted(String tableName, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleTableAlertedEvent event = new ShellOracleTableAlertedEvent();
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
    public static void tableRenamed(String tableName, String newTableName, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleTableRenamedEvent event = new ShellOracleTableRenamedEvent();
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
    public static void viewRenamed(String viewName, String newViewName, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleViewRenamedEvent event = new ShellOracleViewRenamedEvent();
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
    public static void functionRenamed(String functionName, String newFunctionName, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleFunctionRenamedEvent event = new ShellOracleFunctionRenamedEvent();
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
    public static void procedureRenamed(String procedureName, String newProcedureName, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleProcedureRenamedEvent event = new ShellOracleProcedureRenamedEvent();
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
    public static void tableCleared(ShellOracleTableTreeItem tableItem, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleTableClearedEvent event = new ShellOracleTableClearedEvent();
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
    public static void tableTruncated(ShellOracleTableTreeItem tableItem, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleTableTruncatedEvent event = new ShellOracleTableTruncatedEvent();
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
    public static void tableDropped(ShellOracleTableTreeItem tableItem, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleTableDroppedEvent event = new ShellOracleTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    /**
     * 模式关闭事件
     *
     * @param dbItem 数据库节点
     */
    public static void schemaClosed(ShellOracleSchemaTreeItem dbItem) {
        ShellOracleSchemaClosedEvent event = new ShellOracleSchemaClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 模式新增事件
     *
     * @param connectItem 连接节点
     * @param schema      模式
     */
    public static void schemaAdded(ShellOracleRootTreeItem connectItem, OracleSchema schema) {
        ShellOracleSchemaAddedEvent event = new ShellOracleSchemaAddedEvent();
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
    public static void schemaUpdated(ShellOracleRootTreeItem connectItem, OracleSchema schema) {
        ShellOracleSchemaUpdatedEvent event = new ShellOracleSchemaUpdatedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 模式删除事件
     *
     * @param dbItem 数据库节点
     */
    public static void schemaDropped(ShellOracleSchemaTreeItem dbItem) {
        ShellOracleSchemaDroppedEvent event = new ShellOracleSchemaDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 查询新增事件
     *
     * @param item 数据库节点
     */
    public static void queryAdd(ShellOracleSchemaTreeItem item) {
        ShellOracleQueryAddEvent event = new ShellOracleQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询删除事件
     *
     * @param item 查询节点
     */
    public static void queryDeleted(ShellOracleQueryTreeItem item) {
        ShellOracleQueryDeletedEvent event = new ShellOracleQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询打开事件
     *
     * @param query 查询
     * @param item  数据库节点
     */
    public static void queryOpen(ShellQuery query, ShellOracleSchemaTreeItem item) {
        ShellOracleQueryOpenEvent event = new ShellOracleQueryOpenEvent();
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
    public static void queryRenamed(ShellOracleQueryTreeItem item, String queryName) {
        ShellOracleQueryRenamedEvent event = new ShellOracleQueryRenamedEvent();
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
    public static void viewOpen(ShellOracleViewTreeItem item, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleViewOpenEvent event = new ShellOracleViewOpenEvent();
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
    public static void designFunction(OracleFunction function, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleFunctionDesignEvent event = new ShellOracleFunctionDesignEvent();
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
    public static void designProcedure(OracleProcedure procedure, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleProcedureDesignEvent event = new ShellOracleProcedureDesignEvent();
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
    public static void viewAlerted(String viewName, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleViewAlertedEvent event = new ShellOracleViewAlertedEvent();
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
    public static void designView(OracleView dbView, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleViewDesignEvent event = new ShellOracleViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 视图删除事件
     *
     * @param treeItem 视图节点
     */
    public static void dropView(ShellOracleViewTreeItem treeItem) {
        ShellOracleViewDroppedEvent event = new ShellOracleViewDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 函数删除事件
     *
     * @param treeItem 函数节点
     */
    public static void dropFunction(ShellOracleFunctionTreeItem treeItem) {
        ShellOracleFunctionDroppedEvent event = new ShellOracleFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 存储过程删除事件
     *
     * @param treeItem 存储过程节点
     */
    public static void dropProcedure(ShellOracleProcedureTreeItem treeItem) {
        ShellOracleProcedureDroppedEvent event = new ShellOracleProcedureDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 表设计事件
     *
     * @param table  表
     * @param dbItem 数据库节点
     */
    public static void designTable(OracleTable table, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleTableDesignEvent event = new ShellOracleTableDesignEvent();
        event.data(table);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 终端打开事件
     *
     * @param dbItem 数据库节点
     */
    public static void terminalOpen(ShellOracleSchemaTreeItem dbItem) {
        ShellOracleTerminalOpenEvent event = new ShellOracleTerminalOpenEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    public static void designSequence(OracleSequence sequence, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleSequenceDesignEvent event = new ShellOracleSequenceDesignEvent();
        event.data(sequence);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    public static void dropSequence(ShellOracleSequenceTreeItem treeItem) {
        ShellOracleSequenceDroppedEvent event = new ShellOracleSequenceDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    public static void sequenceRenamed(String oldSequenceName, String newSequenceName, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleSequenceRenamedEvent event = new ShellOracleSequenceRenamedEvent();
        event.setDbItem(dbItem);
        event.data(oldSequenceName);
        event.setNewSequenceName(newSequenceName);
        EventUtil.post(event);
    }
}
