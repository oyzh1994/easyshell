package cn.oyzh.easyshell.tabs.message;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;


/**
 * shell消息业务
 *
 * @author oyzh
 * @since 2025/04/23
 */
public class ShellMessageTabController extends RichTabController {

    /**
     * 事件消息
     */
    public static final ObservableList<EventFormatter> EVENT_MESSAGES = FXCollections.observableArrayList();

    /**
     * 变更监听器
     */
    private ListChangeListener<EventFormatter> changeListener = change -> {
        if (change.next()) {
            this.appendMsg(change.getAddedSubList());
        }
    };

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
        EVENT_MESSAGES.clear();
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        StageManager.showMask(() -> {
            synchronized (EVENT_MESSAGES) {
                this.appendMsg(EVENT_MESSAGES);
                EVENT_MESSAGES.addListener(this.changeListener);
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        synchronized (EVENT_MESSAGES) {
            EVENT_MESSAGES.removeListener(this.changeListener);
        }
    }

    /**
     * 追加消息
     *
     * @param formatters 消息列表
     */
    private void appendMsg(List<? extends EventFormatter> formatters) {
        if (CollectionUtil.isNotEmpty(formatters)) {
            for (EventFormatter formatter : formatters) {
                this.msgArea.appendLine(formatter.eventFormat());
            }
        }
    }
}
