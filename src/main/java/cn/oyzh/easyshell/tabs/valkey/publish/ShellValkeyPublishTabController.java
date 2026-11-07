//package cn.oyzh.easyshell.tabs.valkey.publish;
//
//import cn.oyzh.easyshell.fx.ShellDataEditor;
//import cn.oyzh.easyshell.valkey.ShellValkeyClient;
//import cn.oyzh.easyshell.tabs.valkey.ShellValkeyTabController;
//import cn.oyzh.fx.gui.tabs.SubTabController;
//import cn.oyzh.fx.gui.text.field.ClearableTextField;
//import cn.oyzh.fx.plus.information.MessageBox;
//import cn.oyzh.i18n.I18nHelper;
//import javafx.fxml.FXML;
//import org.apache.commons.lang3.StringUtils;
//
///**
// * redis发布内容组件
// *
// * @author oyzh
// * @since 2025-11-18
// */
//public class ShellValkeyPublishTabController extends SubTabController {
//
//    /**
//     * 消息
//     */
//    @FXML
//    private ShellDataEditor msg;
//
//    /**
//     * 通道
//     */
//    @FXML
//    private ClearableTextField channel;
//
//    @Override
//    public ShellValkeyTabController parent() {
//        return (ShellValkeyTabController) super.parent();
//    }
//
//    /**
//     * 获取redis客户端
//     *
//     * @return redis客户端
//     */
//    public ShellValkeyClient getClient() {
//        return this.parent().getClient();
//    }
//
//    /**
//     * 发送信息
//     */
//    @FXML
//    private void send() {
//        try {
//            String channel = this.channel.getText();
//            if (StringUtils.isEmpty(channel)) {
//                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
//                this.channel.requestFocus();
//                return;
//            }
//            this.getClient().publish(channel, this.msg.getTextTrim());
//            MessageBox.okToast(I18nHelper.messageSent(), this.getWindow());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex, this.getWindow());
//        }
//    }
//
////    @Override
////    public void destroy() {
////        this.msg.destroy();
////        super.destroy();
////    }
//}
//
