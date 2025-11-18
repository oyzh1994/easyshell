package cn.oyzh.easyshell.tabs.redis.publish;

import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.tabs.redis.ShellRedisTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import org.apache.commons.lang3.StringUtils;

/**
 * redis发布内容组件
 *
 * @author oyzh
 * @since 2025/11/18
 */
public class ShellRedisPublishTabController extends SubTabController {

    /**
     * 消息
     */
    @FXML
    private ShellDataEditor msg;

    /**
     * 通道
     */
    @FXML
    private ClearableTextField channel;

    @Override
    public ShellRedisTabController parent() {
        return (ShellRedisTabController) super.parent();
    }

    public ShellRedisClient getClient() {
        return this.parent().getClient();
    }

    /**
     * 发送信息
     */
    @FXML
    private void send() {
        try {
            String channel = this.channel.getText();
            if (StringUtils.isEmpty(channel)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                this.channel.requestFocus();
                return;
            }
            this.getClient().publish(channel, this.msg.getTextTrim());
            MessageBox.okToast(I18nHelper.messageSent());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
