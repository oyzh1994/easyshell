package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.sftp2.ShellSFTPClient;
import cn.oyzh.easyshell.tabs.ssh.config.ShellSSHBaseConfigTabController;
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
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ssh-服务配置tab内容组件
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
        // try {
        //     ShellSFTPClient sftpClient = this.client.sftpClient();
        //     if (this.client.isWindows()) {
        //         // 移除linux专属配置
        //         this.tabPane.removeTabs("sshd", "bash", "hosts", "resolv", "profile", "userZshrc", "userBashrc", "userProfile", "environment", "userBashProfile");
        //     } else {
        //         // 移除windows专属配置
        //         this.tabPane.removeTabs("winSshd", "winHosts", "winEnvironment");
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist("/etc/environment")) {
        //             tabPane.removeTab("environment");
        //         }
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist("/etc/bash.bashrc")) {
        //             tabPane.removeTab("bash");
        //         }
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist("/etc/hosts")) {
        //             tabPane.removeTab("hosts");
        //         }
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist("/etc/profile")) {
        //             tabPane.removeTab("profile");
        //         }
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist("/etc/resolv.conf")) {
        //             tabPane.removeTab("resolv");
        //         }
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist("/etc/ssh/sshd_config")) {
        //             tabPane.removeTab("sshd");
        //         }
        //         String userHome = this.client.getUserHome();
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist(userHome + ".profile")) {
        //             tabPane.removeTab("userProfile");
        //         }
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist(userHome + ".zshrc")) {
        //             tabPane.removeTab("userZshrc");
        //         }
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist(userHome + ".bashrc")) {
        //             tabPane.removeTab("userBashrc");
        //         }
        //         // 如果配置文件不存在，则移除此配置
        //         if (!sftpClient.exist(userHome + ".bash_profile")) {
        //             tabPane.removeTab("userBashProfile");
        //         }
        //     }
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     MessageBox.exception(ex);
        // }
    }

    public ShellSSHClient getClient() {
        return client;
    }

    // /**
    //  * 全局配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigProfileTabController profileController;
    //
    // /**
    //  * 全局环境配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigEnvironmentTabController environmentController;
    //
    // /**
    //  * 全局bash配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigBashTabController bashController;
    //
    // /**
    //  * 域名解析配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigHostsTabController hostsController;
    //
    // /**
    //  * 网络解析配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigResolvTabController resolvController;
    //
    // /**
    //  * ssh配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigSshdTabController sshdController;
    //
    // /**
    //  * 用户配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigUserProfileTabController userProfileController;
    //
    // /**
    //  * 用户bash配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigUserBashProfileTabController userBashProfileController;
    //
    // /**
    //  * 用户bashrc配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigUserBashrcTabController userBashrcController;
    //
    // /**
    //  * 用户zshrc配置文件
    //  */
    // // @FXML
    // private ShellSSHConfigUserZshrcTabController userZshrcController;
    //
    // /**
    //  * 域名解析配置文件，windows
    //  */
    // // @FXML
    // private ShellSSHConfigWinHostsTabController winHostsController;
    //
    // /**
    //  * ssh配置文件，windows
    //  */
    // // @FXML
    // private ShellSSHConfigWinSshdTabController winSshdController;
    //
    // /**
    //  * 环境配置，windows
    //  */
    // // @FXML
    // private ShellSSHConfigWinEnvironmentTabController winEnvironmentController;

    // /**
    //  * 初始化标志位
    //  */
    // private boolean initialized = false;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && this.tabPane.isChildEmpty()) {
                this.loadTabs();
            }
        });
    }

    /**
     * 子控件列表
     */
    private final List<RichTabController> subControllers = new ArrayList<>();

    @Override
    public List<? extends RichTabController> getSubControllers() {
        // return List.of(this.profileController, this.userProfileController, this.environmentController,
        //         this.bashController, this.userBashProfileController, this.userBashrcController,
        //         this.userZshrcController, this.resolvController, this.sshdController,
        //         this.hostsController, this.winHostsController, this.winEnvironmentController,
        //         this.winSshdController
        // );
        return this.subControllers;
    }

    /**
     * 加载tab
     */
    private void loadTabs() {
        try {
            if (this.client.isWindows()) {
                FXMLResult result1 = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigWinHostsTab.fxml");
                this.tabPane.addTab(result1.node());
                FXMLResult result2 = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigWinSshdTab.fxml");
                this.tabPane.addTab(result2.node());
                FXMLResult result3 = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigWinEnvironmentTab.fxml");
                this.initSubTab(result1);
                this.initSubTab(result2);
                this.initSubTab(result3);
            } else {
                ShellSFTPClient sftpClient = this.client.sftpClient();
                if (sftpClient.exist("/etc/profile")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigProfileTab.fxml");
                    this.initSubTab(result);
                }
                if (sftpClient.exist("/etc/environment")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigEnvironmentTab.fxml");
                    this.initSubTab(result);
                }
                if (sftpClient.exist("/etc/bash.bashrc")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigBashTab.fxml");
                    this.initSubTab(result);
                }
                if (sftpClient.exist("/etc/hosts")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigHostsTab.fxml");
                    this.initSubTab(result);
                }
                if (sftpClient.exist("/etc/resolv.conf")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigResolvTab.fxml");
                    this.initSubTab(result);
                }
                if (sftpClient.exist("/etc/sshd_config")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigSshdTab.fxml");
                    this.initSubTab(result);
                }
                String userHome = this.client.getUserHome();
                if (sftpClient.exist(userHome + ".profile")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigUserProfileTab.fxml");
                    this.initSubTab(result);
                }
                if (sftpClient.exist(userHome + ".zshrc")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigUserZshrcTab.fxml");
                    this.initSubTab(result);
                }
                if (sftpClient.exist(userHome + ".bashrc")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigUserBashrcTab.fxml");
                    this.initSubTab(result);
                }
                if (sftpClient.exist(userHome + ".bash_profile")) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/config/shellSSHConfigUserBashProfileTab.fxml");
                    this.initSubTab(result);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化子面板
     *
     * @param result fxml解析结果
     */
    private void initSubTab(FXMLResult result) {
        if (result.controller() instanceof RichTabController controller) {
            if (controller instanceof SubTabController controller1) {
                controller1.parent(this);
            }
            if (this.tabPane.isChildEmpty() && controller instanceof ShellSSHBaseConfigTabController controller2) {
                controller2.refresh();
            }
            controller.onTabInit(result.node());
            this.subControllers.add(controller);
        }
        this.tabPane.addTab(result.node());
    }
}