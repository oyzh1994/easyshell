package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.docker.ShellDockerContainerTabController;
import cn.oyzh.easyshell.tabs.connect.docker.ShellDockerDaemonTabController;
import cn.oyzh.easyshell.tabs.connect.docker.ShellDockerExtraTabController;
import cn.oyzh.easyshell.tabs.connect.docker.ShellDockerImageTabController;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
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
     * zk客户端
     */
    private ShellClient client;

    public ShellClient getClient() {
        return client;
    }

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellClient client) {
        this.client = client;
    }

    private boolean initialized = false;

    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        try {
            DockerExec exec = this.getClient().dockerExec();
            String output = exec.docker_v();
            if (StringUtil.isBlank(output)) {
                MessageBox.info(ShellI18nHelper.connectTip5());
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
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
