package cn.oyzh.easyssh.controller.main;

import cn.oyzh.common.Const;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.plus.controller.SubStageController;
import javafx.fxml.FXML;


/**
 * ssh消息业务
 *
 * @author oyzh
 * @since 2024/04/23
 */
public class MessageController extends SubStageController   {

    /**
     * 消息文本框
     */
    @FXML
    private MsgTextArea msgArea;

    /**
     * 清空消息
     */
    @FXML
    private void clearMsg() {
        this.msgArea.clear();
    }

    @EventSubscribe
    private void onEventMsg(EventFormatter formatter) {
        String formatMsg = formatter.eventFormat();
        if (formatMsg != null) {
            this.msgArea.appendLine(String.format("%s %s", Const.DATE_TIME_FORMAT.format(System.currentTimeMillis()), formatMsg));
        }
    }
}
