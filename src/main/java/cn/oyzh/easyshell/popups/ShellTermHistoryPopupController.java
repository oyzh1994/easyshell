package cn.oyzh.easyshell.popups;

import atlantafx.base.controls.Popover;
import cn.oyzh.easyshell.domain.ShellTermHistory;
import cn.oyzh.easyshell.fx.ShellTermHistoryListView;
import cn.oyzh.easyshell.store.ShellTermHistoryStore;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * 终端历史弹窗
 *
 * @author oyzh
 * @since 2025/05/31
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "shellTermHistoryPopup.fxml",
        arrowLocation = Popover.ArrowLocation.BOTTOM_LEFT,
        anchorLocation = PopupWindow.AnchorLocation.WINDOW_TOP_LEFT
)
public class ShellTermHistoryPopupController extends PopupController {

    /**
     * 匹配大小写
     */
    @FXML
    private ShellTermHistoryListView root;

    /**
     * 终端历史存储
     */
    private final ShellTermHistoryStore termHistoryStore = ShellTermHistoryStore.INSTANCE;

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        String iid = this.getProp("iid");
        List<ShellTermHistory> histories = this.termHistoryStore.loadByIid(iid);
        this.root.init(histories);
        this.root.setOnItemPicked(() -> {
            ShellTermHistory history = this.root.getPickedItem();
            this.submit(history);
            this.closeWindow();
        });
    }
}