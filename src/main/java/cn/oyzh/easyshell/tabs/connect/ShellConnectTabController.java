package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.shell.ShellClient;
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
 * shell命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellConnectTabController extends ParentTabController {

    /**
     * shell客户端
     */
    private ShellClient client;

    public ShellClient getClient() {
        return client;
    }

    public void setClient(ShellClient client) {
        this.client = client;
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
     * docker
     */
    @FXML
    private ShellDockerTabController dockerTabController;

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
     * 设置shell客户端
     *
     * @param treeItem shell客户端
     */
    public void init(ShellConnectTreeItem treeItem) {
        this.treeItem = treeItem;
        this.client = new ShellClient(treeItem.value());
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
                this.termTabController.init();
                this.monitorTabController.setClient(this.client);
                this.configTabController.setClient(this.client);
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
        return List.of(this.termTabController, this.sftpTabController, this.dockerTabController,
                this.monitorTabController, this.configTabController);
    }
}
