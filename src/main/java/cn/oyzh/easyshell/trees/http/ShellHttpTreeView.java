package cn.oyzh.easyshell.trees.http;

import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.easyshell.trees.http.collection.ShellHttpCollectionTreeItem;
import cn.oyzh.easyshell.trees.http.collection.ShellHttpCollectionsTreeItem;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentsTreeItem;
import cn.oyzh.easyshell.trees.http.root.ShellHttpRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.util.Callback;

/**
 * HTTP 工作区树。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpTreeView extends RichTreeView {

    /**
     * 各连接的树展开状态
     */
    private static final Map<String, ExpansionState> EXPANSION_STATES = new HashMap<>();

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
        this.dragContent = "shell_http_tree_drag";
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
        this.restoreExpansionState(this.currentExpansionState());
    }

    /**
     * 重新加载节点并尽量保持选中节点
     */
    public void reload() {
        Object selected = this.selectedData();
        ExpansionState state = this.captureExpansionState();
        this.saveExpansionState(state);
        this.loadItems();
        this.restoreExpansionState(state);
        if (this.getItemFilter().getKw() != null && !this.getItemFilter().getKw().isBlank()) {
            this.root().doFilter();
        }
        if (selected != null) {
            this.selectData(selected);
        }
    }

    /**
     * 保存当前展开状态
     */
    public void saveExpansionState() {
        this.saveExpansionState(this.captureExpansionState());
    }

    /**
     * 保存展开状态
     *
     * @param state 展开状态
     */
    private void saveExpansionState(ExpansionState state) {
        String iid = this.workspace == null ? null : this.workspace.getIid();
        if (iid != null) {
            EXPANSION_STATES.put(iid, state);
        }
    }

    /**
     * 获取当前连接保存的展开状态
     *
     * @return 展开状态
     */
    private ExpansionState currentExpansionState() {
        String iid = this.workspace == null ? null : this.workspace.getIid();
        return iid == null ? null : EXPANSION_STATES.get(iid);
    }

    /**
     * 记录当前展开状态
     *
     * @return 展开状态
     */
    private ExpansionState captureExpansionState() {
        ShellHttpCollectionsTreeItem collections = this.collectionsItem();
        ShellHttpEnvironmentsTreeItem environments = this.environmentsItem();
        Set<String> collectionIds = new HashSet<>();
        if (collections != null) {
            for (TreeItem<?> child : collections.unfilteredChildren()) {
                if (child instanceof ShellHttpCollectionTreeItem item && item.isExpanded()) {
                    collectionIds.add(item.collection().getId());
                }
            }
        }
        return new ExpansionState(
                collections != null && collections.isExpanded(),
                environments != null && environments.isExpanded(),
                collectionIds);
    }

    /**
     * 恢复展开状态
     *
     * @param state 展开状态
     */
    private void restoreExpansionState(ExpansionState state) {
        if (state == null) {
            return;
        }
        ShellHttpCollectionsTreeItem collections = this.collectionsItem();
        if (collections != null) {
            collections.setExpanded(state.collectionsExpanded());
            for (TreeItem<?> child : collections.unfilteredChildren()) {
                if (child instanceof ShellHttpCollectionTreeItem item
                        && state.collectionIds().contains(item.collection().getId())) {
                    item.setExpanded(true);
                }
            }
        }
        ShellHttpEnvironmentsTreeItem environments = this.environmentsItem();
        if (environments != null) {
            environments.setExpanded(state.environmentsExpanded());
        }
    }

    /**
     * 树展开状态
     */
    private record ExpansionState(boolean collectionsExpanded, boolean environmentsExpanded,
                                  Set<String> collectionIds) {
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
