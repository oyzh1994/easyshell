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
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * shell树根节点
 *
 * @author oyzh
 * @since 2025/03/29
 */
public class ShellConnectRootTreeItem extends RichTreeItem<ShellConnectRootTreeItemValue> implements ShellConnectManager {

    /**
     * shell分组储存
     */
    private final ShellGroupStore groupStore = ShellGroupStore.INSTANCE;

    /**
     * shell连接储存
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    // /**
    //  * shell设
    //  */
    // private final ShellSetting setting = ShellSettingStore.SETTING;
    //
    // /**
    //  * shell设置储存
    //  */
    // private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    public ShellConnectRootTreeItem(ShellConnectTreeView treeView) {
        super(treeView);
        this.setValue(new ShellConnectRootTreeItemValue());
        // 加载子节点
        this.loadChild();
    }

    @Override
    public ShellConnectTreeView getTreeView() {
        return (ShellConnectTreeView) super.getTreeView();
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem addConnect = MenuItemHelper.addConnect("12", this::addConnect);
        items.add(addConnect);
        FXMenuItem addGroup = MenuItemHelper.addGroup("12", this::addGroup);
        items.add(addGroup);
        items.add(MenuItemHelper.separator());
        FXMenuItem exportData = MenuItemHelper.exportData("12", this::exportData);
        items.add(exportData);
        FXMenuItem importData = MenuItemHelper.importData("12", this::importData);
        items.add(importData);
        FXMenuItem refreshData = MenuItemHelper.refreshData("12", this::reloadChild);
        items.add(refreshData);
        // FXMenuItem moreInfo = MenuItemHelper.moreInfo("12", this::moreInfo);
        // moreInfo.setDisable(this.isChildEmpty());

        // items.add(moreInfo);
        items.addAll(this.getTreeView().getMenuItems());

        return items;
        // return this.getTreeView().getMenuItems();
    }

    // /**
    //  * 显示更多信息
    //  */
    // private void moreInfo() {
    //     this.setting.setConnectShowMoreInfo(!this.setting.isConnectShowMoreInfo());
    //     this.settingStore.update(this.setting);
    //     this.refresh();
    // }

    /**
     * 导出数据
     */
    private void exportData() {
//        ShellEventUtil.showExportConnect();
        ShellViewFactory.dataExport();
    }
//
//     /**
//      * 拖拽文件
//      *
//      * @param files 文件
//      */
//     public void dragFile(List<File> files) {
//         if (CollectionUtil.isEmpty(files)) {
//             return;
//         }
//         if (files.size() != 1) {
//             MessageBox.warn(I18nHelper.onlySupportSingleFile());
//             return;
//         }
//         File file = CollectionUtil.getFirst(files);
// //        ShellEventUtil.showImportConnect(file);
//         ShellViewFactory.importConnect(file);
//     }

    /**
     * 导入数据
     */
    private void importData() {
//        ShellEventUtil.showImportConnect(null);
        ShellViewFactory.dataImport(null);
    }

    /**
     * 添加连接
     */
    private void addConnect() {
//        ShellEventUtil.showAddConnect();
        ShellViewFactory.addConnectGuid(null);
    }

    /**
     * 添加分组
     */
    public void addGroup() {
        String groupName = MessageBox.prompt(I18nHelper.contentTip1());
        // 名称为null，则忽略
        if (groupName == null) {
            return;
        }
        // 不能为空
        if (StringUtil.isBlank(groupName)) {
            MessageBox.warn(I18nHelper.nameCanNotEmpty());
            return;
        }
        // // 检查是否存在
        // if (this.groupStore.exist(groupName)) {
        //     MessageBox.warn(I18nHelper.contentAlreadyExists());
        //     return;
        // }
        ShellGroup group = new ShellGroup();
        group.setName(groupName);
        // if (this.groupStore.replace(group)) {
        //     this.addChild(new ShellConnectGroupTreeItem(group, this.getTreeView()));
        //     ShellEventUtil.groupAdded(groupName);
        // } else {
        //     MessageBox.warn(I18nHelper.operationFail());
        // }
        this.addGroup(group);
    }

    /**
     * 获取分组树节点组件
     *
     * @param groupId 分组id
     */
    private ShellConnectGroupTreeItem getGroupItem(String groupId) {
        if (StringUtil.isNotBlank(groupId)) {
            List<ShellConnectGroupTreeItem> items = this.getAllGroupItems();
            Optional<ShellConnectGroupTreeItem> groupTreeItem = items.parallelStream().filter(g -> Objects.equals(g.value().getGid(), groupId)).findAny();
            return groupTreeItem.orElse(null);
        }
        return null;
    }

   @Override
    public List<ShellConnectGroupTreeItem> getGroupItems() {
        List<ShellConnectGroupTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof ShellConnectGroupTreeItem groupTreeItem) {
                items.add(groupTreeItem);
            }
        }
        return items;
    }

    @Override
    public List<ShellConnectGroupTreeItem> getAllGroupItems() {
        List<ShellConnectGroupTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof ShellConnectGroupTreeItem groupTreeItem) {
                items.addAll(groupTreeItem.getAllGroupItems());
            }
        }
        return items;
    }

    /**
     * 连接新增事件
     *
     * @param shellConnect ssh连接
     */
    public void connectAdded(ShellConnect shellConnect) {
        this.addConnect(shellConnect);
    }

    /**
     * 连接变更事件
     *
     * @param shellConnect ssh连接
     */
    public void connectUpdated(ShellConnect shellConnect) {
        f1:
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof ShellConnectTreeItem connectTreeItem) {
                if (connectTreeItem.value() == shellConnect) {
                    connectTreeItem.value(shellConnect);
                    break;
                }
            } else if (item instanceof ShellConnectGroupTreeItem groupTreeItem) {
                for (ShellConnectTreeItem connectTreeItem : groupTreeItem.getConnectItems()) {
                    if (connectTreeItem.value() == shellConnect) {
                        connectTreeItem.value(shellConnect);
                        break f1;
                    }
                }
            }
        }
    }

    @Override
    public void addGroup(ShellGroup group) {
        group.setPid(null);
        if (this.groupStore.replace(group)) {
            this.addChild(new ShellConnectGroupTreeItem(group, this.getTreeView()));
            ShellEventUtil.groupAdded(group.getName());
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    @Override
    public void addGroupItem(ShellConnectGroupTreeItem item) {
        if (!this.containsChild(item)) {
            item.value().setPid(null);
            this.groupStore.replace(item.value());
            super.addChild(item);
        }
    }

    @Override
    public void addConnect(ShellConnect info) {
        ShellConnectGroupTreeItem groupItem = this.getGroupItem(info.getGroupId());
        if (groupItem == null) {
            super.addChild(new ShellConnectTreeItem(info, this.getTreeView()));
            this.expend();
        } else {
            groupItem.addConnect(info);
        }
    }

    @Override
    public void addConnectItem(ShellConnectTreeItem item) {
        if (!this.containsChild(item)) {
            if (item.value().getGroupId() != null) {
                item.value().setGroupId(null);
                this.connectStore.update(item.value());
            }
            super.addChild(item);
            this.expend();
        }
    }

    @Override
    public void addConnectItems(List<ShellConnectTreeItem> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.addChild((List) items);
            this.expend();
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
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellConnectTreeItem connectTreeItem) {
                items.add(connectTreeItem);
            }
        }
        return items;
    }

    @Override
    public List<ShellConnectTreeItem> getAllConnectItems() {
        List<ShellConnectTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellConnectTreeItem connectTreeItem) {
                items.add(connectTreeItem);
            } else if (child instanceof ShellConnectGroupTreeItem groupTreeItem) {
                items.addAll(groupTreeItem.getAllConnectItems());
            }
        }
        return items;
    }

    @Override
    public List<ShellConnectTreeItem> getConnectedItems() {
        List<ShellConnectTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof ShellConnectTreeItem connectTreeItem) {
//                if (connectTreeItem.isConnected()) {
                items.add(connectTreeItem);
//                }
            } else if (item instanceof ShellConnectGroupTreeItem groupTreeItem) {
                items.addAll(groupTreeItem.getConnectedItems());
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
            return StringUtil.isNotBlank(connectTreeItem.getGroupId());
        }
        if (item instanceof ShellConnectGroupTreeItem groupTreeItem) {
            return StringUtil.isNotBlank(groupTreeItem.getParentId());
        }
        return false;
    }

    @Override
    public void onDropNode(DragNodeItem item) {
        if (item instanceof ShellConnectTreeItem connectTreeItem) {
            connectTreeItem.remove();
            this.addConnectItem(connectTreeItem);
        } else if (item instanceof ShellConnectGroupTreeItem groupTreeItem) {
            groupTreeItem.remove();
            this.addGroupItem(groupTreeItem);
        }
    }

    @Override
    public void reloadChild() {
        super.reloadChild();
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        // // 关闭连接
        // List<ShellConnectTreeItem> connectedItems = this.getConnectedItems();
        // for (ShellConnectTreeItem item : connectedItems) {
        //     item.closeConnect();
        // }
        // 初始化分组
        List<ShellGroup> groups = this.groupStore.load();
        if (CollectionUtil.isNotEmpty(groups)) {
            // List<TreeItem<?>> list = new ArrayList<>();
            // for (ShellGroup group : groups) {
            //     list.add(new ShellConnectGroupTreeItem(group, this.getTreeView()));
            // }
            // this.addChild(list);
            this.addGroupChild(groups, null, this);
        }
        // 初始化连接
        List<ShellConnect> connects = this.connectStore.loadFull();
        if (CollectionUtil.isNotEmpty(connects)) {
            for (ShellConnect connect : connects) {
                this.addConnect(connect);
            }
        }
        this.refresh();
    }

    /**
     * 添加分组节点
     *
     * @param groups 分组列表
     * @param pid    父id
     * @param pItem  父节点
     */
    private void addGroupChild(List<ShellGroup> groups, String pid, RichTreeItem<?> pItem) {
        if (CollectionUtil.isEmpty(groups)) {
            return;
        }
        // 节点列表
        List<TreeItem<?>> items = new ArrayList<>();
        // 寻找当前的分组列表
        List<ShellGroup> list;
        if (StringUtil.isBlank(pid)) {
            list = groups.stream().filter(g -> StringUtil.isBlank(g.getPid())).toList();
        } else {
            list = groups.stream().filter(g -> StringUtil.equals(g.getPid(), pid)).toList();
        }
        // 处理分组
        for (ShellGroup group : list) {
            ShellConnectGroupTreeItem groupItem = new ShellConnectGroupTreeItem(group, this.getTreeView());
            this.addGroupChild(groups, group.getGid(), groupItem);
            items.add(groupItem);
        }
        // 添加节点
        pItem.addChild(items);
    }
}
