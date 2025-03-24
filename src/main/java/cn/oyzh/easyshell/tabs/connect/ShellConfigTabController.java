package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.server.ServerExec;
import cn.oyzh.easyshell.server.ServerMonitor;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.config.ShellBashTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellEnvironmentTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellProfileTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellResolvTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellSshdTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellUserBashProfileTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellUserBashrcTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellUserProfileTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellUserZshrcTabController;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

import java.util.List;

/**
 * 服务器配置tab内容组件
 *
 * @author oyzh
 * @since 2025/03/16
 */
public class ShellConfigTabController extends ParentTabController {

    /**
     * tab
     */
    @FXML
    private FXTab root;

    /**
     * FXTabPane
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * shell客户端
     */
    private ShellClient client;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellClient client) {
        this.client = client;
        try {
            ShellSftp sftp = this.client.openSftp();
            // 如果配置文件不存在，则移除此配置
            if (!sftp.exist("/etc/environment")) {
                tabPane.removeTab("environment");
            }
            // 如果配置文件不存在，则移除此配置
            if (!sftp.exist("/etc/bash.bashrc")) {
                tabPane.removeTab("bash");
            }
            // 如果配置文件不存在，则移除此配置
            if (!sftp.exist("/etc/ssh/sshd_config")) {
                tabPane.removeTab("sshd");
            }
            String userBase = this.client.getUserBase();
            // 如果配置文件不存在，则移除此配置
            if (!sftp.exist(userBase + ".profile")) {
                tabPane.removeTab("userProfile");
            }
            // 如果配置文件不存在，则移除此配置
            if (!sftp.exist(userBase + ".zshrc")) {
                tabPane.removeTab("userZshrc");
            }
            // 如果配置文件不存在，则移除此配置
            if (!sftp.exist(userBase + ".bash_profile")) {
                tabPane.removeTab("userBashProfile");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public ShellClient getClient() {
        return client;
    }

    /**
     * 全局配置文件
     */
    @FXML
    private ShellProfileTabController profileController;

    /**
     * 全局环境配置文件
     */
    @FXML
    private ShellEnvironmentTabController environmentController;

    /**
     * 全局bash配置文件
     */
    @FXML
    private ShellBashTabController bashController;

    /**
     * 网络解析配置文件
     */
    @FXML
    private ShellResolvTabController resolvController;

    /**
     * ssh配置文件
     */
    @FXML
    private ShellSshdTabController sshdController;

    /**
     * 用户配置文件
     */
    @FXML
    private ShellUserProfileTabController userProfileController;

    /**
     * 用户bash配置文件
     */
    @FXML
    private ShellUserBashProfileTabController userBashProfileController;

    /**
     * 用户bashrc配置文件
     */
    @FXML
    private ShellUserBashrcTabController userBashrcController;

    /**
     * 用户zshrc配置文件
     */
    @FXML
    private ShellUserZshrcTabController userZshrcController;

    /**
     * 初始化标志位
     */
    private boolean initialized = false;

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.initialized) {
                this.initialized = true;
                this.profileController.refresh();
            }
        });
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.profileController, this.userProfileController, this.environmentController,
                this.bashController, this.userBashProfileController, this.userBashrcController,
                this.userZshrcController, this.resolvController, this.sshdController
        );
    }
}