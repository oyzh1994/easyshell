package cn.oyzh.easyshell.tabs.zk;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.tabs.ShellParentTabController;
import cn.oyzh.easyshell.tabs.zk.node.ZKNodeTabController;
import cn.oyzh.easyshell.tabs.zk.query.ZKQueryTabController;
import cn.oyzh.easyshell.tabs.zk.server.ZKServerTabController;
import cn.oyzh.easyshell.tabs.zk.terminal.ZKTerminalTabController;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-03
 */
public class ShellZKTabController extends ShellParentTabController {

    /**
     * 客户端
     */
    private ZKClient client;

    /**
     * 根节点
     */
    @FXML
    private FXTabPane root;

    /**
     * 键组件
     */
    @FXML
    private FXTab keys;

    /**
     * 键
     */
    @FXML
    private ZKNodeTabController nodeController;

    /**
     * 查询
     */
    @FXML
    private ZKQueryTabController queryController;

    /**
     * 服务
     */
    @FXML
    private ZKServerTabController serverController;

    /**
     * 终端
     */
    @FXML
    private ZKTerminalTabController terminalController;

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(nodeController, queryController, serverController, terminalController);
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = new ZKClient(connect);
        // 加载根节点
        StageManager.showMask(() -> {
            try {
                this.client.start();
                if (!this.client.isConnected()) {
                    this.client.close();
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                this.hideLeft();
                this.nodeController.init(this.client);
                this.queryController.init(this.client,null);
                this.serverController.init(this.client);
                this.terminalController.init(this.client);
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    public ZKClient getClient() {
        return client;
    }
}
