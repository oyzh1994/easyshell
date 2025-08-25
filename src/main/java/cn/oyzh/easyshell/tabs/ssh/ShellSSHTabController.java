package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.tabs.ShellParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ssh-tab内容组件
 *
 * @author oyzh
 * @since 2025/04/16
 */
public class ShellSSHTabController extends ShellParentTabController {

    /**
     * shell客户端
     */
    private ShellSSHClient client;

    /**
     * 连接储存
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    public ShellSSHClient getClient() {
        return client;
    }

    private ShellConnect shellConnect;

    public ShellConnect shellConnect() {
        return shellConnect;
    }

    /**
     * 效率
     */
    @FXML
    private ShellSSHEffTabController effTabController;

    /**
     * 终端
     */
    @FXML
    private ShellSSHTermTabController termTabController;

    /**
     * 文件
     */
    @FXML
    private ShellSSHSFTPTabController sftpTabController;

    /**
     * 服务器信息
     */
    @FXML
    private ShellSSHServerTabController serverTabController;

    /**
     * docker
     */
    @FXML
    private ShellSSHDockerTabController dockerTabController;

    /**
     * 进程
     */
    @FXML
    private ShellSSHProcessTabController processTabController;

    /**
     * 监控
     */
    @FXML
    private ShellSSHMonitorTabController monitorTabController;

    /**
     * 配置
     */
    @FXML
    private ShellSSHConfigTabController configTabController;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 设置shell客户端
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.shellConnect = connect;
        this.client = new ShellSSHClient(connect);
        // 监听连接状态
        this.client.addStateListener((observableValue, shellConnState, t1) -> {
            if (t1 == ShellConnState.INTERRUPT) {
                MessageBox.warn("[" + this.client.connectName() + "] " + I18nHelper.connectSuspended());
                this.closeTab();
//            } else if (t1 == ShellConnState.CLOSED) {
//                ShellEventUtil.connectionClosed(client);
//            } else if (t1 == ShellConnState.CONNECTED) {
//                ShellEventUtil.connectionConnected(client);
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
                // if (this.setting.isHiddenLeftAfterConnected()) {
                //     ShellEventUtil.layout1();
                // }
                this.hideLeft();
                // 效率模式
                if (this.setting.isEfficiencyMode()) {
                    this.effTabController.init();
                } else {// 正常模式
                    this.termTabController.init();
                }
                this.serverTabController.setClient(this.client);
                this.configTabController.setClient(this.client);
                this.dockerTabController.setClient(this.client);
                this.monitorTabController.setClient(this.client);
                this.processTabController.setClient(this.client);
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client.close();
        // 保存设置
        this.connectStore.update(this.shellConnect());
        // // 展开左侧
        // if (this.setting.isHiddenLeftAfterConnected()) {
        //     ShellEventUtil.layout2();
        // }
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        List<RichTabController> controllers = new ArrayList<>();
        controllers.add(this.dockerTabController);
        controllers.add(this.serverTabController);
        controllers.add(this.processTabController);
        controllers.add(this.monitorTabController);
        controllers.add(this.configTabController);
        if (this.setting.isEfficiencyMode()) {
            controllers.add(this.effTabController);
        } else {
            controllers.add(this.termTabController);
            controllers.add(this.sftpTabController);
        }
        return controllers;
    }

    /**
     * 运行片段
     *
     * @param content 内容
     */
    public void runSnippet(String content) throws IOException {
        if (this.effTabController != null) {
            this.effTabController.runSnippet(content);
        } else if (this.termTabController != null) {
            this.termTabController.runSnippet(content);
        }
    }
}
