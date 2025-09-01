package cn.oyzh.easyshell.tabs.redis.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.easyshell.util.RedisConnectUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * redis命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class RedisTerminalTabController extends RichTabController {

    /**
     * redis命令行文本域
     */
    @FXML
    private RedisTerminalPane terminal;

    /**
     * 初始化
     *
     * @param client redis客户端
     */
    public void init(RedisClient client, Integer dbIndex) {
        this.terminal.init(client, dbIndex);
    }

    /**
     * redis信息
     *
     * @return 当前redis信息
     */
    protected ShellConnect shellConnect() {
        return this.terminal.shellConnect();
    }

    public Integer dbIndex() {
        return this.terminal.getDbIndex();
    }

    public RedisClient client() {
        return this.terminal.getClient();
    }

    @Override
    public void onTabClosed(Event event) {
        if (this.terminal.isTemporary()) {
            RedisConnectUtil.close(this.client(), true, true);
        }
        super.onTabClosed(event);
    }
}
