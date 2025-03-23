package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.server.ServerExec;
import cn.oyzh.easyshell.server.ServerMonitor;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.config.ShellProfileTabController;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.List;

/**
 * 服务器监控tab内容组件
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

    public ShellClient getClient() {
        return client;
    }

    /**
     * zk客户端
     */
    private ShellClient client;

    /**
     * 服务信息
     */
    @FXML
    private FXTableView<ServerMonitor> serverTable;

    /**
     * 汇总信息
     */
    @FXML
    private ShellProfileTabController profileController;

    /**
     *
     */
    private ServerExec serverExec;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellClient client) {
        this.client = client;
        this.serverExec = this.client.serverExec();
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.profileController);
    }
}