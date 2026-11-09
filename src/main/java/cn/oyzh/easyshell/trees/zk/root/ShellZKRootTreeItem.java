package cn.oyzh.easyshell.trees.zk.root;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeItem;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeView;
import cn.oyzh.easyshell.trees.zk.auth.ShellZKAuthTreeItem;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodesTreeItem;
import cn.oyzh.easyshell.trees.zk.query.ShellZKQueriesTreeItem;
import cn.oyzh.easyshell.trees.zk.server.ShellZKServerTreeItem;
import cn.oyzh.easyshell.trees.zk.terminal.ShellZKTerminalTreeItem;
import cn.oyzh.easyshell.util.zk.ShellZKViewFactory;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * zk树根节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKRootTreeItem extends ShellZKTreeItem<ShellZKRootTreeItemValue> {

    /**
     * 构造zk树根节点
     *
     * @param treeView 树视图
     */
    public ShellZKRootTreeItem(ShellZKTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        super.setSortable(false);
        this.setValue(new ShellZKRootTreeItemValue(this));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(4);
        FXMenuItem reload = MenuItemHelper.reloadDatabase(this::reloadChild);
        FXMenuItem importData = MenuItemHelper.importData(this::importData);
        FXMenuItem exportData = MenuItemHelper.exportData(this::exportData);
        FXMenuItem transportData = MenuItemHelper.transportData(this::transportData);
        items.add(reload);
        items.add(importData);
        items.add(exportData);
        items.add(transportData);
        return items;
    }

    /**
     * 导入数据
     */
    public void importData() {
        ShellZKViewFactory.importData(this.connect());
    }

    /**
     * 导出数据
     */
    public void exportData() {
        ShellZKViewFactory.exportData(this.connect(), null);
    }

    /**
     * 传输数据
     */
    public void transportData() {
        ShellZKViewFactory.transportData(this.connect());
    }

    @Override
    public void loadChild() {
        List<TreeItem<?>> items = new ArrayList<>();
        items.add(new ShellZKNodesTreeItem(this.getTreeView()));
        items.add(new ShellZKQueriesTreeItem(this.getTreeView()));
        items.add(new ShellZKAuthTreeItem(this.getTreeView()));
        items.add(new ShellZKTerminalTreeItem(this.getTreeView()));
        items.add(new ShellZKServerTreeItem(this.getTreeView()));
        this.setChild(items);
        this.setLoaded(true);
        this.expend();
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    /**
     * 获取节点分类节点
     *
     * @return 节点分类节点
     */
    public ShellZKNodesTreeItem getNodesChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellZKNodesTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取查询分类节点
     *
     * @return 查询分类节点
     */
    public ShellZKQueriesTreeItem getQueriesChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellZKQueriesTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取认证节点
     *
     * @return 认证节点
     */
    public ShellZKAuthTreeItem getAuthChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellZKAuthTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取服务节点
     *
     * @return 服务节点
     */
    public ShellZKServerTreeItem getServerChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellZKServerTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取终端节点
     *
     * @return 终端节点
     */
    public ShellZKTerminalTreeItem getTerminalChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellZKTerminalTreeItem item) {
                return item;
            }
        }
        return null;
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

    @Override
    public void onPrimaryDoubleClick() {
        if (this.isLoaded()) {
            super.onPrimaryDoubleClick();
        } else {
            this.loadChild();
        }
    }
}
