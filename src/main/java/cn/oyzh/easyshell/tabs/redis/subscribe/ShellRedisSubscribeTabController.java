package cn.oyzh.easyshell.tabs.redis.subscribe;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.tabs.redis.ShellRedisTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPubSub;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * redis订阅内容组件
 *
 * @author oyzh
 * @since 2025/11/18
 */
public class ShellRedisSubscribeTabController extends SubTabController {

    /**
     * 订阅组件
     */
    private JedisPubSub pubSub;

    /**
     * 订阅按钮
     */
    @FXML
    private FXToggleSwitch subscribe;

    /**
     * 消息
     */
    @FXML
    private ReadOnlyTextArea msg;

    /**
     * 通道
     */
    @FXML
    private ClearableTextField channel;

    /**
     * 订阅
     *
     */
    public void subscribe() {
        try {
            String channel = this.channel.getText();
            if (StringUtils.isEmpty(channel)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                this.channel.requestFocus();
                this.subscribe.setSelected(false);
                return;
            }
            this.channel.disable();
            ThreadUtil.start(() -> {
                try {
                    this.getClient().psubscribe(this.pubSub, channel);
                    JulLog.info("subscribe:{} success", channel);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.unsubscribe();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 取消订阅
     */
    public void unsubscribe() {
        try {
            if (this.pubSub.isSubscribed()) {
                this.pubSub.unsubscribe();
            }
            this.channel.enable();
            JulLog.info("unsubscribe:{} success", channel);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.unsubscribe();
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        this.subscribe.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                this.subscribe();
            } else {
                this.unsubscribe();
            }
        });
        this.pubSub = new JedisPubSub() {
            @Override
            public void onPMessage(String pattern, String channel, String message) {
                msg.appendLine("pattern: " + pattern + " channel:" + channel + " message:" + message);
            }

            @Override
            public void onPSubscribe(String pattern, int subscribedChannels) {
                msg.appendLine("subscribe pattern:" + pattern + " subscribed channels:" + subscribedChannels);
            }

            @Override
            public void onPUnsubscribe(String pattern, int subscribedChannels) {
                msg.appendLine("unsubscribe pattern:" + pattern + " subscribed channels:" + subscribedChannels);
            }
        };
    }

    @Override
    public ShellRedisTabController parent() {
        return (ShellRedisTabController) super.parent();
    }

    public ShellRedisClient getClient() {
        return this.parent().getClient();
    }

    /**
     * 清除消息
     */
    @FXML
    private void clearMsg() {
        this.msg.clear();
    }

    @Override
    public void destroy() {
        this.unsubscribe();
        super.destroy();
    }
}
