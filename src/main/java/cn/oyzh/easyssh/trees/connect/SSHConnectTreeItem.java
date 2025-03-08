package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.controller.connect.SSHUpdateConnectController;
import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.store.SSHConnectStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ssh连接节点
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class SSHConnectTreeItem extends RichTreeItem<SSHConnectTreeItemValue> {

    /**
     * ssh信息
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private SSHConnect value;

    /**
     * ssh客户端
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private SSHClient client;

    /**
     * 已取消操作标志位
     */
    private boolean canceled;

    /**
     * ssh连接存储
     */
    private final SSHConnectStore connectStore = SSHConnectStore.INSTANCE;

    public SSHConnectTreeItem(@NonNull SSHConnect value, @NonNull RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        this.value(value);
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(12);
        if (this.isConnecting()) {
            FXMenuItem cancelConnect = MenuItemHelper.cancelConnect("12", this::cancelConnect);
            items.add(cancelConnect);
        } else if (this.isConnected()) {
            FXMenuItem closeConnect = MenuItemHelper.closeConnect("12", this::closeConnect);
            FXMenuItem editConnect = MenuItemHelper.editConnect("12", this::editConnect);
            FXMenuItem cloneConnect = MenuItemHelper.cloneConnect("12", this::cloneConnect);

            items.add(closeConnect);
            items.add(editConnect);
            items.add(cloneConnect);
        } else {
            FXMenuItem connect = MenuItemHelper.startConnect("12", this::onPrimaryDoubleClick);
            FXMenuItem editConnect = MenuItemHelper.editConnect("12", this::editConnect);
            FXMenuItem renameConnect = MenuItemHelper.renameConnect("12", this::rename);
            FXMenuItem deleteConnect = MenuItemHelper.deleteConnect("12", this::delete);
            FXMenuItem cloneConnect = MenuItemHelper.repeatConnect("12", this::cloneConnect);

            items.add(connect);
            items.add(editConnect);
            items.add(renameConnect);
            items.add(cloneConnect);
            items.add(deleteConnect);
        }
        return items;
    }

    /**
     * 取消连接
     */
    public void cancelConnect() {
        this.canceled = true;
        ThreadUtil.startVirtual(() -> this.client.close());
    }

    /**
     * 连接
     */
    public void connect() {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> {
                    this.client.start();
                    if (!this.isConnected()) {
                        if (!this.canceled) {
                            MessageBox.warn("[" + this.value.getName() + "] " + I18nHelper.connectFail());
                        }
                        this.canceled = false;
                        this.closeConnect(false);
                    } else {
                        this.loadChild();
                        this.expend();
                    }
                })
                .onSuccess(this::flushLocal)
                .onError(MessageBox::exception)
                .build();
        // 执行连接
        this.startWaiting(task);
    }

    @Override
    public void clearChild() {
        super.clearChild();
        this.setLoaded(false);
    }

    @Override
    public void loadChild() {
        if (!this.isLoaded()) {
            this.setLoaded(true);
            SSHEventUtil.connectionOpened(this);
        }
    }

    /**
     * 关闭连接
     */
    public void closeConnect() {
        if (this.isConnected()) {
            this.closeConnect(true);
        }
    }

    /**
     * 关闭连接
     *
     * @param waiting 是否开启等待动画
     */
    public void closeConnect(boolean waiting) {
        Runnable func = () -> {
            this.client.close();
            this.clearChild();
        };
        if (waiting) {
            Task task = TaskBuilder.newBuilder()
                    .onStart(func::run)
                    .onFinish(this::refresh)
                    .onSuccess(SystemUtil::gcLater)
                    .onError(MessageBox::exception)
                    .build();
            this.startWaiting(task);
        } else {
            func.run();
        }
    }

    /**
     * 编辑连接
     */
    private void editConnect() {
        if (this.isConnected()) {
            if (!MessageBox.confirm(I18nHelper.closeAndContinue())) {
                return;
            }
            this.closeConnect();
        }
        StageAdapter fxView = StageManager.parseStage(SSHUpdateConnectController.class, this.window());
        fxView.setProp("sshConnect", this.value());
        fxView.display();
    }

    /**
     * 克隆连接
     */
    private void cloneConnect() {
        SSHConnect sshConnect = new SSHConnect();
        sshConnect.copy(this.value);
        sshConnect.setName(this.value.getName() + "-" + I18nHelper.clone1());
        if (this.connectStore.insert(sshConnect)) {
            this.connectManager().addConnect(sshConnect);
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    @Override
    public void delete() {
        if (MessageBox.confirm(I18nHelper.delete() + " [" + this.value().getName() + "]")) {
            this.closeConnect(false);
            if (this.connectManager().delConnectItem(this)) {
                SSHEventUtil.connectDeleted(this.value);
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
            this.setValue(new SSHConnectTreeItemValue(this));
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 设置值
     *
     * @param value ssh信息
     */
    public void value(@NonNull SSHConnect value) {
        this.value = value;
        this.client = new SSHClient(value);
        this.client.stateProperty().addListener((observable, o, n) -> {
            // 连接关闭
            if (n == null || !n.isConnected()) {
                // 清理子节点
                this.clearChild();
            }
        });
        super.setValue(new SSHConnectTreeItemValue(this));
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        return this.client != null && this.client.isConnecting();
    }

    /**
     * 获取当前父节点
     *
     * @return 父节点
     */
    public SSHConnectManager connectManager() {
        Object object = this.getParent();
        if (object instanceof SSHConnectManager connectManager) {
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
        if (!this.isConnected() && !this.isConnecting()) {
            this.connect();
//            SSHEventUtil.connectionOpened(this);
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    public String infoName() {
        return this.value.getName();
    }

    public String getId() {
        return this.value.getId();
    }
}
