package cn.oyzh.easyshell.tabs.redis;

import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.tabs.redis.key.RedisKeysTabController;
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

    private RedisClient client;

    @FXML
    private RedisKeysTabController keysController;

    @FXML
    private RedisServerTabController serverController;

    @FXML
    private RedisTerminalTabController terminalController;

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(keysController, serverController, terminalController);
    }

    public void init(RedisClient client) {
        this.client = client;
        this.keysController.init(client);
        this.serverController.init(client);
        this.terminalController.init(client, null);
    }

    public RedisClient getClient() {
        return client;
    }
}
