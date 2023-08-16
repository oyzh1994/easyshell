package cn.oyzh.easyssh.fx;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.easyfx.controls.FlexImageView;
import cn.oyzh.easyfx.event.EventReceiver;
import cn.oyzh.easyfx.event.EventUtil;
import cn.oyzh.easyfx.information.FXAlertUtil;
import cn.oyzh.easyfx.information.FXDialogUtil;
import cn.oyzh.easyfx.information.FXToastUtil;
import cn.oyzh.easyfx.menu.FXMenuItem;
import cn.oyzh.easyfx.svg.SVGGlyph;
import cn.oyzh.easyfx.util.FXFileChooser;
import cn.oyzh.easyfx.util.IconUtil;
import cn.oyzh.easyfx.view.FXViewUtil;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.easyssh.controller.info.SSHInfoAddController;
import cn.oyzh.easyssh.domain.SSHGroup;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.dto.SSHInfoExport;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.store.SSHGroupStore;
import cn.oyzh.easyssh.store.SSHInfoStore;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.FileChooser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ssh树根节点
 *
 * @author oyzh
 * @since 2023/06/16
 */
@Slf4j
public class SSHRootTreeItem extends BaseTreeItem implements ConnectManager {

    /**
     * ssh信息储存
     */
    private final SSHInfoStore infoStore = SSHInfoStore.INSTANCE;

    /**
     * ssh分组储存
     */
    private final SSHGroupStore groupStore = SSHGroupStore.INSTANCE;

    public SSHRootTreeItem(@NonNull SSHTreeView treeView) {
        this.treeView(treeView);
        this.itemValue("SSH连接列表");
        // 注册事件处理
        EventUtil.register(this);
        // 初始化子节点
        this.initChildes();
        // 监听键变化
        this.getChildren().addListener((ListChangeListener<? super BaseTreeItem>) c -> {
            this.treeView().fireChildChanged();
            this.treeView().flushLocal();
        });
    }

    /**
     * 初始化子节点
     */
    private void initChildes() {
        List<SSHGroup> groups = this.groupStore.load();
        List<SSHInfo> sshInfos = this.infoStore.load();
        this.addGroups(groups);
        this.addConnects(sshInfos);
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        MenuItem addConnect = FXMenuItem.newItem("添加连接", new SVGGlyph("/font/add.svg", "12"), "添加ssh连接", this::addConnect);
        MenuItem addGroup = FXMenuItem.newItem("添加分组", new SVGGlyph("/font/addGroup.svg", "12"), "添加分组", this::addGroup);
        MenuItem exportConnect = FXMenuItem.newItem("导出连接", new SVGGlyph("/font/export.svg", "12"), "导出ssh连接", this::exportConnect);
        MenuItem importConnect = FXMenuItem.newItem("导入连接", new SVGGlyph("/font/Import.svg", "12"), "选择文件，导入ssh连接，也可拖拽文件到窗口进行导入", this::importConnect);

        exportConnect.setDisable(this.isChildEmpty());

        items.add(addConnect);
        items.add(addGroup);
        items.add(exportConnect);
        items.add(importConnect);
        return items;
    }

    /**
     * 导出连接
     */
    private void exportConnect() {
        List<SSHInfo> sshInfos = this.infoStore.load();
        if (sshInfos.isEmpty()) {
            FXAlertUtil.warn("连接为空！");
            return;
        }
        SSHInfoExport export = SSHInfoExport.fromConnects(sshInfos);
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("JSON files", "*.json");
        File file = FXFileChooser.save("保存SSH连接列表", "SSH连接列表.json", new FileChooser.ExtensionFilter[]{extensionFilter});
        if (file != null) {
            try {
                FileUtil.writeUtf8String(export.toJSONString(), file);
                FXToastUtil.ok("保存连接成功！");
            } catch (Exception ex) {
                ex.printStackTrace();
                FXAlertUtil.warn("保存连接失败！");
            }
        }
    }

    /**
     * 拖拽文件
     *
     * @param event 事件
     */
    public void dragFile(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        List<File> files = dragboard.getFiles();
        if (CollUtil.isEmpty(files)) {
            return;
        }
        if (files.size() != 1) {
            FXAlertUtil.warn("仅支持单个文件！");
            return;
        }
        File file = files.get(0);
        // 解析文件
        this.parseConnect(file);
    }

    /**
     * 导入连接
     */
    private void importConnect() {
        FileChooser.ExtensionFilter filter1 = new FileChooser.ExtensionFilter("JSON files", "*.json");
        FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("All", "*.*");
        File file = FXFileChooser.choose("选择ssh连接列表", new FileChooser.ExtensionFilter[]{filter1, filter2});
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
            FXAlertUtil.warn("文件不存在！");
            return;
        }
        if (file.isDirectory()) {
            FXAlertUtil.warn("不支持文件夹！");
            return;
        }
        if (!FileNameUtil.isType(file.getName(), "json")) {
            FXAlertUtil.warn("仅支持json文件！");
            return;
        }
        if (file.length() == 0) {
            FXAlertUtil.warn("文件内容为空！");
            return;
        }
        try {
            String text = FileUtil.readUtf8String(file);
            SSHInfoExport export = SSHInfoExport.fromJSON(text);
            List<SSHInfo> sshInfos = export.getConnects();
            if (CollUtil.isNotEmpty(sshInfos)) {
                for (SSHInfo info : sshInfos) {
                    if (this.infoStore.exist(info)) {
                        FXAlertUtil.warn("连接[" + info.getName() + "]已存在");
                    } else if (this.infoStore.add(info)) {
                        this.addConnect(info);
                    } else {
                        FXAlertUtil.warn("连接[" + info.getName() + "]导入失败");
                    }
                }
                FXToastUtil.ok("导入连接成功！");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            FXAlertUtil.warn("解析连接失败！");
        }
    }

    /**
     * 添加连接
     */
    @EventReceiver(SSHEvents.SSH_ADD_CONNECT)
    private void addConnect() {
        FXViewUtil.showView(SSHInfoAddController.class, this.window());
    }

    /**
     * 关闭连接
     */
    @EventReceiver(value = SSHEvents.SSH_CLOSE_CONNECT, verbose = true, async = true)
    private void closeConnect(SSHInfo info) {
        for (SSHConnectTreeItem connectTreeItem : this.getConnectItems()) {
            if (connectTreeItem.value() == info) {
                connectTreeItem._disConnect();
            }
        }
    }

    /**
     * 添加分组
     */
    @EventReceiver(SSHEvents.SSH_ADD_GROUP)
    private void addGroup() {
        String groupName = FXDialogUtil.prompt("请输入分组名称");

        // 名称为空，则忽略
        if (StrUtil.isBlank(groupName)) {
            return;
        }

        SSHGroup group = new SSHGroup();
        group.setName(groupName);
        if (this.groupStore.exist(group)) {
            FXAlertUtil.warn("此分组已存在！");
            return;
        }
        group = this.groupStore.add(groupName);
        if (group != null) {
            this.addChild(new SSHGroupTreeItem(group, this.treeView()));
        } else {
            FXAlertUtil.warn("添加分组失败！");
        }
    }

    @Override
    public ObservableList<BaseTreeItem> getChildren() {
        return super.getChildren();
    }

    /**
     * 添加多个分组
     *
     * @param sshGroups ssh分组列表
     */
    private void addGroups(List<SSHGroup> sshGroups) {
        if (CollUtil.isNotEmpty(sshGroups)) {
            List<SSHGroupTreeItem> list = new ArrayList<>();
            for (SSHGroup group : sshGroups) {
                SSHGroupTreeItem groupTreeItem = new SSHGroupTreeItem(group, this.treeView());
                list.add(groupTreeItem);
            }
            this.getChildren().addAll(list);
        }
    }

    /**
     * 获取分组键
     *
     * @param groupId 分组id
     */
    private SSHGroupTreeItem getGroupItem(String groupId) {
        if (StrUtil.isNotBlank(groupId)) {
            List<SSHGroupTreeItem> items = this.getGroupItems();
            Optional<SSHGroupTreeItem> groupTreeItem = items.parallelStream().filter(g -> Objects.equals(g.value().getGid(), groupId)).findAny();
            return groupTreeItem.orElse(null);
        }
        return null;
    }

    /**
     * 获取分组键
     *
     * @return 分组键
     */
    private List<SSHGroupTreeItem> getGroupItems() {
        List<SSHGroupTreeItem> items = new ArrayList<>(this.getChildren().size());
        for (BaseTreeItem item : this.getChildren()) {
            if (item instanceof SSHGroupTreeItem groupTreeItem) {
                items.add(groupTreeItem);
            }
        }
        return items;
    }

    @Override
    public boolean flushGraphic() {
        if (this.itemValue().graphic() == null) {
            this.itemValue().graphic(new FlexImageView(IconUtil.getIcon(SSHConst.ICON_PATH), 16));
            return true;
        }
        return false;
    }

    /**
     * 连接新增事件
     *
     * @param info 连接信息
     */
    @EventReceiver(SSHEvents.SSH_INFO_ADD)
    private void onConnectAdd(SSHInfo info) {
        this.addConnect(info);
    }

    /**
     * 连接变更事件
     *
     * @param info 连接信息
     */
    @EventReceiver(SSHEvents.SSH_INFO_UPDATED)
    private void onConnectUpdate(SSHInfo info) {
        ObservableList<BaseTreeItem> items = this.getChildren();
        f1:
        for (BaseTreeItem item : items) {
            if (item instanceof SSHConnectTreeItem connectTreeItem) {
                if (connectTreeItem.value() == info) {
                    connectTreeItem.value(info);
                    break;
                }
            } else if (item instanceof SSHGroupTreeItem groupTreeItem) {
                for (SSHConnectTreeItem connectTreeItem : groupTreeItem.getChildren()) {
                    if (connectTreeItem.value() == info) {
                        connectTreeItem.value(info);
                        break f1;
                    }
                }
            }
        }
    }

    @Override
    public void addConnect(@NonNull SSHInfo sshInfo) {
        SSHGroupTreeItem groupTreeItem = this.getGroupItem(sshInfo.getGroupId());
        if (groupTreeItem == null) {
            super.addChild(new SSHConnectTreeItem(sshInfo, this.treeView()));
        } else {
            groupTreeItem.addConnect(sshInfo);
        }
    }

    @Override
    public void addConnectItem(@NonNull SSHConnectTreeItem item) {
        if (this.getChildren().contains(item)) {
            return;
        }
        if (item.value().getGroupId() != null) {
            item.value().setGroupId(null);
            this.infoStore.update(item.value());
        }
        super.addChild(item);
        if (!this.isExpanded()) {
            this.extend();
        }
    }

    @Override
    public void addConnectItems(@NonNull List<SSHConnectTreeItem> items) {
        if (CollUtil.isNotEmpty(items)) {
            this.getChildren().addAll(items);
        }
    }

    @Override
    public boolean delConnectItem(@NonNull SSHConnectTreeItem item) {
        // 删除连接
        if (this.infoStore.delete(item.value())) {
            this.removeChild(item);
            return true;
        }
        return false;
    }

    @Override
    public List<SSHConnectTreeItem> getConnectItems() {
        List<SSHConnectTreeItem> items = new ArrayList<>(this.getChildren().size());
        for (BaseTreeItem child : this.getChildren()) {
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
        List<SSHConnectTreeItem> items = new ArrayList<>(this.getChildren().size());
        for (BaseTreeItem item : this.getChildren()) {
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
}
