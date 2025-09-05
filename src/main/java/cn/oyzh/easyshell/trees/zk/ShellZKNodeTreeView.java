package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.util.zk.ShellZKACLUtil;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.easyshell.zk.ShellZKNode;
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
 * zk节点树
 *
 * @author oyzh
 * @since 2024/11/29
 */
public class ShellZKNodeTreeView extends RichTreeView implements NodeLifeCycle {

    private ShellZKClient client;

    public void client(ShellZKClient client) {
        this.client = client;
    }

    public ShellZKClient client() {
        return this.client;
    }

    public ShellConnect connect() {
        return this.client.getShellConnect();
    }

    @Override
    protected void initTreeView() {
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    public ShellZKNodeTreeItemFilter getItemFilter() {
        try {
            // 初始化过滤器
            if (this.itemFilter == null) {
                ShellZKNodeTreeItemFilter filter = new ShellZKNodeTreeItemFilter();
                // filter.initFilters(this.client.iid());
                this.itemFilter = filter;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (ShellZKNodeTreeItemFilter) this.itemFilter;
    }

    @Override
    public ShellZKNodeTreeItem root() {
        return (ShellZKNodeTreeItem) super.root();
    }

//    @Override
//    public ShellZKNodeTreeItem getRoot() {
//        return (ShellZKNodeTreeItem) super.getRoot();
//    }

    /**
     * 寻找zk节点
     *
     * @param targetPath 目标路径
     * @return zk节点
     */
    public ShellZKNodeTreeItem findNodeItem(String targetPath) {
        return this.findNodeItem(this.root(), targetPath);
    }

    /**
     * 寻找zk节点
     *
     * @param root       根节点
     * @param targetPath 目标路径
     * @return zk节点
     */
    public ShellZKNodeTreeItem findNodeItem(ShellZKNodeTreeItem root, String targetPath) {
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

    /**
     * 节点添加
     *
     * @param nodePath 节点路径
     */
    public void onNodeAdded(String nodePath) {
        try {
            String pPath = ShellZKNodeUtil.getParentPath(nodePath);
            // 寻找节点
            ShellZKNodeTreeItem parent = this.findNodeItem(pPath);
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

    ///**
    // * 节点已添加
    // *
    // * @param nodePath 节点路径
    // */
    //public void onNodeCreated(String nodePath) {
    //    if (this.client.isLastCreate(nodePath)) {
    //        this.client.clearLastCreate();
    //        return;
    //    }
    //    try {
    //        String pPath = ShellZKNodeUtil.getParentPath(nodePath);
    //        // 寻找节点
    //        ShellZKNodeTreeItem parent = this.findNodeItem(pPath);
    //        // 父节点不存在
    //        if (parent == null) {
    //            JulLog.warn("{}: 未找到节点的父节点，无法处理节点！", nodePath);
    //            return;
    //        }
    //        // 获取节点
    //        ShellZKNodeTreeItem item = parent.getNodeItem(nodePath);
    //        // 刷新节点
    //        if (item != null) {
    //            item.refreshNode();
    //            JulLog.info("节点已存在, 更新节点.");
    //        } else if (parent.isLoaded()) {// 更新父节点状态，并标记状态
    //            parent.refreshStat();
    //            parent.setBeChildChanged();
    //            JulLog.info("节点不存在, 父节点已加载, 标记父节点状态.");
    //        } else {// 更新父节点状态
    //            parent.refreshStat();
    //            JulLog.info("节点不存在, 父节点未加载, 更新父节点状态.");
    //        }
    //    } catch (Exception ex) {
    //        JulLog.warn("节点已新增事件处理失败！", ex);
    //    }
    //}
    //
    ///**
    // * 节点已删除
    // *
    // * @param nodePath 节点路径
    // */
    //public void onNodeRemoved(String nodePath) {
    //    if (this.client.isLastDelete(nodePath)) {
    //        this.client.clearLastDelete();
    //        return;
    //    }
    //    try {
    //        String pPath = ShellZKNodeUtil.getParentPath(nodePath);
    //        // 寻找节点
    //        ShellZKNodeTreeItem parent = this.findNodeItem(pPath);
    //        // 父节点不存在
    //        if (parent == null) {
    //            JulLog.warn("{}: 未找到节点的父节点，无法处理节点！", nodePath);
    //            return;
    //        }
    //        // 寻找节点
    //        ShellZKNodeTreeItem item = parent.getNodeItem(nodePath);
    //        // 更新信息
    //        if (item != null) {
    //            item.setBeDeleted();
    //            JulLog.info("节点存在, 标记节点状态为删除.");
    //        } else {// 更新父节点状态
    //            parent.refreshStat();
    //            JulLog.info("节点不存在, 父节点未加载, 更新父节点状态.");
    //        }
    //    } catch (Exception ex) {
    //        JulLog.warn("节点已删除事件处理失败！", ex);
    //    }
    //}
    //
    ///**
    // * 节点已变更
    // *
    // * @param nodePath 节点路径
    // */
    //public void onNodeChanged(String nodePath) {
    //    if (this.client.isLastUpdate(nodePath)) {
    //        this.client.clearLastUpdate();
    //        return;
    //    }
    //    try {
    //        // 寻找节点
    //        ShellZKNodeTreeItem item = this.findNodeItem(nodePath);
    //        // 更新信息
    //        if (item != null) {
    //            item.setBeChanged();
    //        } else {
    //            JulLog.warn("{}: 未找到被修改节点，无法处理节点！", nodePath);
    //        }
    //    } catch (Exception ex) {
    //        JulLog.warn("节点已修改事件处理失败！", ex);
    //    }
    //}

    /**
     * 获取全部zk子节点列表
     *
     * @return List<ShellZKNodeTreeItem>
     */
    private List<ShellZKNodeTreeItem> getAllNodeItem() {
        List<ShellZKNodeTreeItem> list = new ArrayList<>(128);
        this.getAllNodeItem(this.root(), list);
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
     * 加载根节点
     */
    public void loadRoot() throws Exception {
        ShellZKNodeTreeItem rootItem = this.root();
        // 初始化根节点
        if (this.getRoot() == null) {
            // 获取根节点
            ShellZKNode rootNode = ShellZKNodeUtil.getNode(this.client, "/");
            // 生成根节点
            rootItem = new ShellZKNodeTreeItem(rootNode, this);
            // 设置根节点
            ShellZKNodeTreeItem finalRootItem = rootItem;
            FXUtil.runWait(() -> this.setRoot(finalRootItem));
        }
        // 加载根节点
        rootItem.loadRoot();
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

//    /**
//     * 搜索参数
//     */
//    private ZKSearchParam searchParam;
//
//    /**
//     * 当前节点
//     */
//    private ShellZKNodeTreeItem currentNode;
//
//    /**
//     * 搜索触发事件
//     *
//     * @param param 参数
//     * @return 是否找到节点
//     */
//    public boolean onSearchTrigger(ZKSearchParam param) {
//        // 判断当前参数是否变化
//        if (this.searchParam == null || !this.searchParam.equals(param)) {
//            this.currentNode = null;
//        }
//        this.searchParam = param;
//        // 节点列表
//        List<ShellZKNodeTreeItem> list = this.getAllNodeItem();
//        // 判断是往前还是往后搜索
//        if (!param.isNext()) {
//            list = list.reversed();
//        }
//        // 判断状态
//        if (list.isEmpty() || !list.contains(this.currentNode)) {
//            this.currentNode = null;
//        }
//        String kw = param.getKeyword();
//        ShellZKNodeTreeItem foundNode = null;
//        // 搜索开始标志位
//        boolean findStart = this.currentNode == null;
//        // 遍历节点
//        for (ShellZKNodeTreeItem node : list) {
//            // 寻找到节点，则可以开始搜索了
//            if (this.currentNode != null && node == this.currentNode) {
//                findStart = true;
//                continue;
//            }
//            if (!findStart) {
//                continue;
//            }
//            // 搜索路径
//            if (param.isSearchPath()) {
//                String nodePath = node.nodePath();
//                int index = TextUtil.findIndex(nodePath, kw, null, param.isMatchCase(), param.isMatchFull());
//                if (index != -1) {
//                    foundNode = node;
//                    break;
//                }
//            }
//            // 搜索值
//            if (param.isSearchData()) {
//                byte[] nodeData = node.getData();
//                if (nodeData != null) {
//                    int index = TextUtil.findIndex(new String(nodeData), kw, null, param.isMatchCase(), param.isMatchFull());
//                    if (index != -1) {
//                        foundNode = node;
//                        break;
//                    }
//                }
//            }
//        }
//        // 找到节点就更新当前节点，方便下次搜索
//        if (foundNode != null) {
//            this.currentNode = foundNode;
//            this.selectAndScroll(foundNode);
//        } else {
//            this.currentNode = null;
//        }
//        // 搜索完成事件
//        ShellZKEventUtil.searchComplete(this.connect());
//        return foundNode != null;
//    }

//    /**
//     * 搜索结束事件
//     */
//    public void onSearchFinish() {
//        this.currentNode = null;
//        this.searchParam = null;
//    }
}
