package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.common.dto.FriendlyInfo;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.CostUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.dto.zk.ShellZKACL;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.zk.ShellZKCollectStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.util.zk.ShellZKDataUtil;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.easyshell.zk.ShellZKNode;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.StatsTrack;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellZKNodeTreeItem extends RichTreeItem<ShellZKNodeTreeItemValue> {

    /**
     * zk节点
     */
    protected ShellZKNode value;

    public ShellZKNode value() {
        return value;
    }

    public void value(ShellZKNode value) {
        this.value = value;
    }

    ///**
    // * 设置节点变更
    // */
    // public void setBeChanged() {
    //    this.bitValue().set(8, true);
    //    this.refresh();
    //}

    ///**
    // * 是否变更
    // *
    // * @return 结果
    // */
    // public boolean isBeChanged() {
    //    return this.bitValue != null && this.bitValue().get(8);
    //}
    //
    ///**
    // * 设置节点删除
    // */
    // public void setBeDeleted() {
    //    this.bitValue().set(9, true);
    //    this.refresh();
    //}

    ///**
    // * 是否删除
    // *
    // * @return 结果
    // */
    // public boolean isBeDeleted() {
    //    return this.bitValue != null && this.bitValue().get(9);
    //}
    //
    ///**
    // * 设置节点变化
    // */
    // public void setBeChildChanged() {
    //    this.bitValue().set(10, true);
    //    this.refresh();
    //}
    //
    ///**
    // * 是否子节点变化
    // *
    // * @return 结果
    // */
    // public boolean isBeChildChanged() {
    //    return this.bitValue != null && this.bitValue().get(10);
    //}

    ///**
    // * 清除子节点变化
    // */
    // public void clearBeChildChanged() {
    //    if (this.bitValue != null) {
    //        this.bitValue.set(10, false);
    //    }
    //}
    //
    ///**
    // * 设置忽略变化
    // */
    // public void doIgnoreChanged() {
    //    this.bitValue().set(11, true);
    //}
    //
    ///**
    // * 是否忽略变化
    // *
    // * @return 结果
    // */
    // public boolean isIgnoreChanged() {
    //    return this.bitValue != null && this.bitValue().get(11);
    //}
    //
    ///**
    // * 设置忽略删除
    // */
    // public void doIgnoreDeleted() {
    //    this.bitValue().set(12, true);
    //}
    //
    ///**
    // * 是否忽略删除
    // *
    // * @return 结果
    // */
    // public boolean isIgnoreDeleted() {
    //    return this.bitValue != null && this.bitValue().get(12);
    //}
    //
    ///**
    // * 设置忽略子节点变化
    // */
    // public void doIgnoreChildChanged() {
    //    this.bitValue().set(13, true);
    //}
    //
    ///**
    // * 是否忽略子节点变化
    // *
    // * @return 结果
    // */
    // public boolean isIgnoreChildChanged() {
    //    return this.bitValue != null && this.bitValue().get(13);
    //}

    /**
     * 设置是否需要认证
     *
     * @param needAuth 是否需要认证
     */
    public void setNeedAuth(boolean needAuth) {
        this.bitValue().set(14, needAuth);
    }

    /**
     * 是否需要认证
     *
     * @return 结果
     */
    public boolean isNeedAuth() {
        boolean needAuth = this.bitValue != null && this.bitValue().get(14);
        return needAuth || this.client().isNeedAuth(this.value);
    }

    /**
     * 设置已取消状态
     *
     * @param canceled 已取消状态
     */
    public void setCanceled(boolean canceled) {
        this.bitValue().set(15, canceled);
    }

    /**
     * 是否已取消
     *
     * @return 结果
     */
    public boolean isCanceled() {
        return this.bitValue != null && this.bitValue().get(15);
    }

    /**
     * 获取节点数据
     *
     * @return 数据
     */
    public byte[] getNodeData() {
        return this.value.getNodeData();
    }

    /**
     * 设置数据
     *
     * @param data 数据
     */
    public void nodeData(byte[] data) {
        this.value.setUnsavedData(data);
        this.refresh();
    }

    /**
     * 获取数据
     *
     * @return 数据
     */
    public byte[] getData() {
        byte[] bytes;
        if (this.isDataUnsaved()) {
            bytes = this.getUnsavedData();
        } else {
            bytes = this.getNodeData();
        }
        if (bytes == null) {
            bytes = new byte[0];
        }
        return bytes;
    }

    /**
     * 清理
     */
    public void clear() {
        this.value.clearUnsavedData();
    }

    /**
     * 数据是否未保存
     *
     * @return 结果
     */
    public boolean isDataUnsaved() {
        return this.value.hasUnsavedData();
    }

    public ShellZKNodeTreeItem(ShellZKNode value, ShellZKNodeTreeView treeView) {
        super(treeView);
        this.value = value;
        this.setFilterable(true);
        this.setValue(new ShellZKNodeTreeItemValue(this));
    }

    /**
     * 获取节点路径
     *
     * @return 节点路径
     */
    public String nodePath() {
        return this.value.nodePath();
    }

    @Override
    public void loadChild() {
        if (!this.isLoading()) {
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        CostUtil.record();
                        this.loadChild(false);
                        CostUtil.printCost();
                    })
                    .onFinish(this::refresh)
                    .onSuccess(this::expend)
                    .onError(MessageBox::exception)
                    .build();
            this.startWaiting(task);
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(12);
        if (this.isLoading()) {
            FXMenuItem cancel = MenuItemHelper.cancelOperation("11", this::cancel);
            items.add(cancel);
        } else {
            // 持久节点
            if (this.isPersistentNode()) {
                FXMenuItem add = MenuItemHelper.addNode("12", this::addNode);
                items.add(add);
            }
            // 非根节点 + 子节点 + 持久节点
            if (!this.isRootNode() && this.isChildrenNode() && this.isPersistentNode()) {
                FXMenuItem rename = MenuItemHelper.renameNode("12", this::rename);
                FXMenuItem cloneNode = MenuItemHelper.cloneNode("12", this::cloneNode);
                items.add(rename);
                items.add(cloneNode);
            }
            // 非根节点
            if (!this.isRootNode()) {
                FXMenuItem delete = MenuItemHelper.deleteNode("12", this::delete);
                items.add(delete);
            }
            if (!items.isEmpty()) {
                items.add(MenuItemHelper.separator());
            }
            // 重载
            FXMenuItem reload = MenuItemHelper.refreshData("12", this::reloadChild);
            items.add(reload);
            // 持久节点 + 有读取权限
            if (this.isPersistentNode() && this.hasReadPerm()) {
                FXMenuItem export = MenuItemHelper.exportData("12", this::exportData);
                items.add(export);
            }
            items.add(MenuItemHelper.separator());
            // 数据历史
            FXMenuItem dataHistory = MenuItemHelper.dataHistory("12", this::dataHistory);
            items.add(dataHistory);
            // 复制节点路径
            FXMenuItem copyNodePath = MenuItemHelper.copyNodePath("12", this::copyNodePath);
            items.add(copyNodePath);
            // 认证
            FXMenuItem auth = MenuItemHelper.authNode("12", this::authNode);
            items.add(auth);
            FXMenuItem sortAsc = MenuItemHelper.sortAsc("12", this::sortAsc);
            items.add(sortAsc);
            FXMenuItem sortDesc = MenuItemHelper.sortDesc("12", this::sortDesc);
            items.add(sortDesc);
            items.add(MenuItemHelper.separator());
            // 父节点
            if (this.isParentNode()) {
                FXMenuItem unload = MenuItemHelper.unload("12", this::unloadChild);
                FXMenuItem loadAll = MenuItemHelper.loadAll("12", this::loadChildAll);
                FXMenuItem expandAll = MenuItemHelper.expandAll("12", this::expandAll);
                FXMenuItem collapseAll = MenuItemHelper.collapseAll("12", this::collapseAll);
                items.add(unload);
                items.add(loadAll);
                items.add(expandAll);
                items.add(collapseAll);
            }
        }
        return items;
    }

    /**
     * 数据历史
     */
    private void dataHistory() {
        String nodePath = this.value.nodePath();
        ShellViewFactory.zkHistoryData(this.client(), nodePath);
    }

    /**
     * 复制节点路径
     */
    private void copyNodePath() {
        ClipboardUtil.copy(this.decodeNodePath());
        MessageBox.okToast(I18nHelper.operationSuccess());
    }

    /**
     * 克隆节点
     */
    private void cloneNode() {
        // 判断是否符合要求
        if (this.isRootNode() || this.isParentNode() || this.isEphemeralNode()) {
            return;
        }
        String nodeName = this.nodeName() + "-" + I18nHelper.clone1();
        // 检查是否存在
        String parentPath = ShellZKNodeUtil.getParentPath(this.nodePath());
        String newNodePath = ShellZKNodeUtil.concatPath(parentPath, nodeName);
        try {
            if (this.client().exists(newNodePath)) {
                MessageBox.warn(I18nHelper.node() + " [" + newNodePath + "] " + I18nHelper.alreadyExists());
                return;
            }
            // 创建模式
            CreateMode createMode = this.isEphemeralNode() ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT;
            // 创建新节点
            if (this.client().create(newNodePath, this.getNodeData(), List.copyOf(this.acl()), null, createMode, true) != null) {
                this.getTreeView().nodeAdded(newNodePath);
            } else {// 操作失败
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 节点名称
     *
     * @return 节点名称
     */
    public String nodeName() {
        return this.value.nodeName();
    }

    /**
     * 取消操作
     */
    public void cancel() {
        this.setCanceled(true);
    }

    /**
     * 添加zk子节点
     */
    public void addNode() {
        StageAdapter adapter = ShellViewFactory.zkAddNode(this, this.client());
        if (adapter == null) {
            return;
        }
        String addedNodePath = adapter.getProp("addedNodePath");
        if (addedNodePath != null) {
            this.getTreeView().nodeAdded(addedNodePath);
        }
    }

    /**
     * 认证zk节点
     */
    public void authNode() {
        try {
            StageAdapter adapter = ShellViewFactory.zkAuthNode(this, this.client());
            if (adapter == null) {
                return;
            }
            ShellZKAuth auth = adapter.getProp("auth");
            if (auth == null) {
                return;
            }
            boolean success = adapter.getProp("success");
            if (success) {
                this.getTreeView().authChanged(auth);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 导出zk节点
     */
    private void exportData() {
        ShellViewFactory.zkExportData(this.zkConnect(), this.nodePath());
    }

    @Override
    public void rename() {
        // 判断是否符合要求
        if (this.isRootNode() || this.isParentNode() || this.isEphemeralNode()) {
            return;
        }
        String nodeName = MessageBox.prompt(I18nHelper.pleaseInputNodeName(), this.nodeName());
        // 名称为空或名称跟当前名称相同，则忽略
        if (StringUtil.isBlank(nodeName) || Objects.equals(nodeName, this.nodeName())) {
            return;
        }
        // 检查是否存在
        String parentPath = ShellZKNodeUtil.getParentPath(this.nodePath());
        String newNodePath = ShellZKNodeUtil.concatPath(parentPath, nodeName);
        try {
            if (this.client().exists(newNodePath)) {
                MessageBox.warn(I18nHelper.node() + " [" + newNodePath + "] " + I18nHelper.alreadyExists());
                return;
            }
            // 创建模式
            CreateMode createMode = this.isEphemeralNode() ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT;
            // 创建新节点
            if (this.client().create(newNodePath, this.getNodeData(), List.copyOf(this.acl()), null, createMode, true) != null) {
                // 删除旧节点
                this.deleteNode();
                //// 发送事件
                // ShellZKEventUtil.nodeAdded(this.zkConnect(), newNodePath);
                this.getTreeView().nodeAdded(newNodePath);
            } else {// 操作失败
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public void delete() {
        // 根节点
        if (this.isRootNode()) {
            return;
        }
        // 父节点
        if (!MessageBox.confirm(I18nHelper.delete() + " [" + this.decodeNodePath() + "] ", I18nHelper.areYouSure())) {
            return;
        }
        // 创建任务
        Task task = TaskBuilder.newBuilder()
                .onStart(this::deleteNode)
                .onError(MessageBox::exception)
                .build();
        this.startWaiting(task);
    }

    /**
     * 删除节点
     */
    private void deleteNode() throws Exception {
        try {
            ShellZKNodeTreeItem parent = this.parent();
            // 执行删除
            this.client().delete(this.nodePath(), null, this.isParentNode());
            // 删除历史
            ShellZKDataUtil.deleteHistory(this.nodePath(), this.client());
            // 刷新状态
            if (parent != null) {
                parent.refreshStat();
            }
        } catch (KeeperException.NoNodeException ignore) {

        }
        // 删除树节点
        this.remove();
    }

    /**
     * 当前节点的父zk节点
     *
     * @return 父zk节点
     */
    public ShellZKNodeTreeItem parent() {
        return (ShellZKNodeTreeItem) this.getParent();
    }

    /**
     * 取消加载
     */
    public void unloadChild() {
        this.clearChild();
        this.setLoaded(false);
    }

    /**
     * 加载全部
     */
    private void loadChildAll() {
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> {
                    CostUtil.record();
                    this.loadChild(true, 0);
                    CostUtil.printCost();
                })
                .onFinish(this::expend)
                .onSuccess(this::refresh)
                .onError(MessageBox::exception)
                .build();
        this.startWaiting(task);
    }

    /**
     * 收缩全部
     */
    public void collapseAll() {
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> this.collapseAll(this))
                .onSuccess(() -> this.getTreeView().select(this))
                .onError(ex -> MessageBox.exception(ex, I18nHelper.operationFail()))
                .build();
        this.startWaiting(task);
    }

    /**
     * 展开全部
     */
    public void expandAll() {
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> this.expandAll(this))
                .onSuccess(() -> this.getTreeView().select(this))
                .onError(ex -> MessageBox.exception(ex, I18nHelper.operationFail()))
                .build();
        this.startWaiting(task);
    }

    /**
     * 获取zk节点
     *
     * @param path 路径
     * @return zk节点
     */
    public ShellZKNodeTreeItem getNodeItem(String path) {
        if (StringUtil.isNotBlank(path) && !this.isChildEmpty()) {
            for (TreeItem<?> child : this.unfilteredChildren()) {
                if (child instanceof ShellZKNodeTreeItem treeItem) {
                    if (treeItem.decodeNodePath().equals(path)) {
                        return treeItem;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void remove() {
        // 获取父节点
        ShellZKNodeTreeItem parent = this.parent();
        // 删除节点
        if (parent != null) {
            // 下一个节点
            TreeItem<?> nextItem = null;
            // 如果当前节点被选中
            if (this.isSelected()) {
                // 如果下一个节点不为null，则选中下一个节点，否则选中此节点的父节点
                nextItem = this.nextSibling();
                nextItem = nextItem == null ? parent : nextItem;
            }
            // 取消此节点的收藏
            this.unCollect();
            // 移除此节点
            parent.removeChild(this);
            try {
                // 刷新父节点状态
                parent.refreshStat();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
            // 选中节点
            if (nextItem != null) {
                TreeItem<?> finalNextItem = nextItem;
                FXUtil.runPulse(() -> parent.getTreeView().select(finalNextItem));
            }
        } else {
            JulLog.warn("remove fail, this.parent() is null.");
        }
    }

    /**
     * 添加zk子节点
     *
     * @param path zk节点路径
     */
    public void addChild(String path) throws Exception {
        if (StringUtil.isNotBlank(path)) {
            ShellZKNode node = ShellZKNodeUtil.getNode(this.client(), path);
            this.addChild(node);
        }
    }

    /**
     * 添加zk子节点
     *
     * @param node zk子节点
     */
    public void addChild(ShellZKNode node) {
        if (node != null) {
            this.addChild(new ShellZKNodeTreeItem(node, this.getTreeView()));
        }
    }

    @Override
    public ShellZKNodeTreeView getTreeView() {
        return (ShellZKNodeTreeView) super.getTreeView();
    }

    public ShellZKClient client() {
        return this.getTreeView().client();
    }

    public ShellConnect zkConnect() {
        return this.getTreeView().connect();
    }

    /**
     * 刷新zk节点
     */
    public void refreshNode() throws Exception {
        try {
            ShellZKNodeUtil.refreshNode(this.client(), this.value);
            this.clear();
        } catch (KeeperException.NoAuthException ex) {
            this.setNeedAuth(true);
        }
    }

    /**
     * 刷新zk节点数据
     */
    public void refreshData() throws Exception {
        try {
            JulLog.debug("refreshData.");
            ShellZKNodeUtil.refreshData(this.client(), this.value);
            this.clear();
        } catch (KeeperException.NoAuthException ex) {
            this.setNeedAuth(true);
        }
    }

    /**
     * 刷新zk节点权限
     */
    public void refreshACL() throws Exception {
        try {
            JulLog.debug("refreshACL.");
            ShellZKNodeUtil.refreshAcl(this.client(), this.value);
        } catch (KeeperException.NoAuthException ex) {
            this.setNeedAuth(true);
        }
    }

    /**
     * 刷新zk节点配额
     */
    public void refreshQuota() throws Exception {
        try {
            JulLog.debug("refreshQuota.");
            ShellZKNodeUtil.refreshQuota(this.client(), this.value);
        } catch (KeeperException.NoNodeException ignored) {
            this.value.quota(null);
        } catch (KeeperException.NoAuthException ex) {
            this.setNeedAuth(true);
        }
    }

    /**
     * 刷新zk节点状态
     */
    public void refreshStat() throws Exception {
        try {
            JulLog.debug("refreshStat.");
            ShellZKNodeUtil.refreshStat(this.client(), this.value);
            this.refresh();
        } catch (KeeperException.NoAuthException ex) {
            this.setNeedAuth(true);
        }
    }

    /**
     * 保存节点数据
     *
     * @return 结果
     */
    public boolean saveData() {
        try {
            // 当前数据
            byte[] data = this.getUnsavedData();
            // 更新数据
            Stat stat = this.client().setData(this.nodePath(), data);
            if (stat != null) {
                // 更新数据
                this.value.stat(stat);
                this.value.setNodeData(data);
                this.saveHistory();
                this.clear();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        } finally {
            this.refresh();
        }
        return false;
    }

    @Override
    public void reloadChild() {
        if (!this.isWaiting() || !this.isLoading()) {
            try {
                this.refreshNode();
                this.loadChild();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        }
    }

    /**
     * 加载子节点
     *
     * @param loop 递归加载
     */
    public void loadChild(boolean loop) {
        int limit = ShellSettingStore.SETTING.nodeLoadLimit();
        this.loadChild(loop, limit);
    }

    /**
     * 加载子节点
     *
     * @param loop  递归加载
     * @param limit 限制数量
     */
    public void loadChild(boolean loop, int limit) {
        if (this.isCanceled()) {
            return;
        }
        // 当前树
        ShellZKNodeTreeView treeView = this.getTreeView();
        // 获取选中节点
        TreeItem<?> selectedItem = treeView == null ? null : treeView.getSelectedItem();
        try {
            // 设置标志位
            this.setLoaded(true);
            this.setLoading(true);
//            // 没有子节点
//            if (!this.value.hasChildren()) {
//                this.clearChild();
//            } else {
//                // 添加列表
//                List<TreeItem<?>> addList = new ArrayList<>();
//                // 移除列表
//                List<TreeItem<?>> delList = new ArrayList<>();
//                // 已存在节点
//                List<String> paths = this.itemChildren().parallelStream().map(ShellZKNodeTreeItem::nodePath).toList();
//                // 获取节点列表
//                List<ShellZKNode> list = ShellZKNodeUtil.getChildNode(this.client(), this.nodePath(), paths, limit);
//                // 遍历列表寻找待更新或者待添加节点
//                for (ShellZKNode node : list) {
//                    // 添加到集合
//                    addList.add(new ShellZKNodeTreeItem(node, this.getTreeView()));
//                }
//                // 限制节点加载数量
//                if (limit > 0 && list.size() >= limit) {
//                    ShellZKMoreTreeItem moreItem = this.moreChildren();
//                    if (moreItem != null) {
//                        delList.add(moreItem);
//                    }
//                    addList.add(new ShellZKMoreTreeItem(this.getTreeView()));
//                } else { // 处理不限制的情况
//                    ShellZKMoreTreeItem moreItem = this.moreChildren();
//                    if (moreItem != null) {
//                        delList.add(moreItem);
//                    }
//                }
//                // 删除节点
//                this.removeChild(delList);
//                // 添加节点
//                this.addChild(addList);
//                // 递归处理
//                if (loop && this.itemChildrenSize() > 0) {
////                    List<Runnable> tasks = new ArrayList<>();
////                    for (ShellZKNodeTreeItem item : this.itemChildren()) {
////                        tasks.add(() -> item.loadChild(true, limit));
////                    }
////                    ThreadUtil.submitVirtual(tasks);
//                    for (ShellZKNodeTreeItem item : this.itemChildren()) {
//                        item.loadChild(true, limit);
//                    }
//                }
//            }
            this.doLoadChild(loop, limit);
        } catch (Exception ex) {
            this.setLoaded(false);
            // 无权限
            if ((ex instanceof KeeperException.NoAuthException)) {
                this.setNeedAuth(true);
            } else if (!this.isCanceled() && !this.client().isConnected()) {  // 非取消、连接关闭情况下，则抛出异常
                throw new RuntimeException(ex);
            }
        } finally {
            this.setLoading(false);
            this.doFilter();
            this.doSort();
            // 选中节点
            if (selectedItem != null) {
                treeView.select(selectedItem);
            }
        }
    }

    /**
     * 加载子节点
     *
     * @param loop  递归加载
     * @param limit 限制数量
     */
    private void doLoadChild(boolean loop, int limit) throws Exception {
        // 没有子节点
        if (!this.value.hasChildren()) {
            this.clearChild();
        } else {
            // 添加列表
            List<TreeItem<?>> addList = new ArrayList<>();
            // 移除列表
            List<TreeItem<?>> delList = new ArrayList<>();
            // 已存在节点
            List<String> paths = this.itemChildren().parallelStream().map(ShellZKNodeTreeItem::nodePath).toList();
            // 获取节点列表
            List<ShellZKNode> list = ShellZKNodeUtil.getChildNode(this.client(), this.nodePath(), paths, limit);
            // 遍历列表寻找待更新或者待添加节点
            for (ShellZKNode node : list) {
                // 添加到集合
                addList.add(new ShellZKNodeTreeItem(node, this.getTreeView()));
            }
            // 限制节点加载数量
            if (limit > 0 && list.size() >= limit) {
                ShellZKMoreTreeItem moreItem = this.moreChildren();
                if (moreItem != null) {
                    delList.add(moreItem);
                }
                addList.add(new ShellZKMoreTreeItem(this.getTreeView()));
            } else { // 处理不限制的情况
                ShellZKMoreTreeItem moreItem = this.moreChildren();
                if (moreItem != null) {
                    delList.add(moreItem);
                }
            }
            // 删除节点
            this.removeChild(delList);
            // 添加节点
            this.addChild(addList);
            // 递归处理
            if (loop && this.itemChildrenSize() > 0) {
                for (ShellZKNodeTreeItem item : this.itemChildren()) {
                    item.doLoadChild(true, limit);
                }
            }
        }
    }

    /**
     * 复制节点
     *
     * @param node zk节点
     */
    public void copy(ShellZKNode node) {
        this.value.copy(node);
    }

    /**
     * 节点比较
     *
     * @param node 目标节点
     * @return 结果
     */
    public boolean nodeEquals(ShellZKNode node) {
        return this.value.nodeEquals(node);
    }

    /**
     * 子节点-更多
     *
     * @return ShellZKMoreTreeItem
     */
    protected ShellZKMoreTreeItem moreChildren() {
        List<?> list = super.unfilteredChildren().filtered(e -> e instanceof ShellZKMoreTreeItem);
        return list.isEmpty() ? null : (ShellZKMoreTreeItem) list.getFirst();
    }

    /**
     * 子节点-node节点列表
     *
     * @return node节点列表
     */
    public List<ShellZKNodeTreeItem> itemChildren() {
        List list = super.unfilteredChildren().filtered(e -> e instanceof ShellZKNodeTreeItem);
        return list;
    }

    /**
     * 子节点-node节点数量
     *
     * @return node节点数量
     */
    public int itemChildrenSize() {
        return super.unfilteredChildren().filtered(e -> e instanceof ShellZKNodeTreeItem).size();
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ShellZKMoreTreeItem) {
            return -1;
        }
        if (o instanceof ShellZKNodeTreeItem item) {
            return Comparator.comparing(ShellZKNodeTreeItem::nodePath).compare(this, item);
        }
        return super.compareTo(o);
    }

    @Override
    public void sortAsc() {
        if (super.isSortable() && !super.isSorting()) {
            super.sortAsc();
            this.itemChildren().forEach(ShellZKNodeTreeItem::sortAsc);
        }
    }

    @Override
    public void sortDesc() {
        if (super.isSortable() && !super.isSorting()) {
            super.sortDesc();
            this.itemChildren().forEach(ShellZKNodeTreeItem::sortDesc);
        }
    }

    /**
     * 节点是否被收藏
     */
    public boolean isCollect() {
        return ShellZKCollectStore.INSTANCE.exist(this.iid(), this.decodeNodePath());
    }

    /**
     * 收藏节点
     */
    public void collect() {
        ShellZKCollectStore.INSTANCE.replace(this.iid(), this.decodeNodePath());
    }

    /**
     * 取消收藏节点
     */
    public void unCollect() {
        ShellZKCollectStore.INSTANCE.delete(this.iid(), this.decodeNodePath());
    }

    private String iid() {
        return this.zkConnect().getId();
    }

    ///**
    // * 数据是否太大
    // * 目前限制大小为1024kb
    // *
    // * @return 结果
    // */
    // public boolean isDataTooBig() {
    //    if (this.isDataUnsaved()) {
    //        return this.value.getUnsavedDataSize() > 1024 * 1024;
    //    }
    //    return this.value.getNodeDataSize() > 1024 * 1024;
    //}

    /**
     * 解码的节点路径
     *
     * @return 解码的节点路径
     */
    public String decodeNodePath() {
        return this.value.decodeNodePath();
    }

    /**
     * 删除权限
     *
     * @param acl 权限
     * @return 当前状态
     * @throws Exception 异常
     */
    public Stat deleteACL(ShellZKACL acl) throws Exception {
        return this.client().deleteACL(this.nodePath(), acl);
    }

    /**
     * 获取权限
     *
     * @return 权限
     */
    public List<ShellZKACL> acl() {
        return this.value.acl();
    }

    /**
     * 友好状态信息
     *
     * @return 友好状态信息
     */
    public List<FriendlyInfo<Stat>> statInfos() {
        return this.value.statInfos();
    }

    /**
     * 是否有读取权限
     *
     * @return 结果
     */
    public boolean hasReadPerm() {
        return this.value.hasReadPerm();
    }

    /**
     * 是否持久节点
     *
     * @return 结果
     */
    public boolean isPersistentNode() {
        return this.value.isPersistent();
    }

    /**
     * 是否临时节点
     *
     * @return 结果
     */
    public boolean isEphemeralNode() {
        return this.value.isEphemeral();
    }

    /**
     * 是否根节点
     *
     * @return 结果
     */
    public boolean isRootNode() {
        return this.value.isRoot();
    }

    /**
     * 是否子节点
     *
     * @return 结果
     */
    public boolean isChildrenNode() {
        return this.value.isChildren();
    }

    /**
     * 是否父节点
     *
     * @return 结果
     */
    public boolean isParentNode() {
        return this.value.isParent();
    }

    /**
     * acl是否为空
     *
     * @return 结果
     */
    public boolean aclEmpty() {
        return this.value.aclEmpty();
    }

    /**
     * 获取zk连接名称
     *
     * @return zk连接名称
     */
    public String connectName() {
        return this.zkConnect().getName();
    }

    /**
     * 解码的节点名称
     *
     * @return 解码的节点名称
     */
    public String decodeNodeName() {
        return this.value.decodeNodeName();
    }

    /**
     * 获取配额
     *
     * @return 配额
     */
    public StatsTrack quota() throws Exception {
        if (this.value.quota() == null) {
            this.refreshQuota();
        }
        return this.value.quota();
    }

    /**
     * 保存配额
     *
     * @param bytes 配额数据大小
     * @param count 配额子节点数量
     * @throws Exception 异常
     */
    public void saveQuota(long bytes, int count) throws Exception {
        if (bytes == -1 && count == -1) {
            this.client().delQuota(this.nodePath(), true, true);
        } else {
            this.client().delQuota(this.nodePath(), true, true);
            this.client().createQuota(this.nodePath(), bytes, count);
        }
        this.refreshQuota();
    }

    /**
     * 是否存在摘要权限
     *
     * @param digest 摘要
     * @return 结果
     */
    public boolean existDigestACL(String digest) {
        return this.value.existDigestACL(digest);
    }

    /**
     * 是否有开放权限
     *
     * @return 结果
     */
    public boolean hasWorldACL() {
        return this.value.hasWorldACL();
    }

    /**
     * 是否存在IP权限
     *
     * @param ip ip内容
     * @return 结果
     */
    public boolean existIPACL(String ip) {
        return this.value.existIPACL(ip);
    }

    /**
     * 获取加载耗时
     *
     * @return 加载耗时
     */
    public short loadTime() {
        return this.value.loadTime() == 0 ? 1 : this.value.loadTime();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    /**
     * 保存数据历史
     */
    private void saveHistory() throws Exception {
        // ShellZKHistoryData history = new ShellZKHistoryData();
        // history.setPath(this.nodePath());
        // history.setData(this.getUnsavedData());
        // history.setIid(this.zkConnect().getId());
        // ShellZKDataHistoryStore.INSTANCE.replace(history, this.client());
        // ShellZKEventUtil.dataHistoryAdded(history, this);
        ShellZKDataUtil.addHistory(this.nodePath(), this.getUnsavedData(), this.client());
    }

    /**
     * 获取子节点数量
     *
     * @return 子节点数量
     */
    public int getNumChildren() {
        return this.value.getNumChildren();
    }

    /**
     * 授权变化事件
     */
    public void authChanged() throws Exception {
        this.setNeedAuth(false);
        this.refreshNode();
        this.loadRoot();
        this.refresh();
    }

    /**
     * 加载根节点
     */
    public void loadRoot() {
        if (this.isRootNode()) {
            ShellSetting setting = ShellSettingStore.SETTING;
            if (setting.isLoadFirst()) {
                this.loadChild();
            } else if (setting.isLoadAll()) {
                this.loadChildAll();
            }
        }
    }

    /**
     * 获取未保存的数据
     *
     * @return 未保存的数据
     */
    public byte[] getUnsavedData() {
        return this.value.getUnsavedData();
    }

    @Override
    public synchronized void destroy() {
        this.value.clearNodeData();
        this.value.clearUnsavedData();
        if (!this.isRootNode()) {
            this.value = null;
            super.destroy();
        }
    }

    /**
     * 获取数据大小
     *
     * @return 数据大小
     */
    public int dataSize() {
        return this.getData().length;
    }

    /**
     * 获取数据大小信息
     *
     * @return 数据大小信息
     */
    public String dataSizeInfo() {
        int dataSize = this.dataSize();
        if (dataSize < 0) {
            return "N/A";
        }
        if (dataSize < 1024) {
            return dataSize + "bytes";
        }
        if (dataSize < 1024 * 1024) {
            return dataSize / 1024.0 + "KB";
        }
        if (dataSize < 1024 * 1024 * 1024) {
            return dataSize / 1024.0 / 1024 + "MB";
        }
        return dataSize / 1024.0 / 1024 / 1024 + "GB";
    }

}
