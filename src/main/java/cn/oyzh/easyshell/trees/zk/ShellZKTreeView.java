package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.tabs.zk.ShellZKTabPane;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodesTreeItem;
import cn.oyzh.easyshell.trees.zk.query.ShellZKQueriesTreeItem;
import cn.oyzh.easyshell.trees.zk.query.ShellZKQueryTreeItem;
import cn.oyzh.easyshell.trees.zk.root.ShellZKRootTreeItem;
import cn.oyzh.easyshell.util.zk.ShellZKACLUtil;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.node.NodeLifeCycle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * zk节点树视图
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKTreeView extends RichTreeView implements NodeLifeCycle {

    /**
     * zk客户端
     */
    private ShellZKClient client;

    /**
     * 标签页面板
     */
    private ShellZKTabPane tabPane;

    /**
     * 设置zk客户端
     *
     * @param client zk客户端
     */
    public void client(ShellZKClient client) {
        this.client = client;
        FXUtil.runWait(() -> {
            if (!(super.root() instanceof ShellZKRootTreeItem)) {
                this.setRoot(new ShellZKRootTreeItem(this));
            }
        });
    }

    /**
     * 获取zk客户端
     *
     * @return zk客户端
     */
    public ShellZKClient client() {
        return this.client;
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect connect() {
        return this.client.getShellConnect();
    }

    /**
     * 获取标签页面板
     *
     * @return 标签页面板
     */
    public ShellZKTabPane getTabPane() {
        return tabPane;
    }

    /**
     * 设置标签页面板
     *
     * @param tabPane 标签页面板
     */
    public void setTabPane(ShellZKTabPane tabPane) {
        this.tabPane = tabPane;
    }

    @Override
    protected void initTreeView() {
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    public ShellZKTreeItemFilter getItemFilter() {
        try {
            // 初始化过滤器
            if (this.itemFilter == null) {
                this.itemFilter = new ShellZKTreeItemFilter();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (ShellZKTreeItemFilter) this.itemFilter;
    }

    @Override
    public ShellZKRootTreeItem root() {
        return (ShellZKRootTreeItem) super.root();
    }

    /**
     * 获取节点分类节点
     *
     * @return 节点分类节点
     */
    public ShellZKNodesTreeItem nodesItem() {
        return this.root() == null ? null : this.root().getNodesChild();
    }

    /**
     * 获取查询分类节点
     *
     * @return 查询分类节点
     */
    public ShellZKQueriesTreeItem queriesItem() {
        return this.root() == null ? null : this.root().getQueriesChild();
    }

    /**
     * 获取当前节点树根
     *
     * @return 当前节点树根
     */
    public ShellZKNodeTreeItem currentNodeItem() {
        ShellZKNodesTreeItem nodesItem = this.nodesItem();
        return nodesItem == null ? null : nodesItem.getNodeItem();
    }

    /**
     * 加载节点树
     */
    public void loadItems() {
        this.disable();
        try {
            this.root().loadChild();
        } finally {
            this.enable();
        }
    }

    /**
     * 寻找zk节点
     *
     * @param targetPath 目标路径
     * @return zk节点
     */
    public ShellZKNodeTreeItem findNodeItem(String targetPath) {
        return this.findNodeItem(this.currentNodeItem(), targetPath);
    }

    /**
     * 寻找zk节点
     *
     * @param root       根节点
     * @param targetPath 目标路径
     * @return zk节点
     */
    public ShellZKNodeTreeItem findNodeItem(ShellZKNodeTreeItem root, String targetPath) {
        if (root == null || targetPath == null) {
            return null;
        }
        // 节点对应，返回数据
        if (targetPath.equals(root.nodePath())) {
            return root;
        }
        // 互相不包含，返回null
        if (!targetPath.contains(root.nodePath()) && !root.nodePath().startsWith(targetPath)) {
            return null;
        }
        // 子节点为空，返回null
        if (root.isChildEmpty()) {
            return null;
        }
        // 遍历子节点，寻找匹配节点
        for (ShellZKNodeTreeItem item : root.itemChildren()) {
            ShellZKNodeTreeItem treeItem = this.findNodeItem(item, targetPath);
            // 返回节点信息
            if (treeItem != null) {
                return treeItem;
            }
        }
        return null;
    }

    @Override
    public void expand() {
        if (this.getSelectedItem() instanceof ShellZKNodeTreeItem item) {
            item.expandAll();
            this.select(item);
        }
    }

    @Override
    public void collapse() {
        if (this.getSelectedItem() instanceof ShellZKNodeTreeItem item) {
            item.collapseAll();
            this.select(item);
        }
    }

//    /**
//     * 内容视图
//     */
//    private final boolean contentListViewport = ShellSettingStore.SETTING.isZkContentListViewport();

    /**
     * 节点添加
     *
     * @param nodePath 节点路径
     */
    public void nodeAdded(String nodePath) {
        try {
            String pPath = ShellZKNodeUtil.getParentPath(nodePath);
            // 寻找节点
            ShellZKNodeTreeItem parent = this.findNodeItem(pPath);
//            if (this.contentListViewport) {
//                if (parent == null || parent != this.root()) {
//                    this.loadRoot(pPath);
//                    parent = this.findNodeItem(pPath);
//                }
//            }
            // 父节点不存在
            if (parent == null) {
                JulLog.warn("{}: 未找到节点的父节点，无法处理节点！", nodePath);
                return;
            }
            // 获取节点
            ShellZKNodeTreeItem item = parent.getNodeItem(pPath);
            // 刷新节点
            if (item != null) {
                item.refreshNode();
                JulLog.info("节点已存在, 更新节点.");
            } else if (parent.isLoaded()) {// 添加节点
                parent.refreshStat();
                parent.addChild(nodePath);
                JulLog.info("节点不存在, 添加节点.");
            } else {// 加载子节点
                parent.refreshStat();
                parent.loadChild(false);
                JulLog.info("父节点未加载, 加载父节点.");
            }
            // 过滤节点
            parent.doFilter(this.getItemFilter());
            // 排序节点
            parent.doSort();
            // 选中此节点
            if (item == null) {
                item = parent.getNodeItem(nodePath);
            }
            if (item != null) {
                ShellZKNodeTreeItem finalItem = item;
                FXUtil.runPulse(() -> this.selectAndScroll(finalItem));
            }
        } catch (Exception ex) {
            JulLog.warn("新增节点事件处理失败！", ex);
        }
    }

    /**
     * 获取全部zk子节点列表
     *
     * @return List<ShellZKNodeTreeItem>
     */
    private List<ShellZKNodeTreeItem> getAllNodeItem() {
        List<ShellZKNodeTreeItem> list = new ArrayList<>(128);
        this.getAllNodeItem(this.currentNodeItem(), list);
        return list;
    }

    /**
     * 获取全部zk子节点列表
     *
     * @param item zk节点
     * @param list zk子节点列表
     */
    private void getAllNodeItem(ShellZKNodeTreeItem item, List<ShellZKNodeTreeItem> list) {
        if (item != null) {
            list.add(item);
            for (TreeItem<?> treeItem : item.unfilteredChildren()) {
                if (treeItem instanceof ShellZKNodeTreeItem nodeTreeItem) {
                    this.getAllNodeItem(nodeTreeItem, list);
                }
            }
        }
    }

    /**
     * 认证变更事件
     *
     * @param auth 认证信息
     * @throws Exception 异常
     */
    public void authChanged(ShellZKAuth auth) throws Exception {
        this.client.addAuth(auth.getUser(), auth.getPassword());
        for (ShellZKNodeTreeItem item : this.getAllNodeItem()) {
            if (item.isNeedAuth() || ShellZKACLUtil.existDigest(item.acl(), auth.getUser())) {
                item.authChanged();
            }
        }
    }

    /**
     * 是否有未保存数据的节点
     *
     * @return 结果
     */
    public boolean hasUnsavedData() {
        List<ShellZKNodeTreeItem> items = this.getAllNodeItem();
        for (ShellZKNodeTreeItem item : items) {
            if (item.isDataUnsaved()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打开节点标签页
     *
     * @param item 节点
     */
    public void openNode(ShellZKNodeTreeItem item) {
        if (this.tabPane != null) {
            this.tabPane.openNode(item);
        }
    }

    /**
     * 关闭节点标签页
     *
     * @param nodePath 节点路径
     */
    public void closeNodeTab(String nodePath) {
        if (this.tabPane != null) {
            this.tabPane.closeNodeTab(nodePath);
        }
    }

    /**
     * 打开查询标签页
     *
     * @param item 查询节点
     */
    public void openQuery(ShellZKQueryTreeItem item) {
        if (this.tabPane != null) {
            this.tabPane.openQuery(item);
        }
    }

    /**
     * 新增查询标签页
     *
     * @param queriesItem 查询分类节点
     */
    public void openNewQuery(ShellZKQueriesTreeItem queriesItem) {
        if (this.tabPane != null) {
            this.tabPane.openNewQuery(queriesItem);
        }
    }

    /**
     * 重命名查询标签页
     *
     * @param queryId 查询id
     * @param name    查询名称
     */
    public void renameQueryTab(String queryId, String name) {
        if (this.tabPane != null) {
            this.tabPane.renameQueryTab(queryId, name);
        }
    }

    /**
     * 关闭查询标签页
     *
     * @param queryId 查询id
     */
    public void closeQueryTab(String queryId) {
        if (this.tabPane != null) {
            this.tabPane.closeQueryTab(queryId);
        }
    }

    /**
     * 打开认证标签页
     */
    public void openAuth() {
        if (this.tabPane != null) {
            this.tabPane.openAuth();
        }
    }

    /**
     * 打开服务标签页
     */
    public void openServer() {
        if (this.tabPane != null) {
            this.tabPane.openServer();
        }
    }

    /**
     * 打开终端标签页
     */
    public void openTerminal() {
        if (this.tabPane != null) {
            this.tabPane.openTerminal();
        }
    }
}
