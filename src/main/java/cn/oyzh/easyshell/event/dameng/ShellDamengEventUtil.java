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
 * 达梦数据库事件工具类，用于发布达梦相关事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengEventUtil {

    /**
     * 表打开事件
     *
     * @param item   表节点
     * @param dbItem 数据库节点
     */
    public static void tableOpen(ShellDamengTableTreeItem item, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableOpenEvent event = new ShellDamengTableOpenEvent();
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
    public static void tableAlerted(String tableName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableAlertedEvent event = new ShellDamengTableAlertedEvent();
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
    public static void tableRenamed(String tableName, String newTableName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableRenamedEvent event = new ShellDamengTableRenamedEvent();
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
    public static void viewRenamed(String viewName, String newViewName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengViewRenamedEvent event = new ShellDamengViewRenamedEvent();
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
    public static void functionRenamed(String functionName, String newFunctionName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengFunctionRenamedEvent event = new ShellDamengFunctionRenamedEvent();
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
    public static void procedureRenamed(String procedureName, String newProcedureName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengProcedureRenamedEvent event = new ShellDamengProcedureRenamedEvent();
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
    public static void tableCleared(ShellDamengTableTreeItem tableItem, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableClearedEvent event = new ShellDamengTableClearedEvent();
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
    public static void tableTruncated(ShellDamengTableTreeItem tableItem, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableTruncatedEvent event = new ShellDamengTableTruncatedEvent();
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
    public static void tableDropped(ShellDamengTableTreeItem tableItem, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableDroppedEvent event = new ShellDamengTableDroppedEvent();
        event.setDbItem(dbItem);
        event.data(tableItem);
        EventUtil.post(event);
    }

    /**
     * 模式关闭事件
     *
     * @param dbItem 数据库节点
     */
    public static void schemaClosed(ShellDamengSchemaTreeItem dbItem) {
        ShellDamengSchemaClosedEvent event = new ShellDamengSchemaClosedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 模式新增事件
     *
     * @param connectItem 连接节点
     * @param schema      模式
     */
    public static void schemaAdded(ShellDamengRootTreeItem connectItem, DamengSchema schema) {
        ShellDamengSchemaAddedEvent event = new ShellDamengSchemaAddedEvent();
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
    public static void schemaUpdated(ShellDamengRootTreeItem connectItem, DamengSchema schema) {
        ShellDamengSchemaUpdatedEvent event = new ShellDamengSchemaUpdatedEvent();
        event.data(schema);
        event.setConnectItem(connectItem);
        EventUtil.post(event);
    }

    /**
     * 模式删除事件
     *
     * @param dbItem 数据库节点
     */
    public static void schemaDropped(ShellDamengSchemaTreeItem dbItem) {
        ShellDamengSchemaDroppedEvent event = new ShellDamengSchemaDroppedEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

    /**
     * 查询新增事件
     *
     * @param item 数据库节点
     */
    public static void queryAdd(ShellDamengSchemaTreeItem item) {
        ShellDamengQueryAddEvent event = new ShellDamengQueryAddEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询删除事件
     *
     * @param item 查询节点
     */
    public static void queryDeleted(ShellDamengQueryTreeItem item) {
        ShellDamengQueryDeletedEvent event = new ShellDamengQueryDeletedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 查询打开事件
     *
     * @param query 查询
     * @param item  数据库节点
     */
    public static void queryOpen(ShellQuery query, ShellDamengSchemaTreeItem item) {
        ShellDamengQueryOpenEvent event = new ShellDamengQueryOpenEvent();
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
    public static void queryRenamed(ShellDamengQueryTreeItem item, String queryName) {
        ShellDamengQueryRenamedEvent event = new ShellDamengQueryRenamedEvent();
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
    public static void viewOpen(ShellDamengViewTreeItem item, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengViewOpenEvent event = new ShellDamengViewOpenEvent();
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
    public static void designFunction(DamengFunction function, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengFunctionDesignEvent event = new ShellDamengFunctionDesignEvent();
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
    public static void designProcedure(DamengProcedure procedure, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengProcedureDesignEvent event = new ShellDamengProcedureDesignEvent();
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
    public static void viewAlerted(String viewName, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengViewAlertedEvent event = new ShellDamengViewAlertedEvent();
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
    public static void designView(DamengView dbView, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengViewDesignEvent event = new ShellDamengViewDesignEvent();
        event.data(dbView);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 视图删除事件
     *
     * @param treeItem 视图节点
     */
    public static void dropView(ShellDamengViewTreeItem treeItem) {
        ShellDamengViewDroppedEvent event = new ShellDamengViewDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 函数删除事件
     *
     * @param treeItem 函数节点
     */
    public static void dropFunction(ShellDamengFunctionTreeItem treeItem) {
        ShellDamengFunctionDroppedEvent event = new ShellDamengFunctionDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 存储过程删除事件
     *
     * @param treeItem 存储过程节点
     */
    public static void dropProcedure(ShellDamengProcedureTreeItem treeItem) {
        ShellDamengProcedureDroppedEvent event = new ShellDamengProcedureDroppedEvent();
        event.data(treeItem);
        EventUtil.postSync(event);
    }

    /**
     * 表设计事件
     *
     * @param table  表
     * @param dbItem 数据库节点
     */
    public static void designTable(DamengTable table, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTableDesignEvent event = new ShellDamengTableDesignEvent();
        event.data(table);
        event.setDbItem(dbItem);
        EventUtil.post(event);
    }

    /**
     * 终端打开事件
     *
     * @param dbItem 数据库节点
     */
    public static void terminalOpen(ShellDamengSchemaTreeItem dbItem) {
        ShellDamengTerminalOpenEvent event = new ShellDamengTerminalOpenEvent();
        event.data(dbItem);
        EventUtil.post(event);
    }

}
