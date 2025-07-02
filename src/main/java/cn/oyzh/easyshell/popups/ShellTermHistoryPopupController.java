package cn.oyzh.easyshell.popups;

import atlantafx.base.controls.Popover;
import cn.oyzh.easyshell.fx.term.ShellTermHistoryListView;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.server.ShellServerExec;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.information.MessageBox;
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

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        try {
            ShellSSHClient client = this.getProp("client");
            ShellServerExec serverExec = client.serverExec();
            // 持久化命令
            serverExec.persistentCommand();
            // 获取最近50条历史
            List<String> histories = serverExec.history(50);
            this.root.init(histories);
            this.root.setOnItemPicked(() -> {
                String history = this.root.getPickedItem();
                this.submit(history);
                this.closeWindow();
            });
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }
}