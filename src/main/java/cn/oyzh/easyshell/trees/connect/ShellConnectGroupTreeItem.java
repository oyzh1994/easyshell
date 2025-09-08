package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellGroupStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * shell分组节点
 *
 * @author oyzh
 * @since 2025/05/12
 */
public class ShellConnectGroupTreeItem extends RichTreeItem<ShellConnectGroupTreeItemValue> implements ShellConnectManager {

    /**
     * 分组对象
     */
    private final ShellGroup value;

    public ShellGroup value() {
        return value;
    }

    /**
     * shell分组储存
     */
    private final ShellGroupStore groupStore = ShellGroupStore.INSTANCE;

    /**
     * shell连接储存
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    public ShellConnectGroupTreeItem(ShellGroup group, RichTreeView treeView) {
        super(treeView);
        this.value = group;
        this.setValue(new ShellConnectGroupTreeItemValue(this));
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
    public ShellConnectTreeView getTreeView() {
        return (ShellConnectTreeView) super.getTreeView();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem addConnect = MenuItemHelper.addConnect("12", this::addConnect);
        FXMenuItem renameGroup = MenuItemHelper.renameGroup("12", this::rename);
        FXMenuItem delGroup = MenuItemHelper.deleteGroup("12", this::delete);
        items.add(addConnect);
        items.add(renameGroup);
        items.add(delGroup);
        items.add(MenuItemHelper.separator());
        items.addAll(this.getTreeView().getMenuItems());
        return items;
    }

    @Override
    public void rename() {
        String groupName = MessageBox.prompt(I18nHelper.pleaseInputGroupName(), this.value.getName());
        // 名称为null或者跟当前名称相同，则忽略
        if (StringUtil.isBlank(groupName) || Objects.equals(groupName, this.value.getName())) {
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
            ShellEventUtil.groupRenamed(groupName, oldName);
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
            List<ShellConnectTreeItem> childes = this.getConnectItems();
            childes.forEach(c -> c.value().setGroupId(null));
            // 连接转移到父节点
            this.parent().addConnectItems(childes);
        }
        // 发送事件
        ShellEventUtil.groupDeleted(this.value.getName());
        // 移除节点
        this.remove();
    }

    /**
     * 添加连接
     */
    private void addConnect() {
//        ShellEventUtil.showAddConnect(this.value);
        ShellViewFactory.addConnectGuid(this.value);
    }

    @Override
    public ShellConnectRootTreeItem parent() {
        TreeItem<?> treeItem = this.getParent();
        return (ShellConnectRootTreeItem) treeItem;
    }

    @Override
    public void addConnect(ShellConnect shellConnect) {
        this.addConnectItem(new ShellConnectTreeItem(shellConnect, this.getTreeView()));
    }

    @Override
    public void addConnectItem(ShellConnectTreeItem item) {
        if (!this.containsChild(item)) {
            if (!Objects.equals(item.value().getGroupId(), this.value.getGid())) {
                item.value().setGroupId(this.value.getGid());
                this.connectStore.replace(item.value());
            }
            super.addChild(item);
        }
    }

    @Override
    public void addConnectItems(List<ShellConnectTreeItem> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.addChild((List) items);
        }
    }

    @Override
    public boolean delConnectItem(ShellConnectTreeItem item) {
        // 删除连接
        if (this.connectStore.delete(item.value())) {
            this.removeChild(item);
            return true;
        }
        return false;
    }

    @Override
    public List<ShellConnectTreeItem> getConnectItems() {
        List<ShellConnectTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof ShellConnectTreeItem treeItem) {
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
        if (item instanceof ShellConnectTreeItem connectTreeItem) {
            return !Objects.equals(connectTreeItem.value().getGroupId(), this.value.getGid());
        }
        return false;
    }

    @Override
    public void onDropNode(DragNodeItem item) {
        if (item instanceof ShellConnectTreeItem connectTreeItem) {
            connectTreeItem.remove();
            this.addConnectItem(connectTreeItem);
        }
    }

    public String getGroupId() {
        return this.value.getGid();
    }

    public String getGroupName() {
        return this.value.getName();
    }
}
