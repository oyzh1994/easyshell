package cn.oyzh.easyshell.tabs.zk.home;

import cn.oyzh.easyshell.dto.zk.ShellZKEnvNode;
import cn.oyzh.easyshell.dto.zk.ShellZKServerInfo;
import cn.oyzh.easyshell.tabs.zk.ShellZKTabPane;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk主页标签页控制器
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKHomeTabController extends RichTabController {

    /**
     * 客户端
     */
    private ShellZKClient client;

    @FXML
    private FXLabel version;

    @FXML
    private FXLabel mode;

    @FXML
    private FXLabel zxid;

    @FXML
    private FXLabel nodeCount;

    @FXML
    private FXLabel connections;

    @FXML
    private FXLabel latency;

    @FXML
    private FXLabel command;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof ShellZKTabPane tabPane) {
                if (tabPane.getClient() != null) {
                    this.client = tabPane.getClient();
                    this.initInfo();
                } else {
                    tabPane.clientProperty().addListener((observable1, oldValue1, newValue1) -> {
                        if (newValue1 != null) {
                            this.client = newValue1;
                            this.initInfo();
                        }
                    });
                }
            }
        });
        super.flushTab();
    }

    /**
     * 初始化信息
     */
    private void initInfo() {
        try {
            if (this.client == null || !this.client.isConnected()) {
                return;
            }
            List<ShellZKEnvNode> envNodes = this.client.srvrNodes();
            ShellZKServerInfo info = new ShellZKServerInfo();
            info.update(envNodes);
            this.version.text(info.getVersion());
            this.mode.text(info.getMode());
            this.zxid.text(info.getZxid());
            this.nodeCount.text(info.getNodeCount());
            this.connections.text(info.getConnections());
            this.latency.text(info.getLatencyInfo());
            this.command.text(info.getCommandInfo());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
