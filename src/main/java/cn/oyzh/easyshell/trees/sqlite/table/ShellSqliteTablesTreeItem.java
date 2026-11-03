package cn.oyzh.easyshell.trees.sqlite.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite表类型节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTablesTreeItem extends ShellSqliteTreeItem<ShellSqliteTablesTreeItemValue> {

    /**
     * 表数量缓存
     */
    private Integer tableSize;

    /**
     * 构造表类型节点
     *
     * @param treeView 树视图
     */
    public ShellSqliteTablesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellSqliteTablesTreeItemValue(this));
    }

    @Override
    public ShellSqliteRootTreeItem parent() {
        return (ShellSqliteRootTreeItem) super.parent();
    }

    /**
     * 获取SQLite客户端
     *
     * @return SQLite客户端
     */
    public ShellSqliteClient client() {
        return this.parent().client();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return this.parent().infoName();
    }

    /**
     * 获取表数量
     *
     * @return 表数量
     */
    public int tableSize() {
        try {
            return this.client().tableSize(this.dbName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取表数量（带缓存）
     *
     * @return 表数量
     */
    public int getTableSize() {
        if (this.tableSize == null) {
            this.tableSize = this.tableSize();
        }
        return this.tableSize;
    }

    /**
     * 清空表数量缓存
     */
    public void clearTableSize() {
        this.tableSize = null;
    }

    @Override
    public void loadChild() {
        if (this.isLoading() || this.isLoaded()) {
            return;
        }
        try {
            this.setLoading(true);
            this.clearChild();
            List<TreeItem<?>> items = new ArrayList<>();
            for (SQLiteTable table : this.client().selectTables()) {
                items.add(new ShellSqliteTableTreeItem(table, this.getTreeView()));
            }
            this.setChild(items);
            this.clearTableSize();
            this.setLoaded(true);
            this.doFilter();
            this.doSort();
            this.expend();
            this.refresh();
        } catch (Exception ex) {
            this.setLoaded(false);
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.setLoading(false);
        }
    }

    /**
     * 新增表并刷新排序
     *
     * @param table 表对象
     */
    public void addTable(SQLiteTable table) {
        if (table == null || StringUtil.isBlank(table.getName())) {
            return;
        }
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellSqliteTableTreeItem item
                    && StringUtil.equals(item.tableName(), table.getName())) {
                this.refreshCount();
                return;
            }
        }
        this.addChild(new ShellSqliteTableTreeItem(table, this.getTreeView()));
        this.doFilter();
        this.doSort();
        this.expend();
        this.refreshCount();
    }

    /**
     * 刷新表数量
     */
    public void refreshCount() {
        this.clearTableSize();
        this.refresh();
    }

    @Override
    public void reloadChild() {
        this.clearTableSize();
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addTable(() -> ShellSqliteEventUtil.designTable(this.client(), null));
        FXMenuItem reload = MenuItemHelper.reloadData(this::reloadChild);
        FXMenuItem exportData = MenuItemHelper.exportData(() -> ShellSqliteViewFactory.exportData(this.client(), null));
        FXMenuItem importData = MenuItemHelper.importData(() -> ShellSqliteViewFactory.importData(this.client()));
        items.add(add);
        items.add(reload);
        items.add(exportData);
        items.add(importData);
        return items;
    }
}
