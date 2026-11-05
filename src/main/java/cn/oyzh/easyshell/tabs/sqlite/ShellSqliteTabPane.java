package cn.oyzh.easyshell.tabs.sqlite;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.sqlite.function.ShellSqliteFunctionChangedEvent;
import cn.oyzh.easyshell.event.sqlite.function.ShellSqliteFunctionDesignEvent;
import cn.oyzh.easyshell.event.sqlite.query.ShellSqliteQueryChangedEvent;
import cn.oyzh.easyshell.event.sqlite.query.ShellSqliteQueryDeletedEvent;
import cn.oyzh.easyshell.event.sqlite.query.ShellSqliteQueryOpenEvent;
import cn.oyzh.easyshell.event.sqlite.query.ShellSqliteQueryRenamedEvent;
import cn.oyzh.easyshell.event.sqlite.table.ShellSqliteTableChangedEvent;
import cn.oyzh.easyshell.event.sqlite.table.ShellSqliteTableDesignEvent;
import cn.oyzh.easyshell.event.sqlite.table.ShellSqliteTableOpenEvent;
import cn.oyzh.easyshell.event.sqlite.terminal.ShellSqliteTerminalOpenEvent;
import cn.oyzh.easyshell.event.sqlite.view.ShellSqliteViewChangedEvent;
import cn.oyzh.easyshell.event.sqlite.view.ShellSqliteViewDesignEvent;
import cn.oyzh.easyshell.event.sqlite.view.ShellSqliteViewOpenEvent;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryUtil;
import cn.oyzh.easyshell.tabs.sqlite.function.ShellSqliteFunctionDesignTab;
import cn.oyzh.easyshell.tabs.sqlite.home.ShellSqliteHomeTab;
import cn.oyzh.easyshell.tabs.sqlite.query.ShellSqliteQueryMainTab;
import cn.oyzh.easyshell.tabs.sqlite.table.ShellSqliteTableDesignTab;
import cn.oyzh.easyshell.tabs.sqlite.table.ShellSqliteTableRecordTab;
import cn.oyzh.easyshell.tabs.sqlite.terminal.ShellSqliteTerminalTab;
import cn.oyzh.easyshell.tabs.sqlite.view.ShellSqliteViewDesignTab;
import cn.oyzh.easyshell.tabs.sqlite.view.ShellSqliteViewRecordTab;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeView;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

import java.util.function.Consumer;

/**
 * SQLite 标签页容器。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTabPane extends RichTabPane implements FXEventListener {

    private SimpleObjectProperty<ShellSqliteClient> clientProperty;

    /**
     * 树视图
     */
    private ShellSqliteTreeView treeView;

    private Consumer<String> viewAddedHandler;

    private Runnable viewCountChangedHandler;

    private Consumer<String> tableAddedHandler;

    private Runnable tableCountChangedHandler;

    private Consumer<ShellQuery> queryChangedHandler;

    private Consumer<ShellQuery> queryDeletedHandler;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellSqliteClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellSqliteClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 获取客户端属性
     *
     * @return 客户端属性
     */
    public SimpleObjectProperty<ShellSqliteClient> clientProperty() {
        if (this.clientProperty == null) {
            this.clientProperty = new SimpleObjectProperty<>();
        }
        return this.clientProperty;
    }

    /**
     * 获取数据库树
     *
     * @return 数据库树
     */
    public ShellSqliteTreeView getTreeView() {
        return treeView;
    }

    /**
     * 设置数据库树
     *
     * @param treeView 数据库树
     */
    public void setTreeView(ShellSqliteTreeView treeView) {
        this.treeView = treeView;
    }

    /**
     * 设置视图新增处理器
     *
     * @param viewAddedHandler 视图新增处理器
     */
    public void setViewAddedHandler(Consumer<String> viewAddedHandler) {
        this.viewAddedHandler = viewAddedHandler;
    }

    /**
     * 设置视图数量变更处理器
     *
     * @param viewCountChangedHandler 视图数量变更处理器
     */
    public void setViewCountChangedHandler(Runnable viewCountChangedHandler) {
        this.viewCountChangedHandler = viewCountChangedHandler;
    }

    /**
     * 设置表新增处理器
     *
     * @param tableAddedHandler 表新增处理器
     */
    public void setTableAddedHandler(Consumer<String> tableAddedHandler) {
        this.tableAddedHandler = tableAddedHandler;
    }

    /**
     * 设置表数量变更处理器
     *
     * @param tableCountChangedHandler 表数量变更处理器
     */
    public void setTableCountChangedHandler(Runnable tableCountChangedHandler) {
        this.tableCountChangedHandler = tableCountChangedHandler;
    }

    /**
     * 设置查询变更处理器
     *
     * @param queryChangedHandler 查询变更处理器
     */
    public void setQueryChangedHandler(Consumer<ShellQuery> queryChangedHandler) {
        this.queryChangedHandler = queryChangedHandler;
    }

    /**
     * 设置查询删除处理器
     *
     * @param queryDeletedHandler 查询删除处理器
     */
    public void setQueryDeletedHandler(Consumer<ShellQuery> queryDeletedHandler) {
        this.queryDeletedHandler = queryDeletedHandler;
    }

    @Override
    public void initNode() {
        super.initNode();
        this.initHomeTab();
        this.getTabs().addListener((ListChangeListener<? super Tab>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    TaskManager.startDelay(this::flushHomeTab, 100);
                }
            }
        });
    }

    private void flushHomeTab() {
        if (this.tabsEmpty()) {
            this.initHomeTab();
        } else if (this.tabsSize() > 1 && this.getHomeTab() != null) {
            this.closeTab(ShellSqliteHomeTab.class);
        }
    }

    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            this.addTab(new ShellSqliteHomeTab());
        }
    }

    /**
     * 获取主页标签页
     *
     * @return 主页标签页
     */
    public ShellSqliteHomeTab getHomeTab() {
        return this.getTab(ShellSqliteHomeTab.class);
    }

    /**
     * 显示主页
     */
    public void showHome() {
        this.initHomeTab();
        ShellSqliteHomeTab tab = this.getHomeTab();
        if (tab != null) {
            this.select(tab);
        }
    }

    /**
     * 打开新增查询标签页
     */
    public void openQuery() {
        this.openQuery(new ShellQuery());
    }

    /**
     * 打开查询标签页
     *
     * @param query 查询对象
     */
    public void openQuery(ShellQuery query) {
        if (query != null && StringUtil.isNotBlank(query.getUid())) {
            for (Tab item : this.getTabs()) {
                if (item instanceof ShellSqliteQueryMainTab tab
                        && StringUtil.equals(tab.query().getUid(), query.getUid())) {
                    this.select(tab);
                    return;
                }
            }
        }
        ShellSqliteQueryMainTab tab = new ShellSqliteQueryMainTab();
        this.addTab(tab);
        if (query == null) {
            query = new ShellQuery();
        }
        tab.init(query, this.treeView.root());
        this.select(tab);
    }

    /**
     * 打开对象记录标签页
     *
     * @param objectName 对象名称
     * @param view       是否视图
     */
    public void openObject(String objectName, boolean view) {
        if (!view) {
            this.openTableRecord(objectName);
            return;
        }
        this.openViewRecord(objectName);
    }

    /**
     * 打开表记录标签页
     *
     * @param tableName 表名称
     */
    public void openTableRecord(String tableName) {
        ShellSqliteTableRecordTab tab = this.getTableRecordTab(tableName);
        if (tab == null) {
            ShellSqliteTableTreeItem item = this.treeView == null ? null : this.treeView.findTableItem(tableName);
            if (item == null) {
                return;
            }
            tab = new ShellSqliteTableRecordTab();
            this.addTab(tab);
            tab.init(item);
        }
        this.select(tab);
    }

    /**
     * 打开视图记录标签页
     *
     * @param viewName 视图名称
     */
    public void openViewRecord(String viewName) {
        ShellSqliteViewRecordTab tab = this.getViewRecordTab(viewName);
        if (tab == null) {
            ShellSqliteViewTreeItem item = this.treeView == null ? null : this.treeView.findViewItem(viewName);
            if (item == null) {
                return;
            }
            try {
                item.columns();
            } catch (Exception ex) {
                this.openViewDesign(viewName);
                return;
            }
            tab = new ShellSqliteViewRecordTab();
            this.addTab(tab);
            tab.init(item);
        }
        this.select(tab);
    }

    /**
     * 打开终端标签页
     */
    public void openTerminal() {
        ShellSqliteTerminalTab tab = new ShellSqliteTerminalTab();
        this.addTab(tab);
        tab.init(this.treeView.root());
        this.select(tab);
    }

    /**
     * 打开表设计标签页
     *
     * @param tableName 表名称
     */
    public void openTableDesign(String tableName) {
        for (Tab item : this.getTabs()) {
            if (item instanceof ShellSqliteTableDesignTab tab && tableName != null
                    && tableName.equals(tab.tableName())) {
                this.select(tab);
                return;
            }
        }
        SQLiteTable table = tableName == null ? null : this.getClient().selectTable(tableName);
        ShellSqliteTableDesignTab tab = new ShellSqliteTableDesignTab();
        this.addTab(tab);
        tab.init(table, this.treeView.root());
        this.select(tab);
    }

    /**
     * 打开视图设计标签页
     *
     * @param viewName 视图名称
     */
    public void openViewDesign(String viewName) {
        for (Tab item : this.getTabs()) {
            if (item instanceof ShellSqliteViewDesignTab tab && viewName != null
                    && viewName.equals(tab.viewName())) {
                this.select(tab);
                return;
            }
        }
        SQLiteView view = viewName == null ? null : this.getClient().selectView(viewName);
        ShellSqliteViewDesignTab tab = new ShellSqliteViewDesignTab();
        this.addTab(tab);
        tab.init(view, this.treeView.root());
        this.select(tab);
    }

    /**
     * 打开函数设计标签页
     *
     * @param functionName 函数名称
     */
    public void openFunctionDesign(String functionName) {
        for (Tab item : this.getTabs()) {
            if (item instanceof ShellSqliteFunctionDesignTab tab && functionName != null
                    && functionName.equals(tab.functionName())) {
                this.select(tab);
                return;
            }
        }
        SQLiteFunction function = null;
        if (functionName != null) {
            function = this.getClient().selectFunctions().stream()
                    .filter(item -> functionName.equals(item.getName()))
                    .findFirst().orElse(null);
        }
        ShellSqliteFunctionDesignTab tab = new ShellSqliteFunctionDesignTab();
        this.addTab(tab);
        tab.init(function, this.treeView.root());
        this.select(tab);
    }

    @EventSubscribe
    private void onTableOpen(ShellSqliteTableOpenEvent event) {
        if (event.client() == this.getClient()) {
            this.openObject(event.tableName(), false);
        }
    }

    @EventSubscribe
    private void onTableDesign(ShellSqliteTableDesignEvent event) {
        if (event.client() == this.getClient()) {
            this.openTableDesign(event.tableName());
        }
    }

    @EventSubscribe
    private void onTableChanged(ShellSqliteTableChangedEvent event) {
        if (event.client() != this.getClient()) {
            return;
        }
        boolean created = StringUtil.isBlank(event.originalName()) && StringUtil.isNotBlank(event.tableName());
        boolean deleted = StringUtil.isNotBlank(event.originalName()) && StringUtil.isBlank(event.tableName());
        ShellSqliteTableRecordTab tab = this.getTableRecordTab(event.originalName());
        if (tab != null) {
            if (StringUtil.isBlank(event.tableName())) {
                tab.closeTab();
            } else if (StringUtil.equals(event.originalName(), event.tableName())) {
                tab.reload();
            } else {
                tab.closeTab();
                this.openTableRecord(event.tableName());
            }
        }
        if (created) {
            if (this.tableAddedHandler != null) {
                this.tableAddedHandler.accept(event.tableName());
            }
        } else if (deleted) {
            if (this.tableCountChangedHandler != null) {
                this.tableCountChangedHandler.run();
            }
        }
        ShellSqliteQueryUtil.updateIndexAsync(this.getClient());
    }

    @EventSubscribe
    private void onViewOpen(ShellSqliteViewOpenEvent event) {
        if (event.client() == this.getClient()) {
            this.openObject(event.viewName(), true);
        }
    }

    @EventSubscribe
    private void onViewDesign(ShellSqliteViewDesignEvent event) {
        if (event.client() == this.getClient()) {
            this.openViewDesign(event.viewName());
        }
    }

    @EventSubscribe
    private void onViewChanged(ShellSqliteViewChangedEvent event) {
        if (event.client() != this.getClient()) {
            return;
        }
        boolean created = StringUtil.isBlank(event.originalName()) && StringUtil.isNotBlank(event.viewName());
        boolean deleted = StringUtil.isNotBlank(event.originalName()) && StringUtil.isBlank(event.viewName());
        ShellSqliteViewRecordTab tab = this.getViewRecordTab(event.originalName());
        if (tab != null) {
            if (StringUtil.isBlank(event.viewName())) {
                tab.closeTab();
            } else if (StringUtil.equals(event.originalName(), event.viewName())) {
                tab.reload();
            } else {
                tab.closeTab();
                this.openViewRecord(event.viewName());
            }
        }
        if (created) {
            if (this.viewAddedHandler != null) {
                this.viewAddedHandler.accept(event.viewName());
            }
        } else if (deleted) {
            if (this.viewCountChangedHandler != null) {
                this.viewCountChangedHandler.run();
            }
        }
        ShellSqliteQueryUtil.updateIndexAsync(this.getClient());
    }

    @EventSubscribe
    private void onFunctionDesign(ShellSqliteFunctionDesignEvent event) {
        if (event.client() == this.getClient()) {
            this.openFunctionDesign(event.functionName());
        }
    }

    @EventSubscribe
    private void onFunctionChanged(ShellSqliteFunctionChangedEvent event) {
        if (event.client() != this.getClient() || event.data() == null) {
            return;
        }
        SQLiteFunction function = event.data();
        if (SQLiteFunction.KIND_TABLE_VIEW.equals(function.getKind())
                && this.viewAddedHandler != null) {
            this.viewAddedHandler.accept(function.getName());
        }
        if (this.getClient() != null) {
            ShellSqliteQueryUtil.updateIndexAsync(this.getClient());
        }
    }

    @EventSubscribe
    private void onQueryOpen(ShellSqliteQueryOpenEvent event) {
        if (event.client() == this.getClient()) {
            this.openQuery(event.data());
        }
    }

    @EventSubscribe
    private void onQueryChanged(ShellSqliteQueryChangedEvent event) {
        if (event.client() != this.getClient() || event.data() == null) {
            return;
        }
        for (Tab item : this.getTabs()) {
            if (item instanceof ShellSqliteQueryMainTab tab
                    && StringUtil.equals(tab.query().getUid(), event.data().getUid())) {
                tab.flush();
                break;
            }
        }
        if (this.queryChangedHandler != null) {
            this.queryChangedHandler.accept(event.data());
        }
    }

    @EventSubscribe
    private void onQueryDeleted(ShellSqliteQueryDeletedEvent event) {
        if (event.client() == this.getClient() && event.data() != null) {
            ShellSqliteQueryMainTab tab = this.getQueryTab(event.queryId());
            if (tab != null) {
                this.removeTab(tab);
            }
            if (this.queryDeletedHandler != null) {
                this.queryDeletedHandler.accept(event.data());
            }
        }
    }

    @EventSubscribe
    private void onQueryRenamed(ShellSqliteQueryRenamedEvent event) {
        if (event.client() == this.getClient() && event.data() != null) {
            ShellSqliteQueryMainTab tab = this.getQueryTab(event.queryId());
            if (tab != null) {
                tab.query().setName(event.queryName());
                tab.flush();
            }
            if (this.queryChangedHandler != null) {
                this.queryChangedHandler.accept(event.data());
            }
        }
    }

    @EventSubscribe
    private void onTerminalOpen(ShellSqliteTerminalOpenEvent event) {
        if (event.client() == this.getClient()) {
            this.openTerminal();
        }
    }

    private ShellSqliteTableRecordTab getTableRecordTab(String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqliteTableRecordTab recordTab
                    && StringUtil.equals(recordTab.tableName(), tableName)) {
                return recordTab;
            }
        }
        return null;
    }

    private ShellSqliteViewRecordTab getViewRecordTab(String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqliteViewRecordTab recordTab
                    && StringUtil.equals(recordTab.viewName(), viewName)) {
                return recordTab;
            }
        }
        return null;
    }

    private ShellSqliteQueryMainTab getQueryTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqliteQueryMainTab queryTab
                    && StringUtil.equals(queryTab.query().getUid(), queryId)) {
                return queryTab;
            }
        }
        return null;
    }
}
