package cn.oyzh.easyshell.trees.sqlite;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteTabPane;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTablesTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * SQLite数据库树视图
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTreeView extends RichTreeView implements FXEventListener {

    /**
     * SQLite客户端
     */
    private ShellSqliteClient client;

    /**
     * 数据库标签页
     */
    private ShellSqliteTabPane tabPane;

    /**
     * 构造SQLite数据库树视图
     */
    public ShellSqliteTreeView() {
        this.dragContent = "sqlite_tree_drag";
        this.itemFilter = new ShellSqliteTreeItemFilter();
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellSqliteRootTreeItem(this));
    }

    @Override
    public ShellSqliteTreeItemFilter getItemFilter() {
        if (this.itemFilter == null) {
            this.itemFilter = new ShellSqliteTreeItemFilter();
        }
        return (ShellSqliteTreeItemFilter) this.itemFilter;
    }

    /**
     * 设置SQLite客户端
     *
     * @param client SQLite客户端
     */
    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }

    /**
     * 获取SQLite客户端
     *
     * @return SQLite客户端
     */
    public ShellSqliteClient getClient() {
        return this.client;
    }

    /**
     * 设置数据库标签页
     *
     * @param tabPane 数据库标签页
     */
    public void setTabPane(ShellSqliteTabPane tabPane) {
        this.tabPane = tabPane;
    }

    /**
     * 获取数据库标签页
     *
     * @return 数据库标签页
     */
    public ShellSqliteTabPane getTabPane() {
        return this.tabPane;
    }

    @Override
    public ShellSqliteRootTreeItem root() {
        return (ShellSqliteRootTreeItem) super.root();
    }

    /**
     * 查找表节点
     *
     * @param tableName 表名称
     * @return 表节点
     */
    public ShellSqliteTableTreeItem findTableItem(String tableName) {
        return this.findTableItem(this.root(), tableName);
    }

    /**
     * 查找表节点
     *
     * @param parent    父节点
     * @param tableName 表名称
     * @return 表节点
     */
    private ShellSqliteTableTreeItem findTableItem(TreeItem<?> parent, String tableName) {
        if (parent instanceof ShellSqliteTablesTreeItem group) {
            if (!group.isLoaded()) {
                group.loadChild();
            }
        }
        for (TreeItem<?> child : parent.getChildren()) {
            if (child instanceof ShellSqliteTableTreeItem item && item.tableName().equals(tableName)) {
                return item;
            }
            ShellSqliteTableTreeItem result = this.findTableItem(child, tableName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * 查找视图节点
     *
     * @param viewName 视图名称
     * @return 视图节点
     */
    public ShellSqliteViewTreeItem findViewItem(String viewName) {
        return this.findViewItem(this.root(), viewName);
    }

    /**
     * 查找视图节点
     *
     * @param parent   父节点
     * @param viewName 视图名称
     * @return 视图节点
     */
    private ShellSqliteViewTreeItem findViewItem(TreeItem<?> parent, String viewName) {
        if (parent instanceof ShellSqliteViewsTreeItem group) {
            if (!group.isLoaded()) {
                group.loadChild();
            }
        }
        for (TreeItem<?> child : parent.getChildren()) {
            if (child instanceof ShellSqliteViewTreeItem item && item.viewName().equals(viewName)) {
                return item;
            }
            ShellSqliteViewTreeItem result = this.findViewItem(child, viewName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
