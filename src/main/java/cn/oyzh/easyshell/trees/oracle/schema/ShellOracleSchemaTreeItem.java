package cn.oyzh.easyshell.trees.oracle.schema;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.db.index.ShellSelectIndexParam;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.check.OracleCheck;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.foreignKey.OracleForeignKey;
import cn.oyzh.easyshell.oracle.function.OracleAlertFunctionParam;
import cn.oyzh.easyshell.oracle.function.OracleCreateFunctionParam;
import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.procedure.OracleAlertProcedureParam;
import cn.oyzh.easyshell.oracle.procedure.OracleCreateProcedureParam;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.record.OracleDeleteRecordParam;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleSelectRecordParam;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.oracle.sequence.OracleAlertSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleCreateSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.table.OracleAlertTableParam;
import cn.oyzh.easyshell.oracle.table.OracleCreateTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.trigger.OracleTrigger;
import cn.oyzh.easyshell.oracle.view.OracleAlertViewParam;
import cn.oyzh.easyshell.oracle.view.OracleCreateViewParam;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.oracle.ShellOracleEventUtil;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.db.trigger.ShellSelectTriggerParam;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.easyshell.query.oracle.OracleExecuteResult;
import cn.oyzh.easyshell.query.oracle.OracleExplainResult;
import cn.oyzh.easyshell.trees.index.ShellIndexTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeItem;
import cn.oyzh.easyshell.trees.oracle.function.ShellOracleFunctionTreeItem;
import cn.oyzh.easyshell.trees.oracle.function.ShellOracleFunctionsTreeItem;
import cn.oyzh.easyshell.trees.oracle.procedure.ShellOracleProcedureTreeItem;
import cn.oyzh.easyshell.trees.oracle.procedure.ShellOracleProceduresTreeItem;
import cn.oyzh.easyshell.trees.oracle.query.ShellOracleQueriesTreeItem;
import cn.oyzh.easyshell.trees.oracle.root.ShellOracleRootTreeItem;
import cn.oyzh.easyshell.trees.oracle.sequence.ShellOracleSequencesTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTableTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTablesTreeItem;
import cn.oyzh.easyshell.trees.oracle.terminal.ShellOracleTerminalTreeItem;
import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewTreeItem;
import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewsTreeItem;
import cn.oyzh.easyshell.util.oracle.ShellOracleViewFactory;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBIndex;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle数据库树模式节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleSchemaTreeItem extends ShellOracleTreeItem<ShellOracleSchemaTreeItemValue> implements ShellIndexTarget, ShellTriggerTarget {

    /**
     * 当前值
     */
    private final OracleSchema value;

    /**
     * 获取模式值
     *
     * @return 模式值
     */
    public OracleSchema value() {
        return value;
    }

    /**
     * 构造Oracle数据库树模式节点
     *
     * @param database 模式
     * @param treeView 树视图
     */
    public ShellOracleSchemaTreeItem(OracleSchema database, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(true);
        this.value = database;
        this.setValue(new ShellOracleSchemaTreeItemValue(this));
    }

    @Override
    public ShellOracleRootTreeItem parent() {
        return (ShellOracleRootTreeItem) super.parent();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.value.getName();
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String userName() {
        return this.info().getUser();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        if (!this.isChildEmpty()) {
            FXMenuItem closeSchema = MenuItemHelper.closeSchema(this::closeDB);
            items.add(closeSchema);
        }
        FXMenuItem editSchema = MenuItemHelper.editSchema(this::editDB);
        items.add(editSchema);
        FXMenuItem deleteSchema = MenuItemHelper.deleteSchema(this::delete);
        items.add(deleteSchema);
        items.add(MenuItemManager.getSeparatorMenuItem());
        FXMenuItem dumpData = MenuItemHelper.dumpData(this::dump);
        items.add(dumpData);
        FXMenuItem runSqlFile = MenuItemHelper.runSqlFile(this::runSqlFile);
        items.add(runSqlFile);
        FXMenuItem transportData = MenuItemHelper.transportData(this::transportData);
        items.add(transportData);
        return items;
    }

    /**
     * 运行sql文件
     */
    private void runSqlFile() {
        ShellOracleViewFactory.runSqlFile(this.client(), this.schema());
    }

    /**
     * 传输数据
     */
    private void transportData() {
        ShellOracleViewFactory.transportData(this.info(), this.schema());
    }

    /**
     * 转储
     */
    private void dump() {
        ShellOracleViewFactory.dumpData(this.client(), this.schema(), null, 1);
    }

    @Override
    public void delete() {
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> {
                    if (MessageBox.confirm(ShellI18nHelper.confirmDeleteSchema(this.schema()), this.getWindow())) {
                        if (this.parent().dropSchema(this.schema())) {
                            ShellOracleEventUtil.schemaDropped(this);
                            super.remove();
                        } else {
                            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
                        }
                    }
                })
                .onSuccess(super::refresh)
                .build();
        super.startWaiting(task);
    }

    /**
     * 编辑数据库
     */
    public void editDB() {
        ShellOracleViewFactory.updateSchema(this.value, this.parent());
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        this.clearChild();
        this.collapse();
        this.setLoaded(false);
        ShellOracleEventUtil.schemaClosed(this);
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<TreeItem<?>> typeItems = new ArrayList<>();
                        typeItems.add(new ShellOracleTablesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellOracleViewsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellIndexesTreeItem(this.getTreeView(), this));
                        typeItems.add(new ShellTriggersTreeItem(this.getTreeView(), this));
                        typeItems.add(new ShellOracleFunctionsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellOracleProceduresTreeItem(this.getTreeView()));
                        typeItems.add(new ShellOracleSequencesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellOracleQueriesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellOracleTerminalTreeItem(this.getTreeView()));
                        super.setChild(typeItems);
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.error(ex.getMessage(), this.getWindow());
                    })
                    .onFinish(() -> this.setLoading(false))
                    .build();
            super.startWaiting(task);
        }

    }

    /**
     * 获取表类型子节点
     *
     * @return 表类型子节点
     */
    public ShellOracleTablesTreeItem getTableTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellOracleTablesTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取表节点列表
     *
     * @return 表节点列表
     */
    public List<ShellOracleTableTreeItem> getTableChild() {
        List<ShellOracleTableTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getTableTypeChild().richChildren()) {
            if (child instanceof ShellOracleTableTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    /**
     * 获取查询类型子节点
     *
     * @return 查询类型子节点
     */
    public ShellOracleQueriesTreeItem getQueryTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellOracleQueriesTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取函数类型子节点
     *
     * @return 函数类型子节点
     */
    public ShellOracleFunctionsTreeItem getFunctionTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellOracleFunctionsTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取函数节点列表
     *
     * @return 函数节点列表
     */
    public List<ShellOracleFunctionTreeItem> getFunctionChild() {
        List<ShellOracleFunctionTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getFunctionTypeChild().richChildren()) {
            if (child instanceof ShellOracleFunctionTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    /**
     * 获取过程类型子节点
     *
     * @return 过程类型子节点
     */
    public ShellOracleProceduresTreeItem getProcedureTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellOracleProceduresTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取过程节点列表
     *
     * @return 过程节点列表
     */
    public List<ShellOracleProcedureTreeItem> getProcedureChild() {
        List<ShellOracleProcedureTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getProcedureTypeChild().richChildren()) {
            if (child instanceof ShellOracleProcedureTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    /**
     * 获取视图类型子节点
     *
     * @return 视图类型子节点
     */
    public ShellOracleViewsTreeItem getViewTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellOracleViewsTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取视图节点列表
     *
     * @return 视图节点列表
     */
    public List<ShellOracleViewTreeItem> getViewChild() {
        List<ShellOracleViewTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getViewTypeChild().richChildren()) {
            if (child instanceof ShellOracleViewTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    /**
     * 获取索引类型子节点
     *
     * @return 索引类型子节点
     */
    public ShellIndexesTreeItem getIndexTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellIndexesTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取索引节点列表
     *
     * @return 索引节点列表
     */
    public List<ShellIndexTreeItem> getIndexChild() {
        List<ShellIndexTreeItem> list = new ArrayList<>();
        ShellIndexesTreeItem typeChild = this.getIndexTypeChild();
        if (typeChild != null) {
            for (RichTreeItem<?> child : typeChild.richChildren()) {
                if (child instanceof ShellIndexTreeItem treeItem) {
                    list.add(treeItem);
                }
            }
        }
        return list;
    }

    public ShellOracleSequencesTreeItem getSequenceTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellOracleSequencesTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellOracleClient client() {
        return this.parent().client();
    }

    /**
     * 获取db信息
     *
     * @return db信息
     */
    public ShellConnect info() {
        return this.parent().connect();
    }

    // [已停用] 旧的数量查询实现（保留备用）
//    /**
//     * 获取表数量
//     *
//     * @return 表数量
//     */
//    public int tableSize() {
//        return this.client().tableSize(this.schema());
//    }

    // [已停用] 旧的数量查询实现（保留备用）
//    /**
//     * 获取视图数量
//     *
//     * @return 视图数量
//     */
//    public int viewSize() {
//        return this.client().viewSize(this.schema());
//    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return this.info().getName();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    @Override
    public String connectName() {
        return this.info().getName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    /**
     * 创建表
     *
     * @param table       表
     * @param columns     列
     * @param indexes     索引
     * @param foreignKeys 外键
     * @param triggers    触发器
     * @param checks      检查约束
     */
    public void createTable(OracleTable table, OracleColumns columns, DBObjects<OracleIndex> indexes, DBObjects<OracleForeignKey> foreignKeys, DBObjects<OracleTrigger> triggers, DBObjects<OracleCheck> checks) {
        OracleCreateTableParam param = new OracleCreateTableParam();
        param.setTable(table);
        param.setChecks(checks);
        param.setColumns(columns);
        param.setIndexes(indexes);
        param.setTriggers(triggers);
        param.setForeignKeys(foreignKeys);
        this.client().createTable(param);
    }

    /**
     * 创建表
     *
     * @param param 创建表参数
     */
    public void createTable(OracleCreateTableParam param) {
        this.client().createTable(param);
    }

    /**
     * 构建创建表参数
     *
     * @param table       表
     * @param columns     列
     * @param indexes     索引
     * @param foreignKeys 外键
     * @param triggers    触发器
     * @param checks      检查约束
     * @return 创建表参数
     */
    public OracleCreateTableParam createTableParam(OracleTable table, OracleColumns columns, DBObjects<OracleIndex> indexes, DBObjects<OracleForeignKey> foreignKeys, DBObjects<OracleTrigger> triggers, DBObjects<OracleCheck> checks) {
        OracleCreateTableParam param = new OracleCreateTableParam();
        param.setTable(table);
        param.setChecks(checks);
        param.setColumns(columns);
        param.setIndexes(indexes);
        param.setTriggers(triggers);
        param.setForeignKeys(foreignKeys);
        return param;
    }

    /**
     * 修改表
     *
     * @param table       表
     * @param columns     列
     * @param indexes     索引
     * @param foreignKeys 外键
     * @param triggers    触发器
     * @param checks      检查约束
     */
    public void alterTable(OracleTable table, OracleColumns columns, DBObjects<OracleIndex> indexes, DBObjects<OracleForeignKey> foreignKeys, DBObjects<OracleTrigger> triggers, DBObjects<OracleCheck> checks) {
        OracleAlertTableParam param = new OracleAlertTableParam();
        param.setTable(table);
        param.setChecks(checks);
        param.setColumns(columns);
        param.setIndexes(indexes);
        param.setTriggers(triggers);
        param.setForeignKeys(foreignKeys);
        param.setPrimaryKeys(this.selectePrimaryKeys(table.getName()));
        param.setExistAutoIncrement(this.existAutoIncrement(table.getName()));
        this.client().alertTable(param);
    }

    /**
     * 修改表
     *
     * @param param 修改表参数
     */
    public void alterTable(OracleAlertTableParam param) {
        this.client().alertTable(param);
    }

    /**
     * 构建修改表参数
     *
     * @param table       表
     * @param columns     列
     * @param indexes     索引
     * @param foreignKeys 外键
     * @param triggers    触发器
     * @param checks      检查约束
     * @return 修改表参数
     */
    public OracleAlertTableParam alterTableParam(OracleTable table, OracleColumns columns, DBObjects<OracleIndex> indexes, DBObjects<OracleForeignKey> foreignKeys, DBObjects<OracleTrigger> triggers, DBObjects<OracleCheck> checks) {
        OracleAlertTableParam param = new OracleAlertTableParam();
        param.setTable(table);
        param.setChecks(checks);
        param.setColumns(columns);
        param.setIndexes(indexes);
        param.setTriggers(triggers);
        param.setForeignKeys(foreignKeys);
        param.setPrimaryKeys(this.selectePrimaryKeys(table.getName()));
        param.setExistAutoIncrement(this.existAutoIncrement(table.getName()));
        return param;
    }

    /**
     * 查询主键列
     *
     * @param tableName 表名称
     * @return 主键列
     */
    public List<String> selectePrimaryKeys(String tableName) {
        return this.client().selectePrimaryKeys(this.schema(), tableName);
    }

    /**
     * 判断是否存在自增列
     *
     * @param tableName 表名称
     * @return 是否存在
     */
    public boolean existAutoIncrement(String tableName) {
        return this.client().existAutoIncrement(this.schema(), tableName);
    }

    /**
     * 重命名表
     *
     * @param oldTableName 表名称
     * @param newTableName 新表名称
     */
    public void renameTable(String oldTableName, String newTableName) {
        this.client().renameTable(this.schema(), oldTableName, newTableName);
    }

    /**
     * 重命名视图
     *
     * @param oldViewName 视图名称
     * @param newViewName 新视图名称
     */
    public void renameView(String oldViewName, String newViewName) {
        this.client().renameView(this.schema(), oldViewName, newViewName);
    }

    //    /**
    //     * 重命名事件
    //     *
    //     * @param oldEventName 事件名称
    //     * @param newEventName 新事件名称
    //     */
    //    public void renameEvent(String oldEventName, String newEventName) {
    //        this.client().renameEvent(this.dbName(), oldEventName, newEventName);
    //    }

    /**
     * 重命名函数
     *
     * @param oldFunctionName 函数名称
     * @param newFunctionName 新函数名称
     */
    public void renameFunction(String oldFunctionName, String newFunctionName) {
        this.client().renameFunction(this.schema(), oldFunctionName, newFunctionName);
    }

    /**
     * 重命名过程
     *
     * @param oldProcedureName 过程名称
     * @param newProcedureName 新过程名称
     */
    public void renameProcedure(String oldProcedureName, String newProcedureName) {
        this.client().renameProcedure(this.schema(), oldProcedureName, newProcedureName);
    }

    /**
     * 清空表
     *
     * @param tableName 表名称
     */
    public void clearTable(String tableName) {
        this.client().clearTable(this.schema(), tableName);
    }

    /**
     * 截断表
     *
     * @param tableName 表名称
     */
    public void truncateTable(String tableName) {
        this.client().truncateTable(this.schema(), tableName);
    }

    /**
     * 删除表
     *
     * @param tableName 表名称
     */
    public void dropTable(String tableName) {
        this.client().dropTable(this.schema(), tableName);
    }

    /**
     * 执行sql
     *
     * @param sql sql语句
     * @return 执行结果
     */
    public DBQueryResults<OracleExecuteResult> executeSql(String sql) {
        return this.client().executeSql(this.schema(), sql);
    }

    /**
     * 执行单条sql
     *
     * @param sql sql语句
     * @return 执行结果
     */
    public OracleExecuteResult executeSingleSql(String sql) {
        return this.client().executeSingleSql(this.schema(), sql);
    }

    /**
     * 解析sql
     *
     * @param sql sql语句
     * @return 解析结果
     */
    public DBQueryResults<OracleExplainResult> explainSql(String sql) {
        return this.client().explainSql(this.schema(), sql);
    }

    /**
     * 创建函数
     *
     * @param function 函数
     */
    public void createFunction(OracleFunction function) {
        OracleCreateFunctionParam param = new OracleCreateFunctionParam();
        param.setFunction(function);
        param.setSchema(this.schema());
        this.client().createFunction(param);
    }

    /**
     * 修改函数
     *
     * @param function 函数
     */
    public void alertFunction(OracleFunction function) {
        OracleAlertFunctionParam param = new OracleAlertFunctionParam();
        param.setFunction(function);
        param.setSchema(this.schema());
        this.client().alertFunction(param);
    }

    /**
     * 删除函数
     *
     * @param function 函数
     */
    public void dropFunction(OracleFunction function) {
        this.client().dropFunction(function);
    }

    /**
     * 查询过程
     *
     * @param procedureName 过程名称
     * @return 过程
     */
    public OracleProcedure selectProcedure(String procedureName) {
        return this.client().selectProcedure(this.schema(), procedureName);
    }

    /**
     * 创建过程
     *
     * @param procedure 过程
     */
    public void createProcedure(OracleProcedure procedure) {
        OracleCreateProcedureParam param = new OracleCreateProcedureParam();
        param.setSchema(this.schema());
        param.setProcedure(procedure);
        this.client().createProcedure(param);
    }

    /**
     * 修改过程
     *
     * @param procedure 过程
     */
    public void alertProcedure(OracleProcedure procedure) {
        OracleAlertProcedureParam param = new OracleAlertProcedureParam();
        param.setSchema(this.schema());
        param.setProcedure(procedure);
        this.client().alertProcedure(param);
    }

    /**
     * 删除过程
     *
     * @param procedure 过程
     */
    public void dropProcedure(OracleProcedure procedure) {
        this.client().dropProcedure(procedure);
    }

    /**
     * 查询函数
     *
     * @param functionName 函数名称
     * @return 函数
     */
    public OracleFunction selectFunction(String functionName) {
        return this.client().selectFunction(this.schema(), functionName);
    }

    /**
     * 查询视图
     *
     * @param viewName 视图名称
     * @return 视图
     */
    public OracleView selectView(String viewName) {
        return this.client().selectView(this.schema(), viewName);
    }

    /**
     * 查询表
     *
     * @param tableName 表名称
     * @return 表
     */
    public OracleTable selectTable(String tableName) {
        return this.client().selectTable(this.schema(), tableName);
    }

    /**
     * 创建视图
     *
     * @param view 视图
     */
    public void createView(OracleView view) {
        OracleCreateViewParam param = new OracleCreateViewParam();
        param.setView(view);
        param.setSchema(this.schema());
        this.client().createView(param);
    }

    /**
     * 修改视图
     *
     * @param view 视图
     */
    public void alertView(OracleView view) {
        OracleAlertViewParam param = new OracleAlertViewParam();
        param.setView(view);
        param.setSchema(this.schema());
        this.client().alertView(param);
    }

    /**
     * 删除视图
     *
     * @param view 视图
     */
    public void dropView(OracleView view) {
        this.client().dropView(view);
    }

    /**
     * 判断视图是否存在
     *
     * @param viewName 视图名称
     * @return 是否存在
     */
    public boolean existView(String viewName) {
        return this.client().existView(this.schema(), viewName);
    }

    /**
     * 查询索引
     *
     * @param indexName 索引名称
     * @return 索引
     */
    public OracleIndex selectIndex(String indexName) {
        return this.client().selectIndex(this.schema(), indexName);
    }

    @Override
    public String selectIndexDefinition(String indexName) {
        return this.client().selectIndexDefinition(this.schema(), indexName);
    }

    @Override
    public void executeIndexSql(List<String> sqlList) {
        this.client().executeIndexSql(this.schema(), sqlList);
    }

    @Override
    public boolean existIndex(String indexName) {
        return this.client().existIndex(this.schema(), indexName);
    }

    @Override
    public String indexScope() {
        return this.schema();
    }

    @Override
    public List<? extends DBIndex> selectIndexes() {
        return this.client().selectSchemaIndexes(new ShellSelectIndexParam(this.schema()));
    }

    @Override
    public void dropIndex(String indexName, String tableName) {
        OracleIndex index = new OracleIndex();
        index.setSchema(this.schema());
        index.setName(indexName);
        this.client().dropIndex(index);
    }

    @Override
    public DBIndex newIndex() {
        OracleIndex index = new OracleIndex();
        index.setSchema(this.schema());
        return index;
    }

    @Override
    public void reloadIndexes() {
        ShellIndexesTreeItem typeChild = this.getIndexTypeChild();
        if (typeChild == null) {
            return;
        }
        if (typeChild.isLoaded()) {
            typeChild.reloadChild();
        } else {
            typeChild.refresh();
        }
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    /**
     * 是否支持检查约束特性
     *
     * @return 是否支持
     */
    public boolean isSupportCheckFeature() {
        return this.client().isSupportCheckFeature();
    }

    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    @Override
    public DBDialect dialect() {
        return this.client().dialect();
    }

    /**
     * 删除记录
     *
     * @param param 删除记录参数
     * @return 影响行数
     */
    public int deleteRecord(OracleDeleteRecordParam param) {
        return this.client().deleteRecord(param);
    }

    /**
     * 查询检查约束
     *
     * @param tableName 表名称
     * @return 检查约束列表
     */
    public List<OracleCheck> checks(String tableName) {
        return this.client().selectChecks(this.schema(), tableName);
    }

    /**
     * 查询触发器
     *
     * @param tableName 表名称
     * @return 触发器列表
     */
    public List<OracleTrigger> triggers(String tableName) {
        return this.client().selectTriggers(this.schema(), tableName);
    }

    /**
     * 查询列
     *
     * @param tableName 表名称
     * @return 列列表
     */
    public List<OracleColumn> columns(String tableName) {
        OracleSelectColumnParam param = new OracleSelectColumnParam();
        param.setSchema(this.schema());
        param.setTableName(tableName);
        return this.client().selectColumns(param);
    }

    /**
     * 查询索引
     *
     * @param tableName 表名称
     * @return 索引列表
     */
    public List<OracleIndex> indexes(String tableName) {
        return this.client().indexes(this.schema(), tableName);
    }

    /**
     * 查询外键
     *
     * @param tableName 表名称
     * @return 外键列表
     */
    public List<OracleForeignKey> foreignKeys(String tableName) {
        return this.client().selectForeignKeys(this.schema(), tableName);
    }

    /**
     * 查询记录
     *
     * @param param 查询记录参数
     * @return 记录
     */
    public OracleRecord selectRecord(OracleSelectRecordParam param) {
        return this.client().selectRecord(param);
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect connect() {
        return this.client().getShellConnect();
    }

    /**
     * 克隆表
     *
     * @param tableName     表名称
     * @param newTableName  新表名称
     * @param includeRecord 是否包含数据
     */
    public void cloneTable(String tableName, String newTableName, boolean includeRecord) {
        this.client().cloneTable(this.schema(), tableName, newTableName, includeRecord);
    }

    /**
     * 克隆视图
     *
     * @param viewName    视图名称
     * @param newViewName 新视图名称
     */
    public void cloneView(String viewName, String newViewName) {
        this.client().cloneView(this.schema(), viewName, newViewName);
    }

    /**
     * 克隆函数
     *
     * @param functionName    函数名称
     * @param newFunctionName 新函数名称
     */
    public void cloneFunction(String functionName, String newFunctionName) {
        this.client().cloneFunction(this.schema(), functionName, newFunctionName);
    }

    /**
     * 克隆过程
     *
     * @param procedureName    过程名称
     * @param newProcedureName 新过程名称
     */
    public void cloneProcedure(String procedureName, String newProcedureName) {
        this.client().cloneProcedure(this.schema(), procedureName, newProcedureName);
    }

    public void cloneSequence(String sequenceName, String newSequenceName) {
        this.client().cloneSequence(this.schema(), sequenceName, newSequenceName);
    }

    public OracleSequence selectSequence(String sequenceName) {
        return this.client().selectSequence(this.schema(), sequenceName);
    }

    public void dropSequence(OracleSequence sequence) {
        this.client().dropSequence(sequence);
    }

    public void renameSequence(String oldSequenceName, String newSequenceName) {
        this.client().renameSequence(this.schema(), oldSequenceName, newSequenceName);
    }

    public void createSequence(OracleSequence sequence) {
        OracleCreateSequenceParam param = new OracleCreateSequenceParam();
        param.setSequence(sequence);
        param.setSchema(this.schema());
        this.client().createSequence(param);
    }

    public void alertSequence(OracleSequence sequence) {
        OracleAlertSequenceParam param = new OracleAlertSequenceParam();
        param.setSequence(sequence);
        param.setSchema(this.schema());
        this.client().alertSequence(param);
    }

    @Override
    public String triggerScope() {
        return this.indexScope();
    }

    @Override
    public List<? extends DBTrigger> selectTriggers() {
        return this.client().selectSchemaTriggers(new ShellSelectTriggerParam(this.schema()));
    }

    @Override
    public String selectTriggerDefinition(String triggerName) {
        return this.client().selectTriggerDefinition(this.schema(), triggerName);
    }

    @Override
    public void executeTriggerSql(List<String> sqlList) {
        this.client().executeTriggerSql(this.schema(), sqlList);
    }

    @Override
    public boolean existTrigger(String triggerName) {
        return this.client().existTrigger(this.schema(), triggerName);
    }

    @Override
    public void dropTrigger(String triggerName, String tableName) {
        this.client().dropTrigger(this.schema(), triggerName, tableName);
    }

    @Override
    public DBTrigger newTrigger() {
        return new OracleTrigger();
    }

    @Override
    public void reloadTriggers() {
        ShellTriggersTreeItem typeChild = this.getTriggerTypeChild();
        if (typeChild == null) {
            return;
        }
        if (typeChild.isLoaded()) {
            typeChild.reloadChild();
        } else {
            typeChild.refresh();
        }
    }

    /**
     * 获取触发器类型子节点
     *
     * @return 触发器类型子节点
     */
    public ShellTriggersTreeItem getTriggerTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellTriggersTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }
}
