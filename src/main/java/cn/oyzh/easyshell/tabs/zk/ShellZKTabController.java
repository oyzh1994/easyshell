package cn.oyzh.easyshell.tabs.zk;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.tabs.ShellParentTabController;
import cn.oyzh.easyshell.tabs.zk.auth.ShellZKAuthTabController;
import cn.oyzh.easyshell.tabs.zk.node.ShellZKNodeTabController;
import cn.oyzh.easyshell.tabs.zk.query.ShellZKQueryTabController;
import cn.oyzh.easyshell.tabs.zk.server.ShellZKServerTabController;
import cn.oyzh.easyshell.tabs.zk.terminal.ShellZKTerminalTabController;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
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
    private ShellZKClient client;

    /**
     * 节点
     */
    @FXML
    private ShellZKNodeTabController nodeController;

    /**
     * 查询
     */
    @FXML
    private ShellZKQueryTabController queryController;

    /**
     * 认证
     */
    @FXML
    private ShellZKAuthTabController authController;

    /**
     * 服务
     */
    @FXML
    private ShellZKServerTabController serverController;

    /**
     * 终端
     */
    @FXML
    private ShellZKTerminalTabController terminalController;

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(
                this.nodeController,
                this.queryController,
                this.authController,
                this.serverController,
                this.terminalController
        );
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = new ShellZKClient(connect);
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
                this.queryController.init(this.client);
                this.authController.init(this.client);
                this.serverController.init(this.client);
                this.terminalController.init(this.client.forkClient());
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    public ShellZKClient getClient() {
        return client;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        IOUtil.close(this.client);
    }

    @Override
    public void destroy() {
        this.nodeController.destroy();
        this.queryController.destroy();
        this.authController.destroy();
        this.serverController.destroy();
        this.terminalController.destroy();
        super.destroy();
    }
}
