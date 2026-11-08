package cn.oyzh.easyshell.trees.http;

import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.easyshell.trees.http.collection.ShellHttpCollectionsTreeItem;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentsTreeItem;
import cn.oyzh.easyshell.trees.http.root.ShellHttpRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * HTTP 工作区树。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpTreeView extends RichTreeView {

    /**
     * 工作区
     */
    private ShellHttpWorkspace workspace;

    /**
     * 客户端
     */
    private ShellHttpClient client;

    /**
     * 标签页面板
     */
    private ShellHttpTabPane tabPane;

    /**
     * 构造 HTTP 工作区树
     */
    public ShellHttpTreeView() {
        this.setShowRoot(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>)
                param -> new RichTreeCell<>());
    }

    /**
     * 初始化
     *
     * @param workspace 工作区
     * @param client    客户端
     * @param tabPane   标签页面板
     */
    public void init(ShellHttpWorkspace workspace, ShellHttpClient client,
                     ShellHttpTabPane tabPane) {
        this.workspace = workspace;
        this.client = client;
        this.tabPane = tabPane;
        this.loadItems();
    }

    /**
     * 加载节点
     */
    public void loadItems() {
        this.setRoot(new ShellHttpRootTreeItem(this));
        this.root().loadChild();
        this.root().setExpanded(true);
    }

    /**
     * 重新加载节点并尽量保持选中节点
     */
    public void reload() {
        Object selected = this.selectedData();
        this.loadItems();
        if (this.getItemFilter().getKw() != null && !this.getItemFilter().getKw().isBlank()) {
            this.root().doFilter();
        }
        if (selected != null) {
            this.selectData(selected);
        }
    }

    @Override
    public ShellHttpRootTreeItem root() {
        return (ShellHttpRootTreeItem) super.root();
    }

    @Override
    public ShellHttpTreeItemFilter getItemFilter() {
        if (this.itemFilter == null) {
            this.itemFilter = new ShellHttpTreeItemFilter();
        }
        return (ShellHttpTreeItemFilter) this.itemFilter;
    }

    /**
     * 获取工作区
     *
     * @return 工作区
     */
    public ShellHttpWorkspace workspace() {
        return workspace;
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellHttpClient client() {
        return client;
    }

    /**
     * 获取标签页面板
     *
     * @return 标签页面板
     */
    public ShellHttpTabPane getTabPane() {
        return tabPane;
    }

    /**
     * 刷新环境下拉框
     */
    public void refreshEnvironmentCombo() {
        if (this.tabPane != null) {
            this.tabPane.refreshEnvironmentCombo();
        }
    }

    /**
     * 获取集合分类节点
     *
     * @return 集合分类节点
     */
    public ShellHttpCollectionsTreeItem collectionsItem() {
        for (javafx.scene.control.TreeItem<?> child : this.root().unfilteredChildren()) {
            if (child instanceof ShellHttpCollectionsTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取环境分类节点
     *
     * @return 环境分类节点
     */
    public ShellHttpEnvironmentsTreeItem environmentsItem() {
        for (javafx.scene.control.TreeItem<?> child : this.root().unfilteredChildren()) {
            if (child instanceof ShellHttpEnvironmentsTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取当前选中的数据
     *
     * @return 数据
     */
    public Object selectedData() {
        ShellHttpTreeItem<?> item = (ShellHttpTreeItem<?>) this.getSelectedItem();
        return item == null ? null : item.data();
    }

    /**
     * 选中数据
     *
     * @param data 数据
     */
    public void selectData(Object data) {
        ShellHttpTreeItem<?> item = this.findItem(data);
        if (item != null) {
            this.getSelectionModel().select(item);
            int row = this.getRow(item);
            if (row >= 0) {
                this.scrollTo(row);
            }
        }
    }

    /**
     * 查找节点
     *
     * @param data 数据
     * @return 节点
     */
    public ShellHttpTreeItem<?> findItem(Object data) {
        return this.findItem(this.root(), data);
    }

    /**
     * 查找节点
     *
     * @param parent 父节点
     * @param data   数据
     * @return 节点
     */
    private ShellHttpTreeItem<?> findItem(ShellHttpTreeItem<?> parent, Object data) {
        if (parent == null) {
            return null;
        }
        for (TreeItem<?> child : parent.unfilteredChildren()) {
            if (child instanceof ShellHttpTreeItem<?> item) {
                if (item.data() != null && item.data() == data) {
                    return item;
                }
                ShellHttpTreeItem<?> found = this.findItem(item, data);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
