package cn.oyzh.easyshell.tabs.redis;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.tabs.ShellParentTabController;
import cn.oyzh.easyshell.tabs.redis.key.ShellRedisKeysTabController;
import cn.oyzh.easyshell.tabs.redis.publish.ShellRedisPublishTabController;
import cn.oyzh.easyshell.tabs.redis.query.ShellRedisQueryTabController;
import cn.oyzh.easyshell.tabs.redis.server.ShellRedisServerTabController;
import cn.oyzh.easyshell.tabs.redis.subscribe.ShellRedisSubscribeTabController;
import cn.oyzh.easyshell.tabs.redis.terminal.ShellRedisTerminalTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
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
public class ShellRedisTabController extends ShellParentTabController {

    /**
     * 客户端
     */
    private ShellRedisClient client;

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
    private ShellRedisKeysTabController keysController;

    /**
     * 查询
     */
    @FXML
    private ShellRedisQueryTabController queryController;

    /**
     * 服务
     */
    @FXML
    private ShellRedisServerTabController serverController;

    /**
     * 终端
     */
    @FXML
    private ShellRedisTerminalTabController terminalController;

    /**
     * 发布
     */
    @FXML
    private ShellRedisPublishTabController publishController;

    /**
     * 订阅
     */
    @FXML
    private ShellRedisSubscribeTabController subscribeController;

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(
                this.keysController,
                this.queryController,
                this.serverController,
                this.terminalController,
                this.publishController,
                this.subscribeController
        );
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = new ShellRedisClient(connect);
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
                if (this.client.isSentinelMode()) {
                    this.root.removeTab(this.keys);
                } else {
                    this.keysController.init(this.client);
                }
                this.queryController.init(this.client);
                this.serverController.init(this.client);
                this.terminalController.init(this.client.forkClient());
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    public ShellRedisClient getClient() {
        return client;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.subscribeController.unsubscribe();
        IOUtil.close(this.client);
    }

    @Override
    public void destroy() {
        this.root.destroy();
        this.keysController.destroy();
        this.queryController.destroy();
        this.serverController.destroy();
        this.terminalController.destroy();
        this.publishController.destroy();
        this.subscribeController.destroy();
        super.destroy();
    }
}
