package cn.oyzh.easyshell.trees.dameng.schema;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.check.DamengCheck;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKey;
import cn.oyzh.easyshell.dameng.function.DamengAlertFunctionParam;
import cn.oyzh.easyshell.dameng.function.DamengCreateFunctionParam;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.dameng.procedure.DamengAlertProcedureParam;
import cn.oyzh.easyshell.dameng.procedure.DamengCreateProcedureParam;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.query.dameng.DamengExecuteResult;
import cn.oyzh.easyshell.query.dameng.DamengExplainResult;
import cn.oyzh.easyshell.dameng.record.DamengDeleteRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.dameng.table.DamengAlertTableParam;
import cn.oyzh.easyshell.dameng.table.DamengCreateTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.easyshell.dameng.view.DamengAlertViewParam;
import cn.oyzh.easyshell.dameng.view.DamengCreateViewParam;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionTreeItem;
import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionsTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.ShellDamengProcedureTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.ShellDamengProceduresTreeItem;
import cn.oyzh.easyshell.trees.dameng.query.ShellDamengQueriesTreeItem;
import cn.oyzh.easyshell.trees.dameng.root.ShellDamengRootTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTableTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTablesTreeItem;
import cn.oyzh.easyshell.trees.dameng.terminal.ShellDamengTerminalTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewsTreeItem;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦数据库树模式节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengSchemaTreeItem extends ShellDamengTreeItem<ShellDamengSchemaTreeItemValue> {

    /**
     * 当前值
     */
    private final DamengSchema value;

    /**
     * 获取模式值
     *
     * @return 模式值
     */
    public DamengSchema value() {
        return value;
    }

    /**
     * 构造达梦数据库树模式节点
     *
     * @param database 模式
     * @param treeView 树视图
     */
    public ShellDamengSchemaTreeItem(DamengSchema database, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(true);
        this.value = database;
        this.setValue(new ShellDamengSchemaTreeItemValue(this));
    }

    @Override
    public ShellDamengRootTreeItem parent() {
        return (ShellDamengRootTreeItem) super.parent();
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
        ShellDamengViewFactory.runSqlFile(this.client(), this.schema());
    }

    /**
     * 传输数据
     */
    private void transportData() {
        ShellDamengViewFactory.transportData(this.info(), this.schema());
    }

    /**
     * 转储
     */
    private void dump() {
        ShellDamengViewFactory.dumpData(this.client(), this.schema(), null, 1);
    }

    @Override
    public void delete() {
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> {
                    if (MessageBox.confirm(I18nHelper.deleteSchema() + "[" + this.schema() + "]")) {
                        if (this.parent().dropSchema(this.schema())) {
                            ShellDamengEventUtil.schemaDropped(this);
                            super.remove();
                        } else {
                            MessageBox.warn(I18nHelper.operationFail());
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
        ShellDamengViewFactory.updateSchema(this.value, this.parent());
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        this.clearChild();
        this.collapse();
        this.setLoaded(false);
        ShellDamengEventUtil.schemaClosed(this);
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<TreeItem<?>> typeItems = new ArrayList<>();
                        typeItems.add(new ShellDamengTablesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellDamengViewsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellDamengFunctionsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellDamengProceduresTreeItem(this.getTreeView()));
                        typeItems.add(new ShellDamengQueriesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellDamengTerminalTreeItem(this.getTreeView()));
                        super.setChild(typeItems);
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.error(ex.getMessage());
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
    public ShellDamengTablesTreeItem getTableTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellDamengTablesTreeItem treeItem) {
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
    public List<ShellDamengTableTreeItem> getTableChild() {
        List<ShellDamengTableTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getTableTypeChild().richChildren()) {
            if (child instanceof ShellDamengTableTreeItem treeItem) {
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
    public ShellDamengQueriesTreeItem getQueryTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellDamengQueriesTreeItem treeItem) {
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
    public ShellDamengFunctionsTreeItem getFunctionTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellDamengFunctionsTreeItem treeItem) {
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
    public List<ShellDamengFunctionTreeItem> getFunctionChild() {
        List<ShellDamengFunctionTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getFunctionTypeChild().richChildren()) {
            if (child instanceof ShellDamengFunctionTreeItem treeItem) {
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
    public ShellDamengProceduresTreeItem getProcedureTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellDamengProceduresTreeItem treeItem) {
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
    public List<ShellDamengProcedureTreeItem> getProcedureChild() {
        List<ShellDamengProcedureTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getProcedureTypeChild().richChildren()) {
            if (child instanceof ShellDamengProcedureTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    //    public DamengEventsTreeItem getEventTypeChild() {
    //        for (RichTreeItem<?> child : this.richChildren()) {
    //            if (child instanceof DamengEventsTreeItem treeItem) {
    //                return treeItem;
    //            }
    //        }
    //        return null;
    //    }
    //
    //    public List<DamengEventTreeItem> getEventChild() {
    //        List<DamengEventTreeItem> list = new ArrayList<>();
    //        for (RichTreeItem<?> child : this.getEventTypeChild().richChildren()) {
    //            if (child instanceof DamengEventTreeItem treeItem) {
    //                list.add(treeItem);
    //            }
    //        }
    //        return list;
    //    }

    /**
     * 获取视图类型子节点
     *
     * @return 视图类型子节点
     */
    public ShellDamengViewsTreeItem getViewTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellDamengViewsTreeItem treeItem) {
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
    public List<ShellDamengViewTreeItem> getViewChild() {
        List<ShellDamengViewTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getViewTypeChild().richChildren()) {
            if (child instanceof ShellDamengViewTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellDamengClient client() {
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
    public void createTable(DamengTable table, DamengColumns columns, DBObjects<DamengIndex> indexes, DBObjects<DamengForeignKey> foreignKeys, DBObjects<DamengTrigger> triggers, DBObjects<DamengCheck> checks) {
        DamengCreateTableParam param = new DamengCreateTableParam();
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
    public void createTable(DamengCreateTableParam param) {
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
    public DamengCreateTableParam createTableParam(DamengTable table, DamengColumns columns, DBObjects<DamengIndex> indexes, DBObjects<DamengForeignKey> foreignKeys, DBObjects<DamengTrigger> triggers, DBObjects<DamengCheck> checks) {
        DamengCreateTableParam param = new DamengCreateTableParam();
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
    public void alterTable(DamengTable table, DamengColumns columns, DBObjects<DamengIndex> indexes, DBObjects<DamengForeignKey> foreignKeys, DBObjects<DamengTrigger> triggers, DBObjects<DamengCheck> checks) {
        DamengAlertTableParam param = new DamengAlertTableParam();
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
    public void alterTable(DamengAlertTableParam param) {
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
    public DamengAlertTableParam alterTableParam(DamengTable table, DamengColumns columns, DBObjects<DamengIndex> indexes, DBObjects<DamengForeignKey> foreignKeys, DBObjects<DamengTrigger> triggers, DBObjects<DamengCheck> checks) {
        DamengAlertTableParam param = new DamengAlertTableParam();
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
    public DBQueryResults<DamengExecuteResult> executeSql(String sql) {
        return this.client().executeSql(this.schema(), sql);
    }

    /**
     * 执行单条sql
     *
     * @param sql sql语句
     * @return 执行结果
     */
    public DamengExecuteResult executeSingleSql(String sql) {
        return this.client().executeSingleSql(this.schema(), sql);
    }

    /**
     * 解析sql
     *
     * @param sql sql语句
     * @return 解析结果
     */
    public DBQueryResults<DamengExplainResult> explainSql(String sql) {
        return this.client().explainSql(this.schema(), sql);
    }

    /**
     * 创建函数
     *
     * @param function 函数
     */
    public void createFunction(DamengFunction function) {
        DamengCreateFunctionParam param = new DamengCreateFunctionParam();
        param.setFunction(function);
        param.setSchema(this.schema());
        this.client().createFunction(param);
    }

    /**
     * 修改函数
     *
     * @param function 函数
     */
    public void alertFunction(DamengFunction function) {
        DamengAlertFunctionParam param = new DamengAlertFunctionParam();
        param.setFunction(function);
        param.setSchema(this.schema());
        this.client().alertFunction(param);
    }

    /**
     * 删除函数
     *
     * @param function 函数
     */
    public void dropFunction(DamengFunction function) {
        this.client().dropFunction(function);
    }

    /**
     * 查询过程
     *
     * @param procedureName 过程名称
     * @return 过程
     */
    public DamengProcedure selectProcedure(String procedureName) {
        return this.client().selectProcedure(this.schema(), procedureName);
    }

    /**
     * 创建过程
     *
     * @param procedure 过程
     */
    public void createProcedure(DamengProcedure procedure) {
        DamengCreateProcedureParam param = new DamengCreateProcedureParam();
        param.setSchema(this.schema());
        param.setProcedure(procedure);
        this.client().createProcedure(param);
    }

    /**
     * 修改过程
     *
     * @param procedure 过程
     */
    public void alertProcedure(DamengProcedure procedure) {
        DamengAlertProcedureParam param = new DamengAlertProcedureParam();
        param.setSchema(this.schema());
        param.setProcedure(procedure);
        this.client().alertProcedure(param);
    }

    /**
     * 删除过程
     *
     * @param procedure 过程
     */
    public void dropProcedure(DamengProcedure procedure) {
        this.client().dropProcedure(procedure);
    }

    /**
     * 查询函数
     *
     * @param functionName 函数名称
     * @return 函数
     */
    public DamengFunction selectFunction(String functionName) {
        return this.client().selectFunction(this.schema(), functionName);
    }

    /**
     * 查询视图
     *
     * @param viewName 视图名称
     * @return 视图
     */
    public DamengView selectView(String viewName) {
        return this.client().selectView(this.schema(), viewName);
    }

    /**
     * 查询表
     *
     * @param tableName 表名称
     * @return 表
     */
    public DamengTable selectTable(String tableName) {
        return this.client().selectTable(this.schema(), tableName);
    }

    /**
     * 创建视图
     *
     * @param view 视图
     */
    public void createView(DamengView view) {
        DamengCreateViewParam param = new DamengCreateViewParam();
        param.setView(view);
        param.setSchema(this.schema());
        this.client().createView(param);
    }

    /**
     * 修改视图
     *
     * @param view 视图
     */
    public void alertView(DamengView view) {
        DamengAlertViewParam param = new DamengAlertViewParam();
        param.setView(view);
        param.setSchema(this.schema());
        this.client().alertView(param);
    }

    /**
     * 删除视图
     *
     * @param view 视图
     */
    public void dropView(DamengView view) {
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

    //@Override
    //public synchronized void doFilter(RichTreeItemFilter itemFilter) {
    //    super.doFilter(itemFilter);
    //    this.refresh();
    //}

    //    public DamengEvent selectEvent(String eventName) {
    //        return this.client().selectEvent(this.schema(), eventName);
    //    }

    //    public void alertEvent(DamengEvent event) {
    //        this.client().alertEvent(this.schema(), event);
    //    }
    //
    //    public void createEvent(DamengEvent event) {
    //        this.client().createEvent(this.schema(), event);
    //    }

    //public void dropEvent(DamengEvent event) {
    //    this.client().dropEvent(this.schema(), event);
    //}

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
    public int deleteRecord(DamengDeleteRecordParam param) {
        return this.client().deleteRecord(param);
    }

    /**
     * 查询检查约束
     *
     * @param tableName 表名称
     * @return 检查约束列表
     */
    public List<DamengCheck> checks(String tableName) {
        return this.client().selectChecks(this.schema(), tableName);
    }

    /**
     * 查询触发器
     *
     * @param tableName 表名称
     * @return 触发器列表
     */
    public List<DamengTrigger> triggers(String tableName) {
        return this.client().selectTriggers(this.schema(), tableName);
    }

    /**
     * 查询列
     *
     * @param tableName 表名称
     * @return 列列表
     */
    public List<DamengColumn> columns(String tableName) {
        DamengSelectColumnParam param = new DamengSelectColumnParam();
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
    public List<DamengIndex> indexes(String tableName) {
        return this.client().indexes(this.schema(), tableName);
    }

    /**
     * 查询外键
     *
     * @param tableName 表名称
     * @return 外键列表
     */
    public List<DamengForeignKey> foreignKeys(String tableName) {
        return this.client().selectForeignKeys(this.schema(), tableName);
    }

    /**
     * 查询记录
     *
     * @param param 查询记录参数
     * @return 记录
     */
    public DamengRecord selectRecord(DamengSelectRecordParam param) {
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

//    /**
//     * 克隆事件
//     *
//     * @param eventName    事件名称
//     * @param newEventName 新事件名称
//     */
//    public void cloneEvent(String eventName, String newEventName) {
//        this.client().cloneEvent(this.dbName(), eventName, newEventName);
//    }
}
