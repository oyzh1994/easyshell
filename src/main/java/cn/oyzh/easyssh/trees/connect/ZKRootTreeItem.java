package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.controller.connect.SSHConnectAddController;
import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.domain.SSHGroup;
import cn.oyzh.easyssh.domain.SSHQuery;
import cn.oyzh.easyssh.dto.SSHConnectExport;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.store.SSHConnectStore;
import cn.oyzh.easyssh.store.SSHGroupStore;
import cn.oyzh.easyssh.trees.connect.SSHConnectManager;
import cn.oyzh.easyssh.trees.connect.SSHConnectTreeItem;
import cn.oyzh.easyssh.trees.connect.SSHConnectTreeView;
import cn.oyzh.easyssh.trees.connect.SSHGroupTreeItem;
import cn.oyzh.easyssh.trees.connect.SSHRootTreeItemValue;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.file.FileChooserHelper;
import cn.oyzh.fx.plus.file.FileExtensionFilter;
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
 * zk树根节点
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class SSHRootTreeItem extends RichTreeItem<SSHRootTreeItemValue> implements SSHConnectManager {

    /**
     * zk分组储存
     */
    private final SSHGroupStore groupStore = SSHGroupStore.INSTANCE;

    /**
     * zk连接储存
     */
    private final SSHConnectStore connectStore = SSHConnectStore.INSTANCE;

    public SSHRootTreeItem(@NonNull SSHConnectTreeView treeView) {
        super(treeView);
        this.setValue(new SSHRootTreeItemValue());
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
        List<SSHConnect> infos = this.connectStore.load();
        if (infos.isEmpty()) {
            MessageBox.warn(I18nHelper.connectionIsEmpty());
            return;
        }
        SSHConnectExport export = SSHConnectExport.fromConnects(infos);
        FileExtensionFilter extensionFilter = FileChooserHelper.jsonExtensionFilter();
        File file = FileChooserHelper.save(I18nHelper.saveConnection(), I18nResourceBundle.i18nString("base.zk", "base.connect", "base._json"), extensionFilter);
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
        FileExtensionFilter filter1 = FileChooserHelper.jsonExtensionFilter();
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
            SSHConnectExport export = SSHConnectExport.fromJSON(text);
            List<SSHConnect> connects = export.getConnects();
            if (CollectionUtil.isNotEmpty(connects)) {
                for (SSHConnect connect : connects) {
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
        StageManager.showStage(SSHConnectAddController.class, this.window());
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
        SSHGroup group = new SSHGroup();
        group.setName(groupName);
        if (this.groupStore.replace(group)) {
            this.addChild(new SSHGroupTreeItem(group, this.getTreeView()));
            SSHEventUtil.groupAdded(groupName);
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 获取分组树节点组件
     *
     * @param groupId 分组id
     */
    private SSHGroupTreeItem getGroupItem(String groupId) {
        if (StringUtil.isNotBlank(groupId)) {
            List<SSHGroupTreeItem> items = this.getGroupItems();
            Optional<SSHGroupTreeItem> groupTreeItem = items.parallelStream().filter(g -> Objects.equals(g.value().getGid(), groupId)).findAny();
            return groupTreeItem.orElse(null);
        }
        return null;
    }

    /**
     * 获取分组树节点组件
     *
     * @return 分组树节点组件
     */
    private List<SSHGroupTreeItem> getGroupItems() {
        List<SSHGroupTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof SSHGroupTreeItem groupTreeItem) {
                items.add(groupTreeItem);
            }
        }
        return items;
    }

    /**
     * 连接新增事件
     *
     * @param zkConnect zk连接
     */
    public void connectAdded(SSHConnect zkConnect) {
        this.addConnect(zkConnect);
    }

    /**
     * 连接变更事件
     *
     * @param zkConnect zk连接
     */
    public void connectUpdated(SSHConnect zkConnect) {
        f1:
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof SSHConnectTreeItem connectTreeItem) {
                if (connectTreeItem.value() == zkConnect) {
                    connectTreeItem.value(zkConnect);
                    break;
                }
            } else if (item instanceof SSHGroupTreeItem groupTreeItem) {
                for (SSHConnectTreeItem connectTreeItem : groupTreeItem.getConnectItems()) {
                    if (connectTreeItem.value() == zkConnect) {
                        connectTreeItem.value(zkConnect);
                        break f1;
                    }
                }
            }
        }
    }

    @Override
    public void addConnect(@NonNull SSHConnect info) {
        SSHGroupTreeItem groupItem = this.getGroupItem(info.getGroupId());
        if (groupItem == null) {
            super.addChild(new SSHConnectTreeItem(info, this.getTreeView()));
            this.expend();
        } else {
            groupItem.addConnect(info);
        }
    }

    @Override
    public void addConnectItem(@NonNull SSHConnectTreeItem item) {
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
    public void addConnectItems(@NonNull List<SSHConnectTreeItem> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.addChild((List) items);
            this.expend();
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
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof SSHConnectTreeItem connectTreeItem) {
                items.add(connectTreeItem);
            } else if (child instanceof SSHGroupTreeItem groupTreeItem) {
                items.addAll(groupTreeItem.getConnectItems());
            }
        }
        return items;
    }

    @Override
    public List<SSHConnectTreeItem> getConnectedItems() {
        List<SSHConnectTreeItem> items = new ArrayList<>(this.getChildrenSize());
        for (TreeItem<?> item : this.unfilteredChildren()) {
            if (item instanceof SSHConnectTreeItem connectTreeItem) {
                if (connectTreeItem.isConnected()) {
                    items.add(connectTreeItem);
                }
            } else if (item instanceof SSHGroupTreeItem groupTreeItem) {
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
        return item instanceof SSHConnectTreeItem;
    }

    @Override
    public void onDropNode(DragNodeItem item) {
        if (item instanceof SSHConnectTreeItem connectTreeItem) {
            connectTreeItem.remove();
            this.addConnectItem(connectTreeItem);
        }
    }

    @Override
    public void loadChild() {
        this.clearChild();
        // 初始化分组
        List<SSHGroup> groups = this.groupStore.load();
        // List<SSHGroupTreeItem> groupItems = this.getGroupItems();
        if (CollectionUtil.isNotEmpty(groups)) {
            List<TreeItem<?>> list = new ArrayList<>();
            // f1:
            for (SSHGroup group : groups) {
                // for (SSHGroupTreeItem groupItem : groupItems) {
                //     if (StringUtil.equals(groupItem.getGid(), group.getGid())) {
                //         continue f1;
                //     }
                // }
                list.add(new SSHGroupTreeItem(group, this.getTreeView()));
            }
            this.addChild(list);
        }
        // 初始化连接
        List<SSHConnect> connects = this.connectStore.load();
        List<SSHGroupTreeItem> groupItems = this.getGroupItems();
        if (CollectionUtil.isNotEmpty(connects)) {
            List<SSHConnectTreeItem> connectItems = this.getConnectItems();
            // List<SSHConnect> list = new ArrayList<>();
            f1:
            for (SSHConnect connect : connects) {
                for (SSHConnectTreeItem connectItem : connectItems) {
                    if (StringUtil.equals(connectItem.getId(), connect.getId())) {
                        continue f1;
                    }
                }
                Optional<SSHGroupTreeItem> optional = groupItems.parallelStream().filter(g -> StringUtil.equals(g.getGid(), connect.getGroupId())).findAny();
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

    public void queryAdded(SSHQuery query) {
        List<SSHConnectTreeItem> items = this.getConnectItems();
        if (items != null) {
            for (SSHConnectTreeItem item : items) {
                if (StringUtil.equals(item.getId(), query.getIid())) {
                    item.queriesItem().add(query);
                }
            }
        }
    }
}
