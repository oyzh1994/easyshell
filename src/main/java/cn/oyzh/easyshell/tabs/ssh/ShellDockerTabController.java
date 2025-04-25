package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.tabs.ssh.docker.ShellDockerContainerTabController;
import cn.oyzh.easyshell.tabs.ssh.docker.ShellDockerDaemonTabController;
import cn.oyzh.easyshell.tabs.ssh.docker.ShellDockerExtraTabController;
import cn.oyzh.easyshell.tabs.ssh.docker.ShellDockerImageTabController;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;

/**
 * docker tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellDockerTabController extends ParentTabController {

    /**
     * docker面板
     */
    @FXML
    private FXTab docker;

    /**
     * 容器
     */
    @FXML
    private ShellDockerContainerTabController containerController;

    /**
     * 镜像
     */
    @FXML
    private ShellDockerImageTabController imageController;

    /**
     * 配置文件
     */
    @FXML
    private ShellDockerDaemonTabController daemonController;

    /**
     * 额外
     */
    @FXML
    private ShellDockerExtraTabController extraController;

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
            if (StringUtil.containsAnyIgnoreCase(output, "daemon running", "daemon is not running")) {
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

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.getClient().close();
    }

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
