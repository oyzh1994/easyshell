package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigBashTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigEnvironmentTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigHostsTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigProfileTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigResolvTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigSshdTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigUserBashProfileTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigUserBashrcTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigUserProfileTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigUserZshrcTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigWinEnvironmentTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigWinHostsTabController;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHConfigWinSshdTabController;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

import java.util.List;

/**
 * 服务配置tab内容组件
 *
 * @author oyzh
 * @since 2025/03/16
 */
public class ShellSSHConfigTabController extends ParentTabController {

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
    private ShellSSHClient client;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellSSHClient client) {
        this.client = client;
        try {
            ShellSFTPClient sftpClient = this.client.getSftpClient();
            if (this.client.isWindows()) {
                // 移除linux专属配置
                this.tabPane.removeTabs("sshd","bash","hosts","resolv", "profile","userZshrc","userBashrc","userProfile", "environment","userBashProfile");
            } else {
                // 移除windows专属配置
                this.tabPane.removeTabs("winSshd", "winHosts", "winEnvironment");
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist("/etc/environment")) {
                    tabPane.removeTab("environment");
                }
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist("/etc/bash.bashrc")) {
                    tabPane.removeTab("bash");
                }
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist("/etc/hosts")) {
                    tabPane.removeTab("hosts");
                }
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist("/etc/profile")) {
                    tabPane.removeTab("profile");
                }
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist("/etc/resolv.conf")) {
                    tabPane.removeTab("resolv");
                }
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist("/etc/ssh/sshd_config")) {
                    tabPane.removeTab("sshd");
                }
                String userHome = this.client.getUserHome();
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist(userHome + ".profile")) {
                    tabPane.removeTab("userProfile");
                }
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist(userHome + ".zshrc")) {
                    tabPane.removeTab("userZshrc");
                }
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist(userHome + ".bashrc")) {
                    tabPane.removeTab("userBashrc");
                }
                // 如果配置文件不存在，则移除此配置
                if (!sftpClient.exist(userHome + ".bash_profile")) {
                    tabPane.removeTab("userBashProfile");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public ShellSSHClient getClient() {
        return client;
    }

    /**
     * 全局配置文件
     */
    @FXML
    private ShellSSHConfigProfileTabController profileController;

    /**
     * 全局环境配置文件
     */
    @FXML
    private ShellSSHConfigEnvironmentTabController environmentController;

    /**
     * 全局bash配置文件
     */
    @FXML
    private ShellSSHConfigBashTabController bashController;

    /**
     * 域名解析配置文件
     */
    @FXML
    private ShellSSHConfigHostsTabController hostsController;

    /**
     * 网络解析配置文件
     */
    @FXML
    private ShellSSHConfigResolvTabController resolvController;

    /**
     * ssh配置文件
     */
    @FXML
    private ShellSSHConfigSshdTabController sshdController;

    /**
     * 用户配置文件
     */
    @FXML
    private ShellSSHConfigUserProfileTabController userProfileController;

    /**
     * 用户bash配置文件
     */
    @FXML
    private ShellSSHConfigUserBashProfileTabController userBashProfileController;

    /**
     * 用户bashrc配置文件
     */
    @FXML
    private ShellSSHConfigUserBashrcTabController userBashrcController;

    /**
     * 用户zshrc配置文件
     */
    @FXML
    private ShellSSHConfigUserZshrcTabController userZshrcController;

    /**
     * 域名解析配置文件，windows
     */
    @FXML
    private ShellSSHConfigWinHostsTabController winHostsController;

    /**
     * ssh配置文件，windows
     */
    @FXML
    private ShellSSHConfigWinSshdTabController winSshdController;

    /**
     * 环境配置，windows
     */
    @FXML
    private ShellSSHConfigWinEnvironmentTabController winEnvironmentController;

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
                this.userZshrcController, this.resolvController, this.sshdController,
                this.hostsController, this.winHostsController, this.winEnvironmentController,
                this.winSshdController
        );
    }
}