package cn.oyzh.easyshell.tabs.redis.pubsub;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.dto.redis.RedisPubsubItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import redis.clients.jedis.JedisPubSub;

/**
 * redis发布订阅内容组件
 *
 * @author oyzh
 * @since 2023/08/02
 */
public class RedisPubsubTabController extends RichTabController {

    /**
     * 订阅组件
     */
    private JedisPubSub pubSub;

    /**
     * 文本域
     */
    @FXML
    private ReadOnlyTextArea textArea;

    /**
     * 初始化
     *
     * @param item redis发布订阅键
     */
    public void init(RedisPubsubItem item) {
        this.textArea.appendText(ShellI18nHelper.redisPubsubTip1() + item.getChannel());
        this.pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                textArea.appendLine(I18nHelper.receivedMessage() + " : " + message);
            }
        };
        ThreadUtil.startVirtual(() -> item.getClient().subscribe(this.pubSub, item.getChannel()));
    }

    /**
     * 取消订阅
     */
    public void unsubscribe() {
        try {
            this.pubSub.unsubscribe();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
