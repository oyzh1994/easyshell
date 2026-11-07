package cn.oyzh.easyshell.tabs.valkey.subscribe;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import org.apache.commons.lang3.StringUtils;
import io.valkey.JedisPubSub;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * redis订阅及发布内容组件
 *
 * @author oyzh
 * @since 2025-11-18
 */
public class ShellValkeySubscribeTabController extends RichTabController {

    /**
     * redis客户端
     */
    private ShellValkeyClient client;

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
     * 订阅消息
     */
    @FXML
    private ShellDataEditor msg;

    /**
     * 订阅通道
     */
    @FXML
    private ClearableTextField channel;

    /**
     * 发布通道
     */
    @FXML
    private ClearableTextField publishChannel;

    /**
     * 发布消息
     */
    @FXML
    private ShellDataEditor publishMsg;

    /**
     * 订阅线程
     */
    private Thread pubsubThread;

    /**
     * 初始化
     *
     * @param client redis客户端
     */
    public void init(ShellValkeyClient client) {
        this.client = client;
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellValkeyClient getClient() {
        return client;
    }

    /**
     * 订阅
     */
    public void subscribe() {
        try {
            String channel = this.channel.getText();
            if (StringUtils.isEmpty(channel)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                this.channel.requestFocus();
                this.subscribe.setSelected(false);
                return;
            }
            this.channel.disable();
            // 异步订阅
            this.pubsubThread = ThreadUtil.start(() -> {
                try {
                    this.getClient().psubscribe(this.pubSub, channel);
                    JulLog.info("subscribe:{} success", channel);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 取消订阅
     */
    public void unsubscribe() {
        try {
            // 结束订阅
            ThreadUtil.interrupt(this.pubsubThread);
            if (this.pubSub != null && this.pubSub.isSubscribed()) {
                this.pubSub.unsubscribe();
            }
            if (this.channel != null) {
                this.channel.enable();
            }
            JulLog.info("unsubscribe:{} success", this.channel == null ? null : this.channel.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 发布消息
     */
    @FXML
    private void send() {
        try {
            String channel = this.publishChannel.getText();
            if (StringUtils.isEmpty(channel)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                this.publishChannel.requestFocus();
                return;
            }
            this.getClient().publish(channel, this.publishMsg.getTextTrim());
            MessageBox.okToast(I18nHelper.messageSent(), this.getWindow());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 清除订阅消息
     */
    @FXML
    private void clearMsg() {
        this.msg.clear();
    }

    @Override
    public void onTabClosed(Event event) {
        this.unsubscribe();
        super.onTabClosed(event);
    }

    @Override
    public void destroy() {
        this.unsubscribe();
        super.destroy();
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
}
