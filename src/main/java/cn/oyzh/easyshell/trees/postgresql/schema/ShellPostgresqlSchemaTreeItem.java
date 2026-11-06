package cn.oyzh.easyshell.trees.postgresql.schema;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.check.PostgresqlCheck;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.column.PostgresqlSelectColumnParam;
import cn.oyzh.easyshell.postgresql.foreignKey.PostgresqlForeignKey;
import cn.oyzh.easyshell.postgresql.function.PostgresqlAlertFunctionParam;
import cn.oyzh.easyshell.postgresql.function.PostgresqlCreateFunctionParam;
import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
import cn.oyzh.easyshell.postgresql.index.PostgresqlIndex;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlAlertProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlCreateProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.postgresql.record.PostgresqlDeleteRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlSelectRecordParam;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlAlertSequenceParam;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlCreateSequenceParam;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlSequence;
import cn.oyzh.easyshell.postgresql.table.PostgresqlAlertTableParam;
import cn.oyzh.easyshell.postgresql.table.PostgresqlCreateTableParam;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.postgresql.trigger.PostgresqlTrigger;
import cn.oyzh.easyshell.postgresql.view.PostgresqlAlertViewParam;
import cn.oyzh.easyshell.postgresql.view.PostgresqlCreateViewParam;
import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.postgresql.ShellPostgresqlEventUtil;
import cn.oyzh.easyshell.query.postgresql.PostgresqlExecuteResult;
import cn.oyzh.easyshell.query.postgresql.PostgresqlExplainResult;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeItem;
import cn.oyzh.easyshell.trees.postgresql.function.ShellPostgresqlFunctionTreeItem;
import cn.oyzh.easyshell.trees.postgresql.function.ShellPostgresqlFunctionsTreeItem;
import cn.oyzh.easyshell.trees.postgresql.procedure.ShellPostgresqlProcedureTreeItem;
import cn.oyzh.easyshell.trees.postgresql.procedure.ShellPostgresqlProceduresTreeItem;
import cn.oyzh.easyshell.trees.postgresql.query.ShellPostgresqlQueriesTreeItem;
import cn.oyzh.easyshell.trees.postgresql.root.ShellPostgresqlRootTreeItem;
import cn.oyzh.easyshell.trees.postgresql.sequence.ShellPostgresqlSequencesTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTableTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTablesTreeItem;
import cn.oyzh.easyshell.trees.postgresql.terminal.ShellPostgresqlTerminalTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewsTreeItem;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlViewFactory;
import cn.oyzh.fx.db.DBDialect;
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
 * PostgreSQL数据库树模式节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlSchemaTreeItem extends ShellPostgresqlTreeItem<ShellPostgresqlSchemaTreeItemValue> {

    /**
     * 当前值
     */
    private final PostgresqlSchema value;

    /**
     * 获取模式值
     *
     * @return 模式值
     */
    public PostgresqlSchema value() {
        return value;
    }

    /**
     * 构造PostgreSQL数据库树模式节点
     *
     * @param database 模式
     * @param treeView 树视图
     */
    public ShellPostgresqlSchemaTreeItem(PostgresqlSchema database, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(true);
        this.value = database;
        this.setValue(new ShellPostgresqlSchemaTreeItemValue(this));
    }

    @Override
    public ShellPostgresqlRootTreeItem parent() {
        return (ShellPostgresqlRootTreeItem) super.parent();
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
        ShellPostgresqlViewFactory.runSqlFile(this.client(), this.schema());
    }

    /**
     * 传输数据
     */
    private void transportData() {
        ShellPostgresqlViewFactory.transportData(this.info(), this.schema());
    }

    /**
     * 转储
     */
    private void dump() {
        ShellPostgresqlViewFactory.dumpData(this.client(), this.schema(), null, 1);
    }

    @Override
    public void delete() {
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> {
                    if (MessageBox.confirm(ShellI18nHelper.confirm("deleteSchema", this.schema()), this.getWindow())) {
                        if (this.parent().dropSchema(this.schema())) {
                            ShellPostgresqlEventUtil.schemaDropped(this);
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
        ShellPostgresqlViewFactory.updateSchema(this.value, this.parent());
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        this.clearChild();
        this.collapse();
        this.setLoaded(false);
        ShellPostgresqlEventUtil.schemaClosed(this);
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<TreeItem<?>> typeItems = new ArrayList<>();
                        typeItems.add(new ShellPostgresqlTablesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellPostgresqlViewsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellPostgresqlFunctionsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellPostgresqlProceduresTreeItem(this.getTreeView()));
                        typeItems.add(new ShellPostgresqlSequencesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellPostgresqlQueriesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellPostgresqlTerminalTreeItem(this.getTreeView()));
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
    public ShellPostgresqlTablesTreeItem getTableTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellPostgresqlTablesTreeItem treeItem) {
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
    public List<ShellPostgresqlTableTreeItem> getTableChild() {
        List<ShellPostgresqlTableTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getTableTypeChild().richChildren()) {
            if (child instanceof ShellPostgresqlTableTreeItem treeItem) {
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
    public ShellPostgresqlQueriesTreeItem getQueryTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellPostgresqlQueriesTreeItem treeItem) {
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
    public ShellPostgresqlFunctionsTreeItem getFunctionTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellPostgresqlFunctionsTreeItem treeItem) {
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
    public List<ShellPostgresqlFunctionTreeItem> getFunctionChild() {
        List<ShellPostgresqlFunctionTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getFunctionTypeChild().richChildren()) {
            if (child instanceof ShellPostgresqlFunctionTreeItem treeItem) {
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
    public ShellPostgresqlProceduresTreeItem getProcedureTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellPostgresqlProceduresTreeItem treeItem) {
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
    public List<ShellPostgresqlProcedureTreeItem> getProcedureChild() {
        List<ShellPostgresqlProcedureTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getProcedureTypeChild().richChildren()) {
            if (child instanceof ShellPostgresqlProcedureTreeItem treeItem) {
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
    public ShellPostgresqlViewsTreeItem getViewTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellPostgresqlViewsTreeItem treeItem) {
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
    public List<ShellPostgresqlViewTreeItem> getViewChild() {
        List<ShellPostgresqlViewTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getViewTypeChild().richChildren()) {
            if (child instanceof ShellPostgresqlViewTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    public ShellPostgresqlSequencesTreeItem getSequenceTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellPostgresqlSequencesTreeItem treeItem) {
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
    public ShellPostgresqlClient client() {
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

    /**
     * 获取表数量
     *
     * @return 表数量
     */
    public int tableSize() {
        return this.client().tableSize(this.schema());
    }

    /**
     * 获取视图数量
     *
     * @return 视图数量
     */
    public int viewSize() {
        return this.client().viewSize(this.schema());
    }

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
    public void createTable(PostgresqlTable table, PostgresqlColumns columns, DBObjects<PostgresqlIndex> indexes, DBObjects<PostgresqlForeignKey> foreignKeys, DBObjects<PostgresqlTrigger> triggers, DBObjects<PostgresqlCheck> checks) {
        PostgresqlCreateTableParam param = new PostgresqlCreateTableParam();
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
    public void createTable(PostgresqlCreateTableParam param) {
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
    public PostgresqlCreateTableParam createTableParam(PostgresqlTable table, PostgresqlColumns columns, DBObjects<PostgresqlIndex> indexes, DBObjects<PostgresqlForeignKey> foreignKeys, DBObjects<PostgresqlTrigger> triggers, DBObjects<PostgresqlCheck> checks) {
        PostgresqlCreateTableParam param = new PostgresqlCreateTableParam();
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
    public void alterTable(PostgresqlTable table, PostgresqlColumns columns, DBObjects<PostgresqlIndex> indexes, DBObjects<PostgresqlForeignKey> foreignKeys, DBObjects<PostgresqlTrigger> triggers, DBObjects<PostgresqlCheck> checks) {
        PostgresqlAlertTableParam param = new PostgresqlAlertTableParam();
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
    public void alterTable(PostgresqlAlertTableParam param) {
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
    public PostgresqlAlertTableParam alterTableParam(PostgresqlTable table, PostgresqlColumns columns, DBObjects<PostgresqlIndex> indexes, DBObjects<PostgresqlForeignKey> foreignKeys, DBObjects<PostgresqlTrigger> triggers, DBObjects<PostgresqlCheck> checks) {
        PostgresqlAlertTableParam param = new PostgresqlAlertTableParam();
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
    public DBQueryResults<PostgresqlExecuteResult> executeSql(String sql) {
        return this.client().executeSql(this.schema(), sql);
    }

    /**
     * 执行单条sql
     *
     * @param sql sql语句
     * @return 执行结果
     */
    public PostgresqlExecuteResult executeSingleSql(String sql) {
        return this.client().executeSingleSql(this.schema(), sql);
    }

    /**
     * 解析sql
     *
     * @param sql sql语句
     * @return 解析结果
     */
    public DBQueryResults<PostgresqlExplainResult> explainSql(String sql) {
        return this.client().explainSql(this.schema(), sql);
    }

    /**
     * 创建函数
     *
     * @param function 函数
     */
    public void createFunction(PostgresqlFunction function) {
        PostgresqlCreateFunctionParam param = new PostgresqlCreateFunctionParam();
        param.setFunction(function);
        param.setSchema(this.schema());
        this.client().createFunction(param);
    }

    /**
     * 修改函数
     *
     * @param function 函数
     */
    public void alertFunction(PostgresqlFunction function) {
        PostgresqlAlertFunctionParam param = new PostgresqlAlertFunctionParam();
        param.setFunction(function);
        param.setSchema(this.schema());
        this.client().alertFunction(param);
    }

    /**
     * 删除函数
     *
     * @param function 函数
     */
    public void dropFunction(PostgresqlFunction function) {
        this.client().dropFunction(function);
    }

    /**
     * 查询过程
     *
     * @param procedureName 过程名称
     * @return 过程
     */
    public PostgresqlProcedure selectProcedure(String procedureName) {
        return this.client().selectProcedure(this.schema(), procedureName);
    }

    /**
     * 创建过程
     *
     * @param procedure 过程
     */
    public void createProcedure(PostgresqlProcedure procedure) {
        PostgresqlCreateProcedureParam param = new PostgresqlCreateProcedureParam();
        param.setSchema(this.schema());
        param.setProcedure(procedure);
        this.client().createProcedure(param);
    }

    /**
     * 修改过程
     *
     * @param procedure 过程
     */
    public void alertProcedure(PostgresqlProcedure procedure) {
        PostgresqlAlertProcedureParam param = new PostgresqlAlertProcedureParam();
        param.setSchema(this.schema());
        param.setProcedure(procedure);
        this.client().alertProcedure(param);
    }

    /**
     * 删除过程
     *
     * @param procedure 过程
     */
    public void dropProcedure(PostgresqlProcedure procedure) {
        this.client().dropProcedure(procedure);
    }

    /**
     * 查询函数
     *
     * @param functionName 函数名称
     * @return 函数
     */
    public PostgresqlFunction selectFunction(String functionName) {
        return this.client().selectFunction(this.schema(), functionName);
    }

    /**
     * 查询视图
     *
     * @param viewName 视图名称
     * @return 视图
     */
    public PostgresqlView selectView(String viewName) {
        return this.client().selectView(this.schema(), viewName);
    }

    /**
     * 查询表
     *
     * @param tableName 表名称
     * @return 表
     */
    public PostgresqlTable selectTable(String tableName) {
        return this.client().selectTable(this.schema(), tableName);
    }

    /**
     * 创建视图
     *
     * @param view 视图
     */
    public void createView(PostgresqlView view) {
        PostgresqlCreateViewParam param = new PostgresqlCreateViewParam();
        param.setView(view);
        param.setSchema(this.schema());
        this.client().createView(param);
    }

    /**
     * 修改视图
     *
     * @param view 视图
     */
    public void alertView(PostgresqlView view) {
        PostgresqlAlertViewParam param = new PostgresqlAlertViewParam();
        param.setView(view);
        param.setSchema(this.schema());
        this.client().alertView(param);
    }

    /**
     * 删除视图
     *
     * @param view 视图
     */
    public void dropView(PostgresqlView view) {
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
    public DBDialect dialect() {
        return this.client().dialect();
    }

    /**
     * 删除记录
     *
     * @param param 删除记录参数
     * @return 影响行数
     */
    public int deleteRecord(PostgresqlDeleteRecordParam param) {
        return this.client().deleteRecord(param);
    }

    /**
     * 查询检查约束
     *
     * @param tableName 表名称
     * @return 检查约束列表
     */
    public List<PostgresqlCheck> checks(String tableName) {
        return this.client().selectChecks(this.schema(), tableName);
    }

    /**
     * 查询触发器
     *
     * @param tableName 表名称
     * @return 触发器列表
     */
    public List<PostgresqlTrigger> triggers(String tableName) {
        return this.client().selectTriggers(this.schema(), tableName);
    }

    /**
     * 查询列
     *
     * @param tableName 表名称
     * @return 列列表
     */
    public List<PostgresqlColumn> columns(String tableName) {
        PostgresqlSelectColumnParam param = new PostgresqlSelectColumnParam();
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
    public List<PostgresqlIndex> indexes(String tableName) {
        return this.client().indexes(this.schema(), tableName);
    }

    /**
     * 查询外键
     *
     * @param tableName 表名称
     * @return 外键列表
     */
    public List<PostgresqlForeignKey> foreignKeys(String tableName) {
        return this.client().selectForeignKeys(this.schema(), tableName);
    }

    /**
     * 查询记录
     *
     * @param param 查询记录参数
     * @return 记录
     */
    public PostgresqlRecord selectRecord(PostgresqlSelectRecordParam param) {
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

    public PostgresqlSequence selectSequence(String sequenceName) {
        return this.client().selectSequence(this.schema(), sequenceName);
    }

    public void dropSequence(PostgresqlSequence sequence) {
        this.client().dropSequence(sequence);
    }

    public void renameSequence(String oldSequenceName, String newSequenceName) {
        this.client().renameSequence(this.schema(), oldSequenceName, newSequenceName);
    }

    public void createSequence(PostgresqlSequence sequence) {
        PostgresqlCreateSequenceParam param = new PostgresqlCreateSequenceParam();
        param.setSequence(sequence);
        param.setSchema(this.schema());
        this.client().createSequence(param);
    }

    public void alertSequence(PostgresqlSequence sequence) {
        PostgresqlAlertSequenceParam param = new PostgresqlAlertSequenceParam();
        param.setSequence(sequence);
        param.setSchema(this.schema());
        this.client().alertSequence(param);
    }
}
