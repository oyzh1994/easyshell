package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.shell.ShellConnState;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;

/**
 * shell连接tab内容组件
 *
 * @author oyzh
 * @since 2025/04/16
 */
public class ShellConnectTabController extends ParentTabController {

    /**
     * shell客户端
     */
    private ShellClient client;

    public ShellClient getClient() {
        return client;
    }

    public ShellConnectTreeItem getTreeItem() {
        return treeItem;
    }

    private ShellConnectTreeItem treeItem;

    /**
     * 终端
     */
    @FXML
    private ShellTermTabController termTabController;

    /**
     * 文件
     */
    @FXML
    private ShellSftpTabController sftpTabController;

    /**
     * 服务器信息
     */
    @FXML
    private ShellServerTabController serverTabController;

    /**
     * docker
     */
    @FXML
    private ShellDockerTabController dockerTabController;

    /**
     * 进程
     */
    @FXML
    private ShellProcessTabController processTabController;

    /**
     * 监控
     */
    @FXML
    private ShellMonitorTabController monitorTabController;

    /**
     * 配置
     */
    @FXML
    private ShellConfigTabController configTabController;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 设置shell客户端
     *
     * @param treeItem shell客户端
     */
    public void init(ShellConnectTreeItem treeItem) {
        this.treeItem = treeItem;
        this.client = new ShellClient(treeItem.value());
        // 监听连接状态
        this.client.addStateListener((observableValue, shellConnState, t1) -> {
            if (t1 == ShellConnState.INTERRUPT) {
                MessageBox.warn("[" + this.client.connectName() + "] " + I18nHelper.connectSuspended());
                this.closeTab();
            } else if (t1 == ShellConnState.CLOSED) {
                ShellEventUtil.connectionClosed(client);
            } else if (t1 == ShellConnState.CONNECTED) {
                ShellEventUtil.connectionConnected(client);
            }
        });
        StageManager.showMask(() -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                // 收起左侧
                if (this.setting.isHiddenLeftAfterConnected()) {
                    ShellEventUtil.layout1();
                }
                this.termTabController.init();
                this.serverTabController.setClient(this.client);
                this.configTabController.setClient(this.client);
                this.dockerTabController.setClient(this.client);
                this.monitorTabController.setClient(this.client);
                this.processTabController.setClient(this.client);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.getClient().close();
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }

    /**
     * shell信息
     *
     * @return 当前shell信息
     */
    protected ShellConnect shellConnect() {
        return this.treeItem.value();
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.serverTabController, this.termTabController, this.sftpTabController,
                this.dockerTabController, this.monitorTabController, this.configTabController,
                this.processTabController);
    }
}
