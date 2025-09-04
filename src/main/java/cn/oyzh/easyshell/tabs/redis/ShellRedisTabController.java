package cn.oyzh.easyshell.tabs.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.tabs.ShellParentTabController;
import cn.oyzh.easyshell.tabs.redis.key.RedisKeysTabController;
import cn.oyzh.easyshell.tabs.redis.query.RedisQueryTabController;
import cn.oyzh.easyshell.tabs.redis.server.RedisServerTabController;
import cn.oyzh.easyshell.tabs.redis.terminal.RedisTerminalTabController;
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
    private RedisKeysTabController keysController;

    /**
     * 查询
     */
    @FXML
    private RedisQueryTabController queryController;

    /**
     * 服务
     */
    @FXML
    private RedisServerTabController serverController;

    /**
     * 终端
     */
    @FXML
    private RedisTerminalTabController terminalController;

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(keysController, queryController, serverController, terminalController);
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
                    this.queryController.init(this.client);
                    this.serverController.init(this.client);
                    this.terminalController.init(this.client);
                } else {
                    this.keysController.init(this.client);
                    this.queryController.init(this.client);
                    this.serverController.init(this.client);
                    this.terminalController.init(this.client);
                }
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
}
