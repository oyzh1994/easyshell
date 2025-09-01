package cn.oyzh.easyshell.popups.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.redis.RedisKeyFilterHistory;
import cn.oyzh.easyshell.fx.redis.RedisKeyFilterHistoryPopup;
import cn.oyzh.easyshell.store.redis.RedisKeyFilterHistoryStore;
import cn.oyzh.fx.gui.text.field.SearchTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

import static atlantafx.base.controls.Popover.ArrowLocation.BOTTOM_LEFT;
import static javafx.stage.PopupWindow.AnchorLocation.WINDOW_TOP_LEFT;


/**
 * redis键过滤弹窗
 *
 * @author oyzh
 * @since 2025/02/10
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "redisKeyFilterPopup.fxml",
        arrowLocation = BOTTOM_LEFT,
        anchorLocation = WINDOW_TOP_LEFT
)
public class RedisKeyFilterPopupController extends PopupController {

    /**
     * 过滤模式
     */
    @FXML
    private SearchTextField keyFilter;

    /**
     * 过滤历史储存
     */
    private final RedisKeyFilterHistoryStore historyStore = RedisKeyFilterHistoryStore.INSTANCE;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        String pattern = this.getProp("pattern");
        if (!StringUtil.isBlank(pattern)) {
            this.keyFilter.setText(pattern);
        }
        this.keyFilter.requestFocus();
        this.keyFilter.setHistoryPopup(new RedisKeyFilterHistoryPopup());
    }

    /**
     * 应用
     */
    @FXML
    private void apply() {
        String pattern = this.keyFilter.getText();
        if (StringUtil.isNotBlank(pattern) && !"*".equals(pattern)) {
            RedisKeyFilterHistory history = new RedisKeyFilterHistory();
            history.setPattern(pattern);
            this.historyStore.insert(history);
        }
        this.submit(pattern);
        this.closeWindow();
    }

    /**
     * 关闭
     */
    @FXML
    private void close() {
        this.closeWindow();
    }
}
