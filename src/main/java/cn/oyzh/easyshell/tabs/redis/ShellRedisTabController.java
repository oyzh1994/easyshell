package cn.oyzh.easyshell.tabs.redis;

import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.tabs.redis.key.RedisKeysTabController;
import cn.oyzh.easyshell.tabs.redis.query.RedisQueryTabController;
import cn.oyzh.easyshell.tabs.redis.server.RedisServerTabController;
import cn.oyzh.easyshell.tabs.redis.terminal.RedisTerminalTabController;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-03
 */
public class ShellRedisTabController extends ParentTabController {

    /**
     * 客户端
     */
    private RedisClient client;

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
     * @param client 客户端
     */
    public void init(RedisClient client) {
        this.client = client;
        this.keysController.init(client);
        this.queryController.init(client);
        this.serverController.init(client);
        this.terminalController.init(client);
    }

    public RedisClient getClient() {
        return client;
    }
}
