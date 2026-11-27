package cn.oyzh.easyshell.trees.sqlserver.schema;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.db.index.ShellSelectIndexParam;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.check.SqlServerCheck;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.column.SqlServerSelectColumnParam;
import cn.oyzh.easyshell.sqlserver.foreignKey.SqlServerForeignKey;
import cn.oyzh.easyshell.sqlserver.function.SqlServerAlertFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerCreateFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.index.SqlServerIndex;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.db.trigger.ShellSelectTriggerParam;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerAlertProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerCreateProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.record.SqlServerDeleteRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerAlertSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerCreateSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.easyshell.sqlserver.table.SqlServerAlertTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerCreateTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.easyshell.sqlserver.view.SqlServerAlertViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerCreateViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlserver.ShellSqlServerEventUtil;
import cn.oyzh.easyshell.query.sqlserver.SqlServerExecuteResult;
import cn.oyzh.easyshell.query.sqlserver.SqlServerExplainResult;
import cn.oyzh.easyshell.trees.sqlserver.ShellSqlServerTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.function.ShellSqlServerFunctionTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.function.ShellSqlServerFunctionsTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.procedure.ShellSqlServerProcedureTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.procedure.ShellSqlServerProceduresTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.query.ShellSqlServerQueriesTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.root.ShellSqlServerRootTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.sequence.ShellSqlServerSequencesTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.table.ShellSqlServerTableTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.table.ShellSqlServerTablesTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.terminal.ShellSqlServerTerminalTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewsTreeItem;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerViewFactory;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlServer数据库树模式节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerSchemaTreeItem extends ShellSqlServerTreeItem<ShellSqlServerSchemaTreeItemValue> implements ShellIndexTarget, ShellTriggerTarget {

    /**
     * 当前值
     */
    private final SqlServerSchema value;

    /**
     * 获取模式值
     *
     * @return 模式值
     */
    public SqlServerSchema value() {
        return value;
    }

    /**
     * 构造SqlServer数据库树模式节点
     *
     * @param database 模式
     * @param treeView 树视图
     */
    public ShellSqlServerSchemaTreeItem(SqlServerSchema database, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(true);
        this.value = database;
        this.setValue(new ShellSqlServerSchemaTreeItemValue(this));
    }

    @Override
    public ShellSqlServerRootTreeItem parent() {
        return (ShellSqlServerRootTreeItem) super.parent();
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
        deleteSchema.setDisable(this.value.isSystemSchema());
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
        ShellSqlServerViewFactory.runSqlFile(this.client(), this.schema());
    }

    /**
     * 传输数据
     */
    private void transportData() {
        ShellSqlServerViewFactory.transportData(this.info(), this.schema());
    }

    /**
     * 转储
     */
    private void dump() {
        ShellSqlServerViewFactory.dumpData(this.client(), this.schema(), null, 1);
    }

    @Override
    public void delete() {
        if (this.value.isSystemSchema()) {
            return;
        }
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteSchema(this.schema()), this.getWindow())) {
            return;
        }
        try {
            this.parent().dropSchema(this.schema());
            ShellSqlServerEventUtil.schemaDropped(this);
            super.remove();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 编辑数据库
     */
    public void editDB() {
        ShellSqlServerViewFactory.updateSchema(this.value, this.parent());
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        this.clearChild();
        this.collapse();
        this.setLoaded(false);
        ShellSqlServerEventUtil.schemaClosed(this);
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<TreeItem<?>> typeItems = new ArrayList<>();
                        typeItems.add(new ShellSqlServerTablesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellSqlServerViewsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellIndexesTreeItem(this.getTreeView(), this));
                        typeItems.add(new ShellTriggersTreeItem(this.getTreeView(), this));
                        typeItems.add(new ShellSqlServerFunctionsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellSqlServerProceduresTreeItem(this.getTreeView()));
                        typeItems.add(new ShellSqlServerSequencesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellSqlServerQueriesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellSqlServerTerminalTreeItem(this.getTreeView()));
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
    public ShellSqlServerTablesTreeItem getTableTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellSqlServerTablesTreeItem treeItem) {
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
    public List<ShellSqlServerTableTreeItem> getTableChild() {
        List<ShellSqlServerTableTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getTableTypeChild().richChildren()) {
            if (child instanceof ShellSqlServerTableTreeItem treeItem) {
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
     * 获取查询类型子节点
     *
     * @return 查询类型子节点
     */
    public ShellSqlServerQueriesTreeItem getQueryTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellSqlServerQueriesTreeItem treeItem) {
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
    public ShellSqlServerFunctionsTreeItem getFunctionTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellSqlServerFunctionsTreeItem treeItem) {
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
    public List<ShellSqlServerFunctionTreeItem> getFunctionChild() {
        List<ShellSqlServerFunctionTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getFunctionTypeChild().richChildren()) {
            if (child instanceof ShellSqlServerFunctionTreeItem treeItem) {
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
    public ShellSqlServerProceduresTreeItem getProcedureTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellSqlServerProceduresTreeItem treeItem) {
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
    public List<ShellSqlServerProcedureTreeItem> getProcedureChild() {
        List<ShellSqlServerProcedureTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getProcedureTypeChild().richChildren()) {
            if (child instanceof ShellSqlServerProcedureTreeItem treeItem) {
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
    public ShellSqlServerViewsTreeItem getViewTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellSqlServerViewsTreeItem treeItem) {
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
    public List<ShellSqlServerViewTreeItem> getViewChild() {
        List<ShellSqlServerViewTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getViewTypeChild().richChildren()) {
            if (child instanceof ShellSqlServerViewTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    public ShellSqlServerSequencesTreeItem getSequenceTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellSqlServerSequencesTreeItem treeItem) {
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
    public ShellSqlServerClient client() {
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
    public void createTable(SqlServerTable table, SqlServerColumns columns, DBObjects<SqlServerIndex> indexes, DBObjects<SqlServerForeignKey> foreignKeys, DBObjects<SqlServerTrigger> triggers, DBObjects<SqlServerCheck> checks) {
        SqlServerCreateTableParam param = new SqlServerCreateTableParam();
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
    public void createTable(SqlServerCreateTableParam param) {
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
    public SqlServerCreateTableParam createTableParam(SqlServerTable table, SqlServerColumns columns, DBObjects<SqlServerIndex> indexes, DBObjects<SqlServerForeignKey> foreignKeys, DBObjects<SqlServerTrigger> triggers, DBObjects<SqlServerCheck> checks) {
        SqlServerCreateTableParam param = new SqlServerCreateTableParam();
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
    public void alterTable(SqlServerTable table, SqlServerColumns columns, DBObjects<SqlServerIndex> indexes, DBObjects<SqlServerForeignKey> foreignKeys, DBObjects<SqlServerTrigger> triggers, DBObjects<SqlServerCheck> checks) {
        SqlServerAlertTableParam param = new SqlServerAlertTableParam();
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
    public void alterTable(SqlServerAlertTableParam param) {
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
    public SqlServerAlertTableParam alterTableParam(SqlServerTable table, SqlServerColumns columns, DBObjects<SqlServerIndex> indexes, DBObjects<SqlServerForeignKey> foreignKeys, DBObjects<SqlServerTrigger> triggers, DBObjects<SqlServerCheck> checks) {
        SqlServerAlertTableParam param = new SqlServerAlertTableParam();
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
    public DBQueryResults<SqlServerExecuteResult> executeSql(String sql) {
        return this.client().executeSql(this.schema(), sql);
    }

    /**
     * 执行单条sql
     *
     * @param sql sql语句
     * @return 执行结果
     */
    public SqlServerExecuteResult executeSingleSql(String sql) {
        return this.client().executeSingleSql(this.schema(), sql);
    }

    /**
     * 解析sql
     *
     * @param sql sql语句
     * @return 解析结果
     */
    public DBQueryResults<SqlServerExplainResult> explainSql(String sql) {
        return this.client().explainSql(this.schema(), sql);
    }

    /**
     * 创建函数
     *
     * @param function 函数
     */
    public void createFunction(SqlServerFunction function) {
        SqlServerCreateFunctionParam param = new SqlServerCreateFunctionParam();
        param.setFunction(function);
        param.setSchema(this.schema());
        this.client().createFunction(param);
    }

    /**
     * 修改函数
     *
     * @param function 函数
     */
    public void alertFunction(SqlServerFunction function) {
        SqlServerAlertFunctionParam param = new SqlServerAlertFunctionParam();
        param.setFunction(function);
        param.setSchema(this.schema());
        this.client().alertFunction(param);
    }

    /**
     * 删除函数
     *
     * @param function 函数
     */
    public void dropFunction(SqlServerFunction function) {
        this.client().dropFunction(function);
    }

    /**
     * 查询过程
     *
     * @param procedureName 过程名称
     * @return 过程
     */
    public SqlServerProcedure selectProcedure(String procedureName) {
        return this.client().selectProcedure(this.schema(), procedureName);
    }

    /**
     * 创建过程
     *
     * @param procedure 过程
     */
    public void createProcedure(SqlServerProcedure procedure) {
        SqlServerCreateProcedureParam param = new SqlServerCreateProcedureParam();
        param.setSchema(this.schema());
        param.setProcedure(procedure);
        this.client().createProcedure(param);
    }

    /**
     * 修改过程
     *
     * @param procedure 过程
     */
    public void alertProcedure(SqlServerProcedure procedure) {
        SqlServerAlertProcedureParam param = new SqlServerAlertProcedureParam();
        param.setSchema(this.schema());
        param.setProcedure(procedure);
        this.client().alertProcedure(param);
    }

    /**
     * 删除过程
     *
     * @param procedure 过程
     */
    public void dropProcedure(SqlServerProcedure procedure) {
        this.client().dropProcedure(procedure);
    }

    /**
     * 查询函数
     *
     * @param functionName 函数名称
     * @return 函数
     */
    public SqlServerFunction selectFunction(String functionName) {
        return this.client().selectFunction(this.schema(), functionName);
    }

    /**
     * 查询视图
     *
     * @param viewName 视图名称
     * @return 视图
     */
    public SqlServerView selectView(String viewName) {
        return this.client().selectView(this.schema(), viewName);
    }

    /**
     * 查询表
     *
     * @param tableName 表名称
     * @return 表
     */
    public SqlServerTable selectTable(String tableName) {
        return this.client().selectTable(this.schema(), tableName);
    }

    /**
     * 创建视图
     *
     * @param view 视图
     */
    public void createView(SqlServerView view) {
        SqlServerCreateViewParam param = new SqlServerCreateViewParam();
        param.setView(view);
        param.setSchema(this.schema());
        this.client().createView(param);
    }

    /**
     * 修改视图
     *
     * @param view 视图
     */
    public void alertView(SqlServerView view) {
        SqlServerAlertViewParam param = new SqlServerAlertViewParam();
        param.setView(view);
        param.setSchema(this.schema());
        this.client().alertView(param);
    }

    /**
     * 删除视图
     *
     * @param view 视图
     */
    public void dropView(SqlServerView view) {
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
    public int deleteRecord(SqlServerDeleteRecordParam param) {
        return this.client().deleteRecord(param);
    }

    /**
     * 查询检查约束
     *
     * @param tableName 表名称
     * @return 检查约束列表
     */
    public List<SqlServerCheck> checks(String tableName) {
        return this.client().selectChecks(this.schema(), tableName);
    }

    /**
     * 查询触发器
     *
     * @param tableName 表名称
     * @return 触发器列表
     */
    public List<SqlServerTrigger> triggers(String tableName) {
        return this.client().selectTriggers(this.schema(), tableName);
    }

    /**
     * 查询列
     *
     * @param tableName 表名称
     * @return 列列表
     */
    public List<SqlServerColumn> columns(String tableName) {
        SqlServerSelectColumnParam param = new SqlServerSelectColumnParam();
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
    public List<SqlServerIndex> indexes(String tableName) {
        return this.client().indexes(this.schema(), tableName);
    }

    /**
     * 查询外键
     *
     * @param tableName 表名称
     * @return 外键列表
     */
    public List<SqlServerForeignKey> foreignKeys(String tableName) {
        return this.client().selectForeignKeys(this.schema(), tableName);
    }

    /**
     * 查询记录
     *
     * @param param 查询记录参数
     * @return 记录
     */
    public SqlServerRecord selectRecord(SqlServerSelectRecordParam param) {
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

    public SqlServerSequence selectSequence(String sequenceName) {
        return this.client().selectSequence(this.schema(), sequenceName);
    }

    public void dropSequence(SqlServerSequence sequence) {
        this.client().dropSequence(sequence);
    }

    public void renameSequence(String oldSequenceName, String newSequenceName) {
        this.client().renameSequence(this.schema(), oldSequenceName, newSequenceName);
    }

    public void createSequence(SqlServerSequence sequence) {
        SqlServerCreateSequenceParam param = new SqlServerCreateSequenceParam();
        param.setSequence(sequence);
        param.setSchema(this.schema());
        this.client().createSequence(param);
    }

    public void alertSequence(SqlServerSequence sequence) {
        SqlServerAlertSequenceParam param = new SqlServerAlertSequenceParam();
        param.setSequence(sequence);
        param.setSchema(this.schema());
        this.client().alertSequence(param);
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
    public void dropIndex(String indexName, String tableName) {
        this.client().dropIndex(this.schema(), indexName, tableName);
    }

    @Override
    public DBIndex newIndex() {
        return new SqlServerIndex();
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
        return new SqlServerTrigger();
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
