package cn.oyzh.easyshell.trees.sqlite.root;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteTabPane;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeView;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionsTreeItem;
import cn.oyzh.easyshell.trees.sqlite.query.ShellSqliteQueriesTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTablesTreeItem;
import cn.oyzh.easyshell.trees.sqlite.terminal.ShellSqliteTerminalTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewsTreeItem;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite树根节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteRootTreeItem extends ShellSqliteTreeItem<ShellSqliteRootTreeItemValue> {

    /**
     * 构造SQLite树根节点
     *
     * @param treeView 树视图
     */
    public ShellSqliteRootTreeItem(ShellSqliteTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        this.setValue(new ShellSqliteRootTreeItemValue());
    }

    /**
     * 获取SQLite客户端
     *
     * @return SQLite客户端
     */
    public ShellSqliteClient client() {
        return this.getTreeView().getClient();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.client().getShellConnect();
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
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect connect() {
        return this.info();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return "main";
    }

    /**
     * 获取数据库标签页
     *
     * @return 数据库标签页
     */
    public ShellSqliteTabPane tabPane() {
        return this.getTreeView().getTabPane();
    }

    @Override
    public void loadChild() {
        this.clearChild();
        List<TreeItem<?>> items = new ArrayList<>();
        items.add(new ShellSqliteTablesTreeItem(this.getTreeView()));
        items.add(new ShellSqliteViewsTreeItem(this.getTreeView()));
        items.add(new ShellSqliteFunctionsTreeItem(this.getTreeView()));
        items.add(new ShellSqliteQueriesTreeItem(this.getTreeView()));
        items.add(new ShellSqliteTerminalTreeItem(this.getTreeView()));
        this.setChild(items);
        this.doFilter();
        this.expend();
    }

    @Override
    public void reloadChild() {
        this.loadChild();
    }

    /**
     * 新增或更新查询节点
     *
     * @param query 查询对象
     */
    public void upsertQuery(ShellQuery query) {
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellSqliteQueriesTreeItem queryGroup) {
                queryGroup.upsertQuery(query);
                return;
            }
        }
    }

    /**
     * 删除查询节点
     *
     * @param query 查询对象
     */
    public void removeQuery(ShellQuery query) {
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellSqliteQueriesTreeItem queryGroup) {
                queryGroup.removeQuery(query);
                return;
            }
        }
    }

    /**
     * 新增视图节点
     *
     * @param view 视图对象
     */
    public void addView(SQLiteView view) {
        ShellSqliteViewsTreeItem viewGroup = this.viewGroup();
        if (viewGroup != null && view != null) {
            viewGroup.addView(view);
        }
    }

    /**
     * 新增视图节点
     *
     * @param viewName 视图名称
     */
    public void addView(String viewName) {
        this.addView(this.client().selectView(viewName));
    }

    /**
     * 新增表节点
     *
     * @param table 表对象
     */
    public void addTable(SQLiteTable table) {
        ShellSqliteTablesTreeItem tableGroup = this.tableGroup();
        if (tableGroup != null && table != null) {
            tableGroup.addTable(table);
        }
    }

    /**
     * 新增表节点
     *
     * @param tableName 表名称
     */
    public void addTable(String tableName) {
        this.addTable(this.client().selectTable(tableName));
    }

    /**
     * 刷新视图数量
     */
    public void refreshViewCount() {
        ShellSqliteViewsTreeItem viewGroup = this.viewGroup();
        if (viewGroup != null) {
            viewGroup.refreshCount();
        }
    }

    /**
     * 刷新表数量
     */
    public void refreshTableCount() {
        ShellSqliteTablesTreeItem tableGroup = this.tableGroup();
        if (tableGroup != null) {
            tableGroup.refreshCount();
        }
    }

    /**
     * 获取表分组节点
     *
     * @return 表分组节点
     */
    private ShellSqliteTablesTreeItem tableGroup() {
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellSqliteTablesTreeItem group) {
                return group;
            }
        }
        return null;
    }

    /**
     * 获取视图分组节点
     *
     * @return 视图分组节点
     */
    private ShellSqliteViewsTreeItem viewGroup() {
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellSqliteViewsTreeItem group) {
                return group;
            }
        }
        return null;
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.tabPane().showHome();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem designTable = MenuItemHelper.designTable(() -> ShellSqliteEventUtil.designTable(this.client(), null));
        FXMenuItem designView = MenuItemHelper.designView(() -> ShellSqliteEventUtil.designView(this.client(), null));
        FXMenuItem designFunction = MenuItemHelper.designFunction(() -> ShellSqliteEventUtil.designFunction(this.client(), null));
        items.add(designTable);
        items.add(designView);
        items.add(designFunction);
        items.add(MenuItemHelper.separator());
        FXMenuItem importData = MenuItemHelper.importData(() -> ShellSqliteViewFactory.importData(this.client()));
        FXMenuItem exportData = MenuItemHelper.exportData(() -> ShellSqliteViewFactory.exportData(this.client(), null));
        FXMenuItem dumpData = MenuItemHelper.dumpData(() -> ShellSqliteViewFactory.dumpData(this.client(), null));
        FXMenuItem runSqlFile = MenuItemHelper.runSqlFile(() -> ShellSqliteViewFactory.runSqlFile(this.client()));
        FXMenuItem transportData = MenuItemHelper.transportData(() -> ShellSqliteViewFactory.transportData(this.info()));
        items.add(importData);
        items.add(exportData);
        items.add(dumpData);
        items.add(runSqlFile);
        items.add(transportData);
        items.add(MenuItemHelper.separator());
        FXMenuItem reloadDatabase = MenuItemHelper.reloadDatabase(this::reloadChild);
        items.add(reloadDatabase);
        return items;
    }
}
