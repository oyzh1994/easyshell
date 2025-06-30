package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.tabs.ssh.docker.ShellSSHDockerContainerTabController;
import cn.oyzh.easyshell.tabs.ssh.docker.ShellSSHDockerDaemonTabController;
import cn.oyzh.easyshell.tabs.ssh.docker.ShellSSHDockerExtraTabController;
import cn.oyzh.easyshell.tabs.ssh.docker.ShellSSHDockerImageTabController;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

import java.util.List;

/**
 * ssh-docker tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellSSHDockerTabController extends ParentTabController {

    /**
     * docker面板
     */
    @FXML
    private FXTab docker;

    /**
     * 容器
     */
    @FXML
    private ShellSSHDockerContainerTabController containerController;

    /**
     * 镜像
     */
    @FXML
    private ShellSSHDockerImageTabController imageController;

    /**
     * 配置文件
     */
    @FXML
    private ShellSSHDockerDaemonTabController daemonController;

    /**
     * 额外
     */
    @FXML
    private ShellSSHDockerExtraTabController extraController;

    /**
     * shell客户端
     */
    private ShellSSHClient client;

    public ShellSSHClient getClient() {
        return client;
    }

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellSSHClient client) {
        this.client = client;
    }

    /**
     * 初始化标志位
     */
    private boolean initialized;

    /**
     * 执行初始化
     */
    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        try {
            ShellDockerExec exec = this.getClient().dockerExec();
            this.containerController.init(exec);
            String output = exec.docker_ps();
            if (ShellUtil.isCommandNotFound(output)) {
                MessageBox.info(ShellI18nHelper.connectTip5());
                return;
            }
            // 未运行
            if (StringUtil.containsAnyIgnoreCase(output, "daemon running", "daemon is not running", "system cannot find the file specified.")) {
                MessageBox.warn(ShellI18nHelper.connectTip6());
                return;
            }
            this.containerController.refreshContainer();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.docker.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.init();
            }
        });

    }

//    @Override
//    public void onTabClosed(Event event) {
//        super.onTabClosed(event);
//        this.getClient().close();
//    }

    public void loadContainer() {
        this.containerController.refreshContainer();
    }

    public void loadImage() {
        this.imageController.refreshImage();
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.containerController, this.imageController, this.daemonController, this.extraController);
    }
}
