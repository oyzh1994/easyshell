package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.easyshell.util.mysql.ShellMysqlViewFactory;
import cn.oyzh.easyshell.util.redis.ShellRedisViewFactory;
import cn.oyzh.easyshell.util.zk.ShellZKViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.MoveSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * shell连接节点
 *
 * @author oyzh
 * @since 2025/03/29
 */
public class ShellConnectTreeItem extends RichTreeItem<ShellConnectTreeItemValue> {

    /**
     * shell信息
     */
    private ShellConnect value;

    public ShellConnect value() {
        return value;
    }

    /**
     * shell连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    public ShellConnectTreeItem(ShellConnect value, RichTreeView treeView) {
        super(treeView);
        this.value(value);
    }

    @Override
    public ShellConnectTreeView getTreeView() {
        return (ShellConnectTreeView) super.getTreeView();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem openConnect = MenuItemHelper.openConnect(this::onPrimaryDoubleClick);
        items.add(openConnect);
        if (this.isSSHType()) {
            FXMenuItem openSFTP = MenuItemHelper.openSFTP(this::openSFTP);
            items.add(openSFTP);
        }
        FXMenuItem editConnect = MenuItemHelper.editConnect(this::editConnect);
        items.add(editConnect);
        FXMenuItem renameConnect = MenuItemHelper.renameConnect(this::rename);
        items.add(renameConnect);
        FXMenuItem copyInfo = MenuItemHelper.copyInfo(this::copyInfo);
        items.add(copyInfo);
        FXMenuItem cloneConnect = MenuItemHelper.cloneConnect(this::cloneConnect);
        items.add(cloneConnect);
        FXMenuItem deleteConnect = MenuItemHelper.deleteConnect(this::delete);
        items.add(deleteConnect);
        items.add(MenuItemHelper.separator());
        if (this.value.isFileType()) {
            FXMenuItem transportFile = MenuItemHelper.transportFile(() -> {
                ShellViewFactory.fileTransport(this.value);
            });
            items.add(transportFile);
        } else if (this.isRedisType()) {
            FXMenuItem transportData = MenuItemHelper.transportData(() -> {
                ShellRedisViewFactory.redisTransportData(this.value, null);
            });
            items.add(transportData);
            FXMenuItem importData = MenuItemHelper.importData(() -> {
                ShellRedisViewFactory.redisImportData(this.value);
            });
            items.add(importData);
            FXMenuItem exportData = MenuItemHelper.exportData(() -> {
                ShellRedisViewFactory.redisExportData(this.value, null);
            });
            items.add(exportData);
        } else if (this.isZKType()) {
            FXMenuItem transportData = MenuItemHelper.transportData(() -> {
                ShellZKViewFactory.zkTransportData(this.value);
            });
            items.add(transportData);
            FXMenuItem importData = MenuItemHelper.importData(() -> {
                ShellZKViewFactory.zkImportData(this.value);
            });
            items.add(importData);
            FXMenuItem exportData = MenuItemHelper.exportData(() -> {
                ShellZKViewFactory.zkExportData(this.value, null);
            });
            items.add(exportData);
        } else if (this.isMysqlType()) {
            FXMenuItem transportData = MenuItemHelper.transportData(() -> {
                ShellMysqlViewFactory.transportData(this.value, null);
            });
            items.add(transportData);
        } else if (this.isMongoType()) {
            FXMenuItem transportData = MenuItemHelper.transportData(() -> {
                ShellMongoViewFactory.transportData(this.value, null);
            });
            items.add(transportData);
        }
        // 处理分组移动
        List<ShellConnectGroupTreeItem> groupItems = this.getTreeView().getGroupItems();
        Menu moveTo = MenuItemHelper.menu(I18nHelper.moveTo(), new MoveSVGGlyph("12"));
        if (CollectionUtil.isNotEmpty(groupItems)) {
            // for (ShellConnectGroupTreeItem item : groupItems) {
            //     MenuItem menuItem = MenuItemHelper.menuItem(item.getGroupName(), () -> this.moveTo(item));
            //     if (StringUtil.equals(this.getGroupId(), item.getGroupId())) {
            //         menuItem.setDisable(true);
            //     }
            //     moveTo.getItems().add(menuItem);
            // }
            ShellConnectManager manager = this.getTreeView().root();
            this.buildMoveToMenuItems(moveTo, manager);
            MenuItem rootItem = MenuItemHelper.menuItem(I18nHelper.hostList(), null, () -> this.moveTo(manager));
            moveTo.getItems().add(rootItem);
        } else {
            moveTo.setDisable(true);
        }
        items.add(moveTo);
        items.add(MenuItemHelper.separator());
        items.addAll(this.getTreeView().getMenuItems());
        return items;
    }

    /**
     * 构建移动到菜单
     *
     * @param moveTo  移动到图标
     * @param manager 连接管理器
     */
    private void buildMoveToMenuItems(Menu moveTo, ShellConnectManager manager) {
        List<ShellConnectGroupTreeItem> groupItems = manager.getGroupItems();
        if (CollectionUtil.isEmpty(groupItems)) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        for (ShellConnectGroupTreeItem item : groupItems) {
            List<ShellConnectGroupTreeItem> list = item.getGroupItems();
            if (CollectionUtil.isEmpty(list)) {
                MenuItem subMoveTo = MenuItemHelper.menuItem(item.getGroupName(), null, () -> this.moveTo(item));
                if (StringUtil.equals(this.getGroupId(), item.getGroupId())) {
                    subMoveTo.setDisable(true);
                }
                items.add(subMoveTo);
            } else {
                Menu subMoveTo = MenuItemHelper.menu(item.getGroupName(), null, () -> this.moveTo(item));
                if (StringUtil.equals(this.getGroupId(), item.getGroupId())) {
                    subMoveTo.setDisable(true);
                }
                items.add(subMoveTo);
                this.buildMoveToMenuItems(subMoveTo, item);
            }
        }
        moveTo.getItems().setAll(items);
    }

    /**
     * 移动到分组
     *
     * @param manager 连接管理器
     */
    private void moveTo(ShellConnectManager manager) {
        this.remove();
        manager.addConnectItem(this);
    }

    public boolean isSSHType() {
        return value.isSSHType();
    }

    public boolean isSFTPType() {
        return value.isSFTPType();
    }

    public boolean isFTPType() {
        return value.isFTPType();
    }

    public boolean isLocalType() {
        return value.isLocalType();
    }

    public boolean isSerialType() {
        return value.isSerialType();
    }

    public boolean isTelnetType() {
        return value.isTelnetType();
    }

    public boolean isS3Type() {
        return value.isS3Type();
    }

    public boolean isSMBType() {
        return value.isSMBType();
    }

    public boolean isRDPType() {
        return value.isRDPType();
    }

    public boolean isVNCType() {
        return value.isVNCType();
    }

    public boolean isWebdavType() {
        return value.isWebdavType();
    }

    public boolean isRedisType() {
        return value.isRedisType();
    }

    public boolean isMysqlType() {
        return value.isMysqlType();
    }

    public boolean isMongoType() {
        return value.isMongoType();
    }

    public boolean isZKType() {
        return value.isZKType();
    }

    public boolean isRloginType() {
        return value.isRloginType();
    }

    @Override
    public void clearChild() {
        super.clearChild();
        this.setLoaded(false);
    }

    @Override
    public void loadChild() {
        ShellEventUtil.connectionOpened(this.value);
    }

    /**
     * 编辑连接
     */
    private void editConnect() {
        if (this.value.isSSHType()) {
            ShellViewFactory.updateSSHConnect(this.value);
        } else if (this.value.isLocalType()) {
            ShellViewFactory.updateLocalConnect(this.value);
        } else if (this.value.isTelnetType()) {
            ShellViewFactory.updateTelnetConnect(this.value);
        } else if (this.value.isSFTPType()) {
            ShellViewFactory.updateSFTPConnect(this.value);
        } else if (this.value.isFTPType()) {
            ShellViewFactory.updateFTPConnect(this.value);
        } else if (this.value.isS3Type()) {
            ShellViewFactory.updateS3Connect(this.value);
        } else if (this.value.isSerialType()) {
            ShellViewFactory.updateSerialConnect(this.value);
        } else if (this.value.isVNCType()) {
            ShellViewFactory.updateVNCConnect(this.value);
        } else if (this.value.isRloginType()) {
            ShellViewFactory.updateRLoginConnect(this.value);
        } else if (this.value.isSMBType()) {
            ShellViewFactory.updateSMBConnect(this.value);
        } else if (this.value.isRedisType()) {
            ShellViewFactory.updateRedisConnect(this.value);
        } else if (this.value.isZKType()) {
            ShellViewFactory.updateZKConnect(this.value);
        } else if (this.value.isRDPType()) {
            ShellViewFactory.updateRDPConnect(this.value);
        } else if (this.value.isWebdavType()) {
            ShellViewFactory.updateWebdavConnect(this.value);
        } else if (this.value.isMysqlType()) {
            ShellViewFactory.updateMysqlConnect(this.value);
        } else if (this.value.isMongoType()) {
            ShellViewFactory.updateMongoConnect(this.value);
        }
    }

    // /**
    //  * 传输文件
    //  */
    // private void transportFile() {
    //     ShellViewFactory.fileTransport(this.value);
    // }

    /**
     * 复制信息
     */
    private void copyInfo() {
        if (this.value.isLocalType()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.protocol()).append(":").append(this.value.getType()).append("\n");
        if (this.isS3Type()) {
            builder.append(I18nHelper.host()).append(":").append(this.value.getHost()).append("\n");
            builder.append(I18nHelper.userName()).append(":").append(this.value.getUser()).append("\n");
            builder.append(I18nHelper.password()).append(":").append(this.value.getPassword()).append("\n");
            builder.append(I18nHelper.region()).append(":").append(this.value.getRegion()).append("\n");
        } else if (this.isSMBType()) {
            builder.append(I18nHelper.host()).append(":").append(this.value.hostIp()).append("\n");
            builder.append(I18nHelper.port()).append(":").append(this.value.hostPort()).append("\n");
            if (StringUtil.isNotBlank(this.value.getUser())) {
                builder.append(I18nHelper.userName()).append(":").append(this.value.getUser()).append("\n");
            }
            if (StringUtil.isNotBlank(this.value.getPassword())) {
                builder.append(I18nHelper.password()).append(":").append(this.value.getPassword()).append("\n");
            }
            if (StringUtil.isNotBlank(this.value.getSmbShareName())) {
                builder.append(I18nHelper.shareName()).append(":").append(this.value.getSmbShareName()).append("\n");
            }
            if (StringUtil.isNotBlank(this.value.getDomain())) {
                builder.append(I18nHelper.domain()).append(":").append(this.value.getDomain()).append("\n");
            }
        } else if (this.isSerialType()) {
            builder.append(I18nHelper.portName()).append(":").append(this.value.getSerialPortName()).append("\n");
            builder.append(I18nHelper.baudRate()).append(":").append(this.value.getSerialBaudRate()).append("\n");
            builder.append(I18nHelper.numDataBits()).append(":").append(this.value.getSerialNumDataBits()).append("\n");
            builder.append(I18nHelper.parityBits()).append(":").append(this.value.getSerialParityBits()).append("\n");
            builder.append(I18nHelper.numStopBits()).append(":").append(this.value.getSerialNumStopBits()).append("\n");
            builder.append(I18nHelper.flowControl()).append(":").append(this.value.getSerialFlowControl()).append("\n");
        } else {
            builder.append(I18nHelper.host()).append(":").append(this.value.hostIp()).append("\n");
            builder.append(I18nHelper.port()).append(":").append(this.value.hostPort()).append("\n");
            if (StringUtil.isNotBlank(this.value.getUser())) {
                builder.append(I18nHelper.userName()).append(":").append(this.value.getUser()).append("\n");
            }
            if (StringUtil.isNotBlank(this.value.getPassword())) {
                builder.append(I18nHelper.password()).append(":").append(this.value.getPassword()).append("\n");
            }
        }
        String content = builder.substring(0, builder.lastIndexOf("\n"));
        if (!ClipboardUtil.copy(content)) {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 克隆连接
     */
    private void cloneConnect() {
        ShellConnect shellConnect = new ShellConnect();
        shellConnect.copy(this.value);
        shellConnect.setName(this.value.getName() + "-" + I18nHelper.clone1());
        if (this.connectStore.replace(shellConnect)) {
            this.connectManager().addConnect(shellConnect);
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    @Override
    public void delete() {
        if (MessageBox.confirm(I18nHelper.delete() + " [" + this.value().getName() + "]")) {
            // this.closeConnect(false);
            if (this.connectManager().delConnectItem(this)) {
                ShellEventUtil.connectDeleted(this.value);
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        }
    }

    @Override
    public void rename() {
        String connectName = MessageBox.prompt(I18nHelper.contentTip1(), this.value.getName());
        // 名称为null或者跟当前名称相同，则忽略
        if (connectName == null || Objects.equals(connectName, this.value.getName())) {
            return;
        }
        // 检查名称
        if (StringUtil.isBlank(connectName)) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
            return;
        }
        this.value.setName(connectName);
        // 修改名称
        if (this.connectStore.update(this.value)) {
            this.setValue(new ShellConnectTreeItemValue(this));
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 设置值
     *
     * @param value ssh信息
     */
    public void value(ShellConnect value) {
        this.value = value;
        //        this.client = new ShellBaseSSHClient(value);
        //        this.client.addStateListener((observable, o, n) -> this.refresh());
        super.setValue(new ShellConnectTreeItemValue(this));
    }

    //    /**
    //     * 是否已连接
    //     *
    //     * @return 结果
    //     */
    //    public boolean isConnected() {
    //        return this.client != null && this.client.isConnected();
    //    }
    //
    //    /**
    //     * 是否连接中
    //     *
    //     * @return 结果
    //     */
    //    public boolean isConnecting() {
    //        return this.client != null && this.client.isConnecting();
    //    }

    /**
     * 获取当前父节点
     *
     * @return 父节点
     */
    public ShellConnectManager connectManager() {
        Object object = this.getParent();
        if (object instanceof ShellConnectManager connectManager) {
            return connectManager;
        }
        return null;
    }

    @Override
    public boolean allowDrag() {
        return true;
    }

    @Override
    public void onPrimaryDoubleClick() {
        //        if (!this.isConnected() && !this.isConnecting()) {
        //            this.connect();
        ShellEventUtil.connectionOpened(this.value);
        //        } else {
        //            super.onPrimaryDoubleClick();
        //        }
    }

    /**
     * 打开sftp
     */
    private void openSFTP() {
        ShellConnect connect = new ShellConnect();
        connect.copy(this.value);
        connect.setType("sftp");
        ShellEventUtil.connectionOpened(connect);
    }

    public String connectName() {
        return this.value.getName();
    }

    public String getId() {
        return this.value.getId();
    }

    public String getGroupId() {
        return this.value.getGroupId();
    }
}
