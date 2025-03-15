package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.connect.ShellAddConnectController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.dto.ShellConnectExport;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellGroupStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import lombok.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ssh树根节点
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class ShellRootTreeItem extends RichTreeItem<ShellRootTreeItemValue> implements ShellConnectManager {

    /**
     * ssh分组储存
     */
    private final ShellGroupStore groupStore = ShellGroupStore.INSTANCE;

    /**
     * ssh连接储存
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    public ShellRootTreeItem(@NonNull ShellConnectTreeView treeView) {
        super(treeView);
        this.setValue(new ShellRootTreeItemValue());
        // 加载子节点
        this.loadChild();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(4);
        FXMenuItem addConnect = MenuItemHelper.addConnect("12", this::addConnect);
        FXMenuItem exportConnect = MenuItemHelper.exportConnect("12", this::exportConnect);
        FXMenuItem importConnect = MenuItemHelper.importConnect("12", this::importConnect);
        FXMenuItem addGroup = MenuItemHelper.addGroup("12", this::addGroup);

        exportConnect.setDisable(this.isChildEmpty());

        items.add(addConnect);
        items.add(exportConnect);
        items.add(importConnect);
        items.add(addGroup);
        return items;
    }

    /**
     * 导出连接
     */
    public void exportConnect() {
        List<ShellConnect> infos = this.connectStore.load();
        if (infos.isEmpty()) {
            MessageBox.warn(I18nHelper.connectionIsEmpty());
            return;
        }
        ShellConnectExport export = ShellConnectExport.fromConnects(infos);
        FileExtensionFilter extensionFilter = FXChooser.jsonExtensionFilter();
        File file = FileChooserHelper.save(I18nHelper.saveConnection(), I18nResourceBundle.i18nString("base.ssh", "base.connect", "base._json"), extensionFilter);
        if (file != null) {
            try {
                FileUtil.writeUtf8String(export.toJSONString(), file);
                MessageBox.okToast(I18nHelper.exportConnectionSuccess());
            } catch (Exception ex) {
                MessageBox.exception(ex, I18nHelper.exportConnectionFail());
            }
        }
    }

    /**
     * 拖拽文件
     *
     * @param files 文件
     */
    public void dragFile(List<File> files) {
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        if (files.size() != 1) {
            MessageBox.warn(I18nHelper.onlySupportSingleFile());
            return;
        }
        File file = CollectionUtil.getFirst(files);
        // 解析文件
        this.parseConnect(file);
    }

    /**
     * 导入连接
     */
    public void importConnect() {
        FileExtensionFilter filter1 = FXChooser.jsonExtensionFilter();
        File file = FileChooserHelper.choose(I18nHelper.chooseFile(), filter1);
        // 解析文件
        this.parseConnect(file);
    }

    /**
     * 解析连接文件
     *
     * @param file 文件
     */
    private void parseConnect(File file) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            MessageBox.warn(I18nHelper.fileNotExists());
            return;
        }
        if (file.isDirectory()) {
            MessageBox.warn(I18nHelper.notSupportFolder());
            return;
        }
        if (!FileNameUtil.isJsonType(FileNameUtil.extName(file.getName()))) {
            MessageBox.warn(I18nHelper.invalidFormat());
            return;
        }
        if (file.length() == 0) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
            return;
        }
        try {
            String text = FileUtil.readUtf8String(file);
            ShellConnectExport export = ShellConnectExport.fromJSON(text);
            List<ShellConnect> connects = export.getConnects();
            if (CollectionUtil.isNotEmpty(connects)) {
                for (ShellConnect connect : connects) {
                    if (!this.connectStore.replace(connect)) {
                        MessageBox.warn(I18nHelper.connect() + " : " + connect.getName() + " " + I18nHelper.importFail());
                    }
                }
                // 重新加载节点
                this.loadChild();
                // 提示成功
                MessageBox.okToast(I18nHelper.importConnectionSuccess());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, I18nHelper.importConnectionFail());
        }
    }

    /**
     * 添加连接
     */
    private void addConnect() {
        StageManager.showStage(ShellAddConnectController.class, this.window());
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
        // 检查是否存在
        if (this.groupStore.exist(groupName)) {
            MessageBox.warn(I18nHelper.contentAlreadyExists());
            return;
        }
        ShellGroup group = new ShellGroup();
        group.setName(groupName);
        if (this.groupStore.replace(group)) {
            this.addChild(new ShellGroupTreeItem(group, this.getTreeView()));
            ShellEventUtil.groupAdded(groupName);
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 获取分组树节点组件
     *
     * @param groupId 分组id
     */
    private ShellGroupTreeItem getGroupItem(String groupId) {
        if (StringUtil.isNotBlank(groupId)) {
            List<ShellGroupTreeItem> items = this.getGroupItems();
            Optional<ShellGroupTreeItem> groupTreeItem = items.parallelStream().filter(g -> Objects.equals(g.value().getGid(), groupId)).findAny();
            return groupTreeItem.orElse(null);
        }
        return null;
    }

    /**
     * 获取分组树节点组件
     *
     * @return 分组树节点组件
     */
    private List<ShellGroupTreeItem> getGroupItems() {
        List<ShellGroupTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof ShellGroupTreeItem groupTreeItem) {
                items.add(groupTreeItem);
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
            } else if (item instanceof ShellGroupTreeItem groupTreeItem) {
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
    public void addConnect(@NonNull ShellConnect info) {
        ShellGroupTreeItem groupItem = this.getGroupItem(info.getGroupId());
        if (groupItem == null) {
            super.addChild(new ShellConnectTreeItem(info, this.getTreeView()));
            this.expend();
        } else {
            groupItem.addConnect(info);
        }
    }

    @Override
    public void addConnectItem(@NonNull ShellConnectTreeItem item) {
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
    public void addConnectItems(@NonNull List<ShellConnectTreeItem> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.addChild((List) items);
            this.expend();
        }
    }

    @Override
    public boolean delConnectItem(@NonNull ShellConnectTreeItem item) {
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
            } else if (child instanceof ShellGroupTreeItem groupTreeItem) {
                items.addAll(groupTreeItem.getConnectItems());
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
            } else if (item instanceof ShellGroupTreeItem groupTreeItem) {
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
        return item instanceof ShellConnectTreeItem;
    }

    @Override
    public void onDropNode(DragNodeItem item) {
        if (item instanceof ShellConnectTreeItem connectTreeItem) {
            connectTreeItem.remove();
            this.addConnectItem(connectTreeItem);
        }
    }

    @Override
    public void loadChild() {
        this.clearChild();
        // 初始化分组
        List<ShellGroup> groups = this.groupStore.load();
        // List<SSHGroupTreeItem> groupItems = this.getGroupItems();
        if (CollectionUtil.isNotEmpty(groups)) {
            List<TreeItem<?>> list = new ArrayList<>();
            // f1:
            for (ShellGroup group : groups) {
                // for (SSHGroupTreeItem groupItem : groupItems) {
                //     if (StringUtil.equals(groupItem.getGid(), group.getGid())) {
                //         continue f1;
                //     }
                // }
                list.add(new ShellGroupTreeItem(group, this.getTreeView()));
            }
            this.addChild(list);
        }
        // 初始化连接
        List<ShellConnect> connects = this.connectStore.load();
        List<ShellGroupTreeItem> groupItems = this.getGroupItems();
        if (CollectionUtil.isNotEmpty(connects)) {
            List<ShellConnectTreeItem> connectItems = this.getConnectItems();
            // List<SSHConnect> list = new ArrayList<>();
            f1:
            for (ShellConnect connect : connects) {
                for (ShellConnectTreeItem connectItem : connectItems) {
                    if (StringUtil.equals(connectItem.getId(), connect.getId())) {
                        continue f1;
                    }
                }
                Optional<ShellGroupTreeItem> optional = groupItems.parallelStream().filter(g -> StringUtil.equals(g.getGid(), connect.getGroupId())).findAny();
                if (optional.isPresent()) {
                    optional.get().addConnect(connect);
                } else {
                    this.addConnect(connect);
                    // list.add(connect);
                }
            }
            // this.addConnects(list);
        }
    }
}
