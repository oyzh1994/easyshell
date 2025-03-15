package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.controller.connect.SSHAddConnectController;
import cn.oyzh.easyssh.domain.ShellConnect;
import cn.oyzh.easyssh.domain.SSHGroup;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.store.SSHConnectStore;
import cn.oyzh.easyssh.store.SSHGroupStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ssh分组节点
 *
 * @author oyzh
 * @since 2023/05/12
 */
public class SSHGroupTreeItem extends RichTreeItem<SSHGroupTreeItemValue> implements SSHConnectManager {

    /**
     * 分组对象
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private final SSHGroup value;

    /**
     * ssh分组储存
     */
    private final SSHGroupStore groupStore = SSHGroupStore.INSTANCE;

    /**
     * ssh连接储存
     */
    private final SSHConnectStore connectStore = SSHConnectStore.INSTANCE;

    public SSHGroupTreeItem(@NonNull SSHGroup group, @NonNull RichTreeView treeView) {
        super(treeView);
        this.value = group;
        this.setValue(new SSHGroupTreeItemValue(this));
        // 判断是否展开
        this.setExpanded(this.value.isExpand());
        // 监听收缩变化
        super.addEventHandler(branchCollapsedEvent(), (EventHandler<TreeModificationEvent<TreeItem<?>>>) event -> {
            if (this.value.isExpand()) {
                this.value.setExpand(false);
                this.groupStore.update(this.value);
            }
        });
        // 监听展开变化
        super.addEventHandler(branchExpandedEvent(), (EventHandler<TreeModificationEvent<TreeItem<?>>>) event -> {
            if (!this.value.isExpand()) {
                this.value.setExpand(true);
                this.groupStore.update(this.value);
            }
        });
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(4);
        FXMenuItem addConnect = MenuItemHelper.addConnect("12", this::addConnect);
        FXMenuItem renameGroup = MenuItemHelper.renameGroup("12", this::rename);
        FXMenuItem delGroup = MenuItemHelper.deleteGroup("12", this::delete);
        items.add(addConnect);
        items.add(renameGroup);
        items.add(delGroup);
        return items;
    }

    @Override
    public void rename() {
        String groupName = MessageBox.prompt(I18nHelper.contentTip1(), this.value.getName());
        // 名称为null或者跟当前名称相同，则忽略
        if (groupName == null || Objects.equals(groupName, this.value.getName())) {
            return;
        }
        // 检查名称
        if (StringUtil.isBlank(groupName)) {
            return;
        }
        // 检查是否存在
        if (this.groupStore.exist(groupName)) {
            MessageBox.warn(I18nHelper.groupAlreadyExists());
            return;
        }
        // 旧名称
        String oldName = this.value.getName();
        // 修改名称
        this.value.setName(groupName);
        if (this.groupStore.replace(this.value)) {
            this.refresh();
            SSHEventUtil.groupRenamed(groupName, oldName);
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    @Override
    public void delete() {
        if (this.isChildEmpty() && !MessageBox.confirm(I18nHelper.deleteGroupTip1())) {
            return;
        }
        if (!this.isChildEmpty() && !MessageBox.confirm(I18nHelper.deleteGroupTip2())) {
            return;
        }
        // 删除失败
        if (!this.groupStore.delete(this.value.getName())) {
            MessageBox.warn(I18nHelper.operationFail());
            return;
        }
        // 处理连接
        if (!this.isChildEmpty()) {
            // 清除分组id
            List<SSHConnectTreeItem> childes = this.getConnectItems();
            childes.forEach(c -> c.value().setGroupId(null));
            // 连接转移到父节点
            this.parent().addConnectItems(childes);
        }
        // 发送事件
        SSHEventUtil.groupDeleted(this.value.getName());
        // 移除节点
        this.remove();
    }

    /**
     * 添加连接
     */
    private void addConnect() {
        StageAdapter fxView = StageManager.parseStage(SSHAddConnectController.class, this.window());
        fxView.setProp("group", this.value);
        fxView.display();
    }

    @Override
    public SSHRootTreeItem parent() {
        TreeItem<?> treeItem = this.getParent();
        return (SSHRootTreeItem) treeItem;
    }

    @Override
    public void addConnect(@NonNull ShellConnect shellConnect) {
        this.addConnectItem(new SSHConnectTreeItem(shellConnect, this.getTreeView()));
    }

    @Override
    public void addConnectItem(@NonNull SSHConnectTreeItem item) {
        if (!this.containsChild(item)) {
            if (!Objects.equals(item.value().getGroupId(), this.value.getGid())) {
                item.value().setGroupId(this.value.getGid());
               this.connectStore.replace(item.value());
            }
            super.addChild(item);
        }
    }

    @Override
    public void addConnectItems(@NonNull List<SSHConnectTreeItem> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.addChild((List) items);
        }
    }

    @Override
    public boolean delConnectItem(@NonNull SSHConnectTreeItem item) {
        // 删除连接
        if (this.connectStore.delete(item.value())) {
            this.removeChild(item);
            return true;
        }
        return false;
    }

    @Override
    public List<SSHConnectTreeItem> getConnectItems() {
        List<SSHConnectTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof SSHConnectTreeItem treeItem) {
                items.add(treeItem);
            }
        }
        return items;
    }

    @Override
    public boolean allowDrop() {
        return true;
    }

    @Override
    public boolean allowDropNode(DragNodeItem item) {
        if (item instanceof SSHConnectTreeItem connectTreeItem) {
            return !Objects.equals(connectTreeItem.value().getGroupId(), this.value.getGid());
        }
        return false;
    }

    @Override
    public void onDropNode(DragNodeItem item) {
        if (item instanceof SSHConnectTreeItem connectTreeItem) {
            connectTreeItem.remove();
            this.addConnectItem(connectTreeItem);
        }
    }

    public String getGid() {
        return this.value.getGid();
    }

}
