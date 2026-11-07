package cn.oyzh.easyshell.trees.zk.node;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeItem;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeView;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.easyshell.zk.ShellZKNode;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.thread.BackgroundService;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * zk节点分类节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKNodesTreeItem extends ShellZKTreeItem<ShellZKNodesTreeItemValue> {

    /**
     * 当前节点路径
     */
    private String currentPath = "/";

    /**
     * 构造节点分类节点
     *
     * @param treeView 树视图
     */
    public ShellZKNodesTreeItem(ShellZKTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        super.setSortable(false);
        this.setValue(new ShellZKNodesTreeItemValue(this));
        // 展开时加载根节点
        this.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.isLoaded() && !this.isLoading()) {
                BackgroundService.submit(() -> this.loadNodeRoot(this.currentPath));
            }
        });
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(1);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    /**
     * 加载指定路径的节点作为节点树根
     *
     * @param path 节点路径
     */
    public void loadNodeRoot(String path) {
        if (this.isLoading()) {
            return;
        }
        try {
            this.setLoading(true);
            ShellZKNode node = cn.oyzh.easyshell.util.zk.ShellZKNodeUtil.getNode(this.client(), path);
            ShellZKNodeTreeItem item = new ShellZKNodeTreeItem(node, this.getTreeView());
            this.currentPath = path;
            this.setChild(item);
            this.setLoaded(true);
            // 加载节点子节点
            item.loadRoot();
        } catch (Exception ex) {
            this.setLoaded(false);
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.setLoading(false);
            this.expend();
            this.doFilter();
            this.doSort();
        }
    }

    /**
     * 获取当前节点树根
     *
     * @return 节点树根
     */
    public ShellZKNodeTreeItem getNodeItem() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellZKNodeTreeItem item) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadNodeRoot(this.currentPath);
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            BackgroundService.submit(() -> this.loadNodeRoot(this.currentPath));
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    /**
     * 获取zk客户端
     *
     * @return zk客户端
     */
    public ShellZKClient client() {
        return this.getTreeView().client();
    }

    /**
     * 获取zk连接信息
     *
     * @return zk连接信息
     */
    public ShellConnect connect() {
        return this.getTreeView().connect();
    }

    /**
     * 获取节点列表
     *
     * @return 节点列表
     */
    public List<ShellZKNodeTreeItem> nodeItems() {
        List<ShellZKNodeTreeItem> items = new ArrayList<>();
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellZKNodeTreeItem item) {
                items.add(item);
            }
        }
        return items;
    }
}
