package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.connect.ShellUpdateConnectController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * shell连接节点
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class ShellConnectTreeItem extends RichTreeItem<ShellConnectTreeItemValue> {

    /**
     * shell信息
     */
    private ShellConnect value;

    public ShellConnect value() {
        return value;
    }

//    /**
//     * shell客户端
//     */
//    @Getter
//    @Accessors(chain = true, fluent = true)
//    private SSHClient client;

    /**
     * 已取消操作标志位
     */
    private boolean canceled;

    /**
     * shell连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    public ShellConnectTreeItem(ShellConnect value, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        this.value(value);
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(12);
//        if (this.isConnecting()) {
////            FXMenuItem cancelConnect = MenuItemHelper.cancelConnect("12", this::cancelConnect);
////            items.add(cancelConnect);
//        } else if (this.isConnected()) {
////            FXMenuItem closeConnect = MenuItemHelper.closeConnect("12", this::closeConnect);
//            FXMenuItem editConnect = MenuItemHelper.editConnect("12", this::editConnect);
//            FXMenuItem cloneConnect = MenuItemHelper.cloneConnect("12", this::cloneConnect);
//
////            items.add(closeConnect);
//            items.add(editConnect);
//            items.add(cloneConnect);
//        } else {
//            FXMenuItem connect = MenuItemHelper.startConnect("12", this::onPrimaryDoubleClick);
        FXMenuItem editConnect = MenuItemHelper.editConnect("12", this::editConnect);
        FXMenuItem renameConnect = MenuItemHelper.renameConnect("12", this::rename);
        FXMenuItem deleteConnect = MenuItemHelper.deleteConnect("12", this::delete);
        FXMenuItem cloneConnect = MenuItemHelper.cloneConnect("12", this::cloneConnect);

//            items.add(connect);
        items.add(editConnect);
        items.add(renameConnect);
        items.add(cloneConnect);
        items.add(deleteConnect);
//        }
        return items;
    }

//    /**
//     * 取消连接
//     */
//    public void cancelConnect() {
//        this.canceled = true;
//        ThreadUtil.startVirtual(() -> this.client.close());
//    }

//    /**
//     * 连接
//     */
//    public void connect() {
//        if (this.isConnected() || this.isConnecting()) {
//            return;
//        }
//        Task task = TaskBuilder.newBuilder()
//                .onStart(() -> {
//                    this.client.start();
//                    if (!this.isConnected()) {
//                        if (!this.canceled) {
//                            MessageBox.warn("[" + this.value.getName() + "] " + I18nHelper.connectFail());
//                        }
//                        this.canceled = false;
//                        this.closeConnect(false);
//                    } else {
//                        this.loadChild();
//                    }
//                })
//                .onError(MessageBox::exception)
//                .build();
//        // 执行连接
//        this.startWaiting(task);
//    }

    @Override
    public void clearChild() {
        super.clearChild();
        this.setLoaded(false);
    }

    @Override
    public void loadChild() {
        ShellEventUtil.connectionOpened(this);
    }

    /**
     * 关闭连接
     */
    public void closeConnect() {
//        if (this.isConnected()) {
//            this.closeConnect(true);
//        }
    }

    /**
     * 关闭连接
     *
     * @param waiting 是否开启等待动画
     */
    public void closeConnect(boolean waiting) {
//        Runnable func = () -> this.client.close();
//        if (waiting) {
//            Task task = TaskBuilder.newBuilder()
//                    .onStart(func::run)
//                    .onFinish(this::refresh)
//                    .onSuccess(SystemUtil::gcLater)
//                    .onError(MessageBox::exception)
//                    .build();
//            this.startWaiting(task);
//        } else {
//            func.run();
//        }
    }

    /**
     * 编辑连接
     */
    private void editConnect() {
        if (!MessageBox.confirm(I18nHelper.closeAndContinue())) {
            return;
        }
        // 关闭所有连接
        ShellEventUtil.connectEdit(this.value);
        StageAdapter fxView = StageManager.parseStage(ShellUpdateConnectController.class, this.window());
        fxView.setProp("shellConnect", this.value());
        fxView.display();
    }

    /**
     * 克隆连接
     */
    private void cloneConnect() {
        ShellConnect shellConnect = new ShellConnect();
        shellConnect.copy(this.value);
        shellConnect.setName(this.value.getName() + "-" + I18nHelper.clone1());
        if (this.connectStore.insert(shellConnect)) {
            this.connectManager().addConnect(shellConnect);
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    @Override
    public void delete() {
        if (MessageBox.confirm(I18nHelper.delete() + " [" + this.value().getName() + "]")) {
            this.closeConnect(false);
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
//        this.client = new SSHClient(value);
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
        ShellEventUtil.connectionOpened(this);
//        } else {
//            super.onPrimaryDoubleClick();
//        }
    }

    public String infoName() {
        return this.value.getName();
    }

    public String getId() {
        return this.value.getId();
    }
}
