package cn.oyzh.easyshell.trees.mariadb.database;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.check.MariadbCheck;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.column.MariadbSelectColumnParam;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.mariadb.foreignKey.MariadbForeignKey;
import cn.oyzh.easyshell.mariadb.function.MariadbAlertFunctionParam;
import cn.oyzh.easyshell.mariadb.function.MariadbCreateFunctionParam;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
import cn.oyzh.easyshell.mariadb.procedure.MariadbAlertProcedureParam;
import cn.oyzh.easyshell.mariadb.procedure.MariadbCreateProcedureParam;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.mariadb.record.MariadbDeleteRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbSelectRecordParam;
import cn.oyzh.easyshell.mariadb.table.MariadbAlertTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbCreateTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.trigger.MariadbTrigger;
import cn.oyzh.easyshell.mariadb.view.MariadbAlertViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbCreateViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbExecuteResult;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbExplainResult;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeItem;
import cn.oyzh.easyshell.trees.mariadb.event.ShellMariadbEventTreeItem;
import cn.oyzh.easyshell.trees.mariadb.event.ShellMariadbEventsTreeItem;
import cn.oyzh.easyshell.trees.mariadb.function.ShellMariadbFunctionTreeItem;
import cn.oyzh.easyshell.trees.mariadb.function.ShellMariadbFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mariadb.procedure.ShellMariadbProcedureTreeItem;
import cn.oyzh.easyshell.trees.mariadb.procedure.ShellMariadbProceduresTreeItem;
import cn.oyzh.easyshell.trees.mariadb.query.ShellMariadbQueriesTreeItem;
import cn.oyzh.easyshell.trees.mariadb.root.ShellMariadbRootTreeItem;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTableTreeItem;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTablesTreeItem;
import cn.oyzh.easyshell.trees.mariadb.terminal.ShellMariadbTerminalTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewsTreeItem;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbViewFactory;
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
 * MariaDB数据库节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDatabaseTreeItem extends ShellMariadbTreeItem<ShellMariadbDatabaseTreeItemValue> {

    /**
     * 当前值
     */
    private final MariadbDatabase value;

    /**
     * 获取数据库对象
     *
     * @return 数据库对象
     */
    public MariadbDatabase value() {
        return value;
    }

    /**
     * 构造数据库节点
     *
     * @param database 数据库对象
     * @param treeView 树视图
     */
    public ShellMariadbDatabaseTreeItem(MariadbDatabase database, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(true);
        this.value = database;
        this.setValue(new ShellMariadbDatabaseTreeItemValue(this));
    }

    @Override
    public ShellMariadbRootTreeItem parent() {
        return (ShellMariadbRootTreeItem) super.parent();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
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
            FXMenuItem closeDB = MenuItemHelper.closeDatabase(this::closeDB);
            items.add(closeDB);
        }
        FXMenuItem editDB = MenuItemHelper.editDatabase(this::editDB);
        items.add(editDB);
        FXMenuItem dropDB = MenuItemHelper.deleteDatabase(this::delete);
        items.add(dropDB);
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
        ShellMariadbViewFactory.runSqlFile(this.client(), this.dbName());
    }

    /**
     * 传输数据
     */
    private void transportData() {
        ShellMariadbViewFactory.transportData(this.info(), this.dbName());
    }

    /**
     * 转储
     */
    private void dump() {
        ShellMariadbViewFactory.dumpData(this.client(), this.dbName(), null, 1);
    }

    @Override
    public void delete() {
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> {
                    if (MessageBox.confirm(I18nHelper.deleteDatabase() + "[" + this.dbName() + "]")) {
                        if (this.parent().dropDatabase(this.dbName())) {
                            ShellMariadbEventUtil.databaseDropped(this);
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
        ShellMariadbViewFactory.databaseUpdate(this.value, this.parent());
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        this.clearChild();
        this.collapse();
        this.setLoaded(false);
        ShellMariadbEventUtil.databaseClosed(this);
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<TreeItem<?>> typeItems = new ArrayList<>();
                        typeItems.add(new ShellMariadbTablesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellMariadbViewsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellMariadbFunctionsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellMariadbProceduresTreeItem(this.getTreeView()));
                        typeItems.add(new ShellMariadbEventsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellMariadbQueriesTreeItem(this.getTreeView()));
                        typeItems.add(new ShellMariadbTerminalTreeItem(this.getTreeView()));
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
    public ShellMariadbTablesTreeItem getTableTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellMariadbTablesTreeItem treeItem) {
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
    public List<ShellMariadbTableTreeItem> getTableChild() {
        List<ShellMariadbTableTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getTableTypeChild().richChildren()) {
            if (child instanceof ShellMariadbTableTreeItem treeItem) {
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
    public ShellMariadbQueriesTreeItem getQueryTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellMariadbQueriesTreeItem treeItem) {
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
    public ShellMariadbFunctionsTreeItem getFunctionTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellMariadbFunctionsTreeItem treeItem) {
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
    public List<ShellMariadbFunctionTreeItem> getFunctionChild() {
        List<ShellMariadbFunctionTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getFunctionTypeChild().richChildren()) {
            if (child instanceof ShellMariadbFunctionTreeItem treeItem) {
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
    public ShellMariadbProceduresTreeItem getProcedureTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellMariadbProceduresTreeItem treeItem) {
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
    public List<ShellMariadbProcedureTreeItem> getProcedureChild() {
        List<ShellMariadbProcedureTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getProcedureTypeChild().richChildren()) {
            if (child instanceof ShellMariadbProcedureTreeItem treeItem) {
                list.add(treeItem);
            }
        }
        return list;
    }

    /**
     * 获取事件类型子节点
     *
     * @return 事件类型子节点
     */
    public ShellMariadbEventsTreeItem getEventTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellMariadbEventsTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取事件节点列表
     *
     * @return 事件节点列表
     */
    public List<ShellMariadbEventTreeItem> getEventChild() {
        List<ShellMariadbEventTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getEventTypeChild().richChildren()) {
            if (child instanceof ShellMariadbEventTreeItem treeItem) {
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
    public ShellMariadbViewsTreeItem getViewTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellMariadbViewsTreeItem treeItem) {
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
    public List<ShellMariadbViewTreeItem> getViewChild() {
        List<ShellMariadbViewTreeItem> list = new ArrayList<>();
        for (RichTreeItem<?> child : this.getViewTypeChild().richChildren()) {
            if (child instanceof ShellMariadbViewTreeItem treeItem) {
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
    public ShellMariadbClient client() {
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
        return this.client().tableSize(this.dbName());
    }

    /**
     * 获取视图数量
     *
     * @return 视图数量
     */
    public int viewSize() {
        return this.client().viewSize(this.dbName());
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
     * @param table       表对象
     * @param columns     列信息
     * @param indexes     索引
     * @param foreignKeys 外键
     * @param triggers    触发器
     * @param checks      检查约束
     */
    public void createTable(MariadbTable table, MariadbColumns columns, DBObjects<MariadbIndex> indexes, DBObjects<MariadbForeignKey> foreignKeys, DBObjects<MariadbTrigger> triggers, DBObjects<MariadbCheck> checks) {
        MariadbCreateTableParam param = new MariadbCreateTableParam();
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
    public void createTable(MariadbCreateTableParam param) {
        this.client().createTable(param);
    }

    /**
     * 构建创建表参数
     *
     * @param table       表对象
     * @param columns     列信息
     * @param indexes     索引
     * @param foreignKeys 外键
     * @param triggers    触发器
     * @param checks      检查约束
     * @return 创建表参数
     */
    public MariadbCreateTableParam createTableParam(MariadbTable table, MariadbColumns columns, DBObjects<MariadbIndex> indexes, DBObjects<MariadbForeignKey> foreignKeys, DBObjects<MariadbTrigger> triggers, DBObjects<MariadbCheck> checks) {
        MariadbCreateTableParam param = new MariadbCreateTableParam();
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
     * @param table       表对象
     * @param columns     列信息
     * @param indexes     索引
     * @param foreignKeys 外键
     * @param triggers    触发器
     * @param checks      检查约束
     */
    public void alterTable(MariadbTable table, MariadbColumns columns, DBObjects<MariadbIndex> indexes, DBObjects<MariadbForeignKey> foreignKeys, DBObjects<MariadbTrigger> triggers, DBObjects<MariadbCheck> checks) {
        MariadbAlertTableParam param = new MariadbAlertTableParam();
        param.setTable(table);
        param.setChecks(checks);
        param.setColumns(columns);
        param.setIndexes(indexes);
        param.setTriggers(triggers);
        param.setForeignKeys(foreignKeys);
        param.setExistPrimaryKey(this.existPrimaryKey(table.getName()));
        this.client().alertTable(param);
    }

    /**
     * 修改表
     *
     * @param param 修改表参数
     */
    public void alterTable(MariadbAlertTableParam param) {
        this.client().alertTable(param);
    }

    /**
     * 构建修改表参数
     *
     * @param table       表对象
     * @param columns     列信息
     * @param indexes     索引
     * @param foreignKeys 外键
     * @param triggers    触发器
     * @param checks      检查约束
     * @return 修改表参数
     */
    public MariadbAlertTableParam alterTableParam(MariadbTable table, MariadbColumns columns, DBObjects<MariadbIndex> indexes, DBObjects<MariadbForeignKey> foreignKeys, DBObjects<MariadbTrigger> triggers, DBObjects<MariadbCheck> checks) {
        MariadbAlertTableParam param = new MariadbAlertTableParam();
        param.setTable(table);
        param.setChecks(checks);
        param.setColumns(columns);
        param.setIndexes(indexes);
        param.setTriggers(triggers);
        param.setForeignKeys(foreignKeys);
        param.setExistPrimaryKey(this.existPrimaryKey(table.getName()));
        return param;
    }

    /**
     * 主键是否存在
     *
     * @param tableName 表名称
     * @return 是否存在
     */
    public boolean existPrimaryKey(String tableName) {
        return this.client().existPrimaryKey(this.dbName(), tableName);
    }

    /**
     * 重命名表
     *
     * @param oldTableName 原表名称
     * @param newTableName 新表名称
     */
    public void renameTable(String oldTableName, String newTableName) {
        this.client().renameTable(this.dbName(), oldTableName, newTableName);
    }

    /**
     * 重命名视图
     *
     * @param oldViewName 视图名称
     * @param newViewName 新视图名称
     */
    public void renameView(String oldViewName, String newViewName) {
        this.client().renameView(this.dbName(), oldViewName, newViewName);
    }

    /**
     * 重命名事件
     *
     * @param oldEventName 事件名称
     * @param newEventName 新事件名称
     */
    public void renameEvent(String oldEventName, String newEventName) {
        this.client().renameEvent(this.dbName(), oldEventName, newEventName);
    }

    /**
     * 重命名函数
     *
     * @param oldFunctionName 函数名称
     * @param newFunctionName 新函数名称
     */
    public void renameFunction(String oldFunctionName, String newFunctionName) {
        this.client().renameFunction(this.dbName(), oldFunctionName, newFunctionName);
    }

    /**
     * 重命名过程
     *
     * @param oldProcedureName 过程名称
     * @param newProcedureName 新过程名称
     */
    public void renameProcedure(String oldProcedureName, String newProcedureName) {
        this.client().renameProcedure(this.dbName(), oldProcedureName, newProcedureName);
    }

    /**
     * 清空表
     *
     * @param tableName 表名称
     */
    public void clearTable(String tableName) {
        this.client().clearTable(this.dbName(), tableName);
    }

    /**
     * 清空表数据并重置自增
     *
     * @param tableName 表名称
     */
    public void truncateTable(String tableName) {
        this.client().truncateTable(this.dbName(), tableName);
    }

    /**
     * 删除表
     *
     * @param tableName 表名称
     */
    public void dropTable(String tableName) {
        this.client().dropTable(this.dbName(), tableName);
    }

    /**
     * 执行sql
     *
     * @param sql sql语句
     * @return 执行结果集
     */
    public DBQueryResults<ShellMariadbExecuteResult> executeSql(String sql) {
        return this.client().executeSql(this.dbName(), sql);
    }

    /**
     * 执行单条sql
     *
     * @param sql sql语句
     * @return 执行结果
     */
    public ShellMariadbExecuteResult executeSingleSql(String sql) {
        return this.client().executeSingleSql(this.dbName(), sql);
    }

    /**
     * 解析sql
     *
     * @param sql sql语句
     * @return 执行结果集
     */
    public DBQueryResults<ShellMariadbExplainResult> explainSql(String sql) {
        return this.client().explainSql(this.dbName(), sql);
    }

    /**
     * 创建函数
     *
     * @param function 函数对象
     */
    public void createFunction(MariadbFunction function) {
        MariadbCreateFunctionParam param = new MariadbCreateFunctionParam();
        param.setFunction(function);
        param.setDbName(this.dbName());
        this.client().createFunction(param);
    }

    /**
     * 修改函数
     *
     * @param function 函数对象
     */
    public void alertFunction(MariadbFunction function) {
        MariadbAlertFunctionParam param = new MariadbAlertFunctionParam();
        param.setFunction(function);
        param.setDbName(this.dbName());
        this.client().alertFunction(param);
    }

    /**
     * 删除函数
     *
     * @param function 函数对象
     */
    public void dropFunction(MariadbFunction function) {
        this.client().dropFunction(this.dbName(), function);
    }

    /**
     * 查询过程
     *
     * @param procedureName 过程名称
     * @return 过程对象
     */
    public MariadbProcedure selectProcedure(String procedureName) {
        return this.client().selectProcedure(this.dbName(), procedureName);
    }

    /**
     * 创建过程
     *
     * @param procedure 过程对象
     */
    public void createProcedure(MariadbProcedure procedure) {
        MariadbCreateProcedureParam param = new MariadbCreateProcedureParam();
        param.setDbName(this.dbName());
        param.setProcedure(procedure);
        this.client().createProcedure(param);
    }

    /**
     * 修改过程
     *
     * @param procedure 过程对象
     */
    public void alertProcedure(MariadbProcedure procedure) {
        MariadbAlertProcedureParam param = new MariadbAlertProcedureParam();
        param.setDbName(this.dbName());
        param.setProcedure(procedure);
        this.client().alertProcedure(param);
    }

    /**
     * 删除过程
     *
     * @param procedure 过程对象
     */
    public void dropProcedure(MariadbProcedure procedure) {
        this.client().dropProcedure(this.dbName(), procedure);
    }

    /**
     * 查询函数
     *
     * @param functionName 函数名称
     * @return 函数对象
     */
    public MariadbFunction selectFunction(String functionName) {
        return this.client().selectFunction(this.dbName(), functionName);
    }

    /**
     * 查询视图
     *
     * @param viewName 视图名称
     * @return 视图对象
     */
    public MariadbView selectView(String viewName) {
        return this.client().selectView(this.dbName(), viewName);
    }

    /**
     * 查询表
     *
     * @param tableName 表名称
     * @return 表对象
     */
    public MariadbTable selectTable(String tableName) {
        return this.client().selectTable(this.dbName(), tableName);
    }

    /**
     * 创建视图
     *
     * @param view 视图对象
     */
    public void createView(MariadbView view) {
        MariadbCreateViewParam param = new MariadbCreateViewParam();
        param.setView(view);
        param.setDbName(this.dbName());
        this.client().createView(param);
    }

    /**
     * 修改视图
     *
     * @param view 视图对象
     */
    public void alertView(MariadbView view) {
        MariadbAlertViewParam param = new MariadbAlertViewParam();
        param.setView(view);
        param.setDbName(this.dbName());
        this.client().alertView(param);
    }

    /**
     * 删除视图
     *
     * @param view 视图对象
     */
    public void dropView(MariadbView view) {
        this.client().dropView(this.dbName(), view);
    }

    /**
     * 视图是否存在
     *
     * @param viewName 视图名称
     * @return 是否存在
     */
    public boolean existView(String viewName) {
        return this.client().existView(this.dbName(), viewName);
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

    /**
     * 查询事件
     *
     * @param eventName 事件名称
     * @return 事件对象
     */
    public MariadbEvent selectEvent(String eventName) {
        return this.client().selectEvent(this.dbName(), eventName);
    }

    /**
     * 修改事件
     *
     * @param event 事件对象
     */
    public void alertEvent(MariadbEvent event) {
        this.client().alertEvent(this.dbName(), event);
    }

    /**
     * 创建事件
     *
     * @param event 事件对象
     */
    public void createEvent(MariadbEvent event) {
        this.client().createEvent(this.dbName(), event);
    }

    /**
     * 删除事件
     *
     * @param event 事件对象
     */
    public void dropEvent(MariadbEvent event) {
        this.client().dropEvent(this.dbName(), event);
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
     * @return 受影响行数
     */
    public int deleteRecord(MariadbDeleteRecordParam param) {
        return this.client().deleteRecord(param);
    }

    /**
     * 获取表检查约束列表
     *
     * @param tableName 表名称
     * @return 表检查约束列表
     */
    public List<MariadbCheck> checks(String tableName) {
        return this.client().selectChecks(this.dbName(), tableName);
    }

    /**
     * 获取表触发器列表
     *
     * @param tableName 表名称
     * @return 表触发器列表
     */
    public List<MariadbTrigger> triggers(String tableName) {
        return this.client().selectTriggers(this.dbName(), tableName);
    }

    /**
     * 获取表列列表
     *
     * @param tableName 表名称
     * @return 表列列表
     */
    public List<MariadbColumn> columns(String tableName) {
        MariadbSelectColumnParam param = new MariadbSelectColumnParam();
        param.setDbName(this.dbName());
        param.setTableName(tableName);
        return this.client().selectColumns(param);
    }

    /**
     * 获取表索引列表
     *
     * @param tableName 表名称
     * @return 表索引列表
     */
    public List<MariadbIndex> indexes(String tableName) {
        return this.client().selectIndexes(this.dbName(), tableName);
    }

    /**
     * 获取表外键列表
     *
     * @param tableName 表名称
     * @return 表外键列表
     */
    public List<MariadbForeignKey> foreignKeys(String tableName) {
        return this.client().selectForeignKeys(this.dbName(), tableName);
    }

    /**
     * 查询单条记录
     *
     * @param param 查询记录参数
     * @return 记录
     */
    public MariadbRecord selectRecord(MariadbSelectRecordParam param) {
        return this.client().selectRecord(param);
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
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
        this.client().cloneTable(this.dbName(), tableName, newTableName, includeRecord);
    }

    /**
     * 克隆视图
     *
     * @param viewName    视图名称
     * @param newViewName 新视图名称
     */
    public void cloneView(String viewName, String newViewName) {
        this.client().cloneView(this.dbName(), viewName, newViewName);
    }

    /**
     * 克隆函数
     *
     * @param functionName    函数名称
     * @param newFunctionName 新函数名称
     */
    public void cloneFunction(String functionName, String newFunctionName) {
        this.client().cloneFunction(this.dbName(), functionName, newFunctionName);
    }

    /**
     * 克隆过程
     *
     * @param procedureName    过程名称
     * @param newProcedureName 新过程名称
     */
    public void cloneProcedure(String procedureName, String newProcedureName) {
        this.client().cloneProcedure(this.dbName(), procedureName, newProcedureName);
    }

    /**
     * 克隆事件
     *
     * @param eventName    事件名称
     * @param newEventName 新事件名称
     */
    public void cloneEvent(String eventName, String newEventName) {
        this.client().cloneEvent(this.dbName(), eventName, newEventName);
    }
}
