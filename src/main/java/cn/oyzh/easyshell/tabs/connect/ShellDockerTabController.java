package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.docker.DockerInfoController;
import cn.oyzh.easyshell.controller.docker.DockerVersionController;
import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.fx.ShellContainerStatusComboBox;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.docker.ShellDockerContainerTabController;
import cn.oyzh.easyshell.tabs.connect.docker.ShellDockerExtraTabController;
import cn.oyzh.easyshell.tabs.connect.docker.ShellDockerImageTabController;
import cn.oyzh.easyshell.trees.docker.DockerContainerTableView;
import cn.oyzh.easyshell.trees.docker.DockerImageTableView;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellDockerTabController extends ParentTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    @FXML
    private ShellDockerContainerTabController containerController;

    @FXML
    private ShellDockerImageTabController imageController;

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
        return List.of(this.containerController, this.imageController, this.extraController);
    }
}
