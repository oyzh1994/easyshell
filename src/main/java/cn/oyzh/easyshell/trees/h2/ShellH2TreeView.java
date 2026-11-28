package cn.oyzh.easyshell.trees.h2;

import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.tabs.h2.ShellH2TabPane;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TableTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TablesTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * H2数据库树视图
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TreeView extends RichTreeView implements FXEventListener {

    /**
     * H2客户端
     */
    private ShellH2Client client;

    /**
     * 数据库标签页
     */
    private ShellH2TabPane tabPane;

    /**
     * 构造H2数据库树视图
     */
    public ShellH2TreeView() {
        this.dragContent = "h2_tree_drag";
        this.itemFilter = new ShellH2TreeItemFilter();
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellH2RootTreeItem(this));
    }

    @Override
    public ShellH2TreeItemFilter getItemFilter() {
        if (this.itemFilter == null) {
            this.itemFilter = new ShellH2TreeItemFilter();
        }
        return (ShellH2TreeItemFilter) this.itemFilter;
    }

    /**
     * 设置H2客户端
     *
     * @param client H2客户端
     */
    public void setClient(ShellH2Client client) {
        this.client = client;
    }

    /**
     * 获取H2客户端
     *
     * @return H2客户端
     */
    public ShellH2Client getClient() {
        return this.client;
    }

    /**
     * 设置数据库标签页
     *
     * @param tabPane 数据库标签页
     */
    public void setTabPane(ShellH2TabPane tabPane) {
        this.tabPane = tabPane;
    }

    /**
     * 获取数据库标签页
     *
     * @return 数据库标签页
     */
    public ShellH2TabPane getTabPane() {
        return this.tabPane;
    }

    @Override
    public ShellH2RootTreeItem root() {
        return (ShellH2RootTreeItem) super.root();
    }

    /**
     * 查找表节点
     *
     * @param tableName 表名称
     * @return 表节点
     */
    public ShellH2TableTreeItem findTableItem(String tableName) {
        return this.findTableItem(this.root(), this.client == null ? null : this.client.schema(), tableName);
    }

    /**
     * 查找指定模式中的表节点。
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 表节点
     */
    public ShellH2TableTreeItem findTableItem(String schema, String tableName) {
        ShellH2SchemaTreeItem schemaItem = schema == null
                ? null : this.root().schemaGroup(schema);
        return schemaItem == null
                ? this.findTableItem(this.root(), schema, tableName)
                : this.findTableItem(schemaItem, schema, tableName);
    }

    /**
     * 查找表节点
     *
     * @param parent    父节点
     * @param tableName 表名称
     * @return 表节点
     */
    private ShellH2TableTreeItem findTableItem(TreeItem<?> parent, String schema, String tableName) {
        if (parent instanceof ShellH2SchemaTreeItem schemaItem && !schemaItem.isLoaded()) {
            schemaItem.loadChild();
        }
        if (parent instanceof ShellH2TablesTreeItem group) {
            if (!group.isLoaded()) {
                group.loadChild();
            }
        }
        for (TreeItem<?> child : parent.getChildren()) {
            if (child instanceof ShellH2TableTreeItem item
                    && item.tableName().equals(tableName)
                    && (schema == null || item.dbName().equalsIgnoreCase(schema))) {
                return item;
            }
            ShellH2TableTreeItem result = this.findTableItem(child, schema, tableName);
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
    public ShellH2ViewTreeItem findViewItem(String viewName) {
        return this.findViewItem(this.root(), this.client == null ? null : this.client.schema(), viewName);
    }

    public ShellH2ViewTreeItem findViewItem(String schema, String viewName) {
        ShellH2SchemaTreeItem schemaItem = schema == null
                ? null : this.root().schemaGroup(schema);
        return schemaItem == null
                ? this.findViewItem(this.root(), schema, viewName)
                : this.findViewItem(schemaItem, schema, viewName);
    }

    /**
     * 查找视图节点
     *
     * @param parent   父节点
     * @param viewName 视图名称
     * @return 视图节点
     */
    private ShellH2ViewTreeItem findViewItem(TreeItem<?> parent, String schema, String viewName) {
        if (parent instanceof ShellH2SchemaTreeItem schemaItem && !schemaItem.isLoaded()) {
            schemaItem.loadChild();
        }
        if (parent instanceof ShellH2ViewsTreeItem group) {
            if (!group.isLoaded()) {
                group.loadChild();
            }
        }
        for (TreeItem<?> child : parent.getChildren()) {
            if (child instanceof ShellH2ViewTreeItem item
                    && item.viewName().equals(viewName)
                    && (schema == null || item.dbName().equalsIgnoreCase(schema))) {
                return item;
            }
            ShellH2ViewTreeItem result = this.findViewItem(child, schema, viewName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
