package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.server.ServerExec;
import cn.oyzh.easyshell.server.ServerMonitor;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.config.ShellEnvironmentTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellProfileTabController;
import cn.oyzh.easyshell.tabs.connect.config.ShellUserProfileTabController;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
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
    }

    public ShellClient getClient() {
        return client;
    }

    /**
     * 配置文件1
     */
    @FXML
    private ShellProfileTabController profileController;

    /**
     * 配置文件2
     */
    @FXML
    private ShellEnvironmentTabController environmentController;

    /**
     * 配置文件3
     */
    @FXML
    private ShellUserProfileTabController userProfileController;

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
        return List.of(this.profileController, this.userProfileController, this.environmentController
        );
    }
}