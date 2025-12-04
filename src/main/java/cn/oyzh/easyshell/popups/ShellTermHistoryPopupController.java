package cn.oyzh.easyshell.popups;

import atlantafx.base.controls.Popover;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.fx.term.ShellTermHistoryListView;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.server.ShellServerExec;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
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
     * ssh客户端
     */
    private ShellSSHClient client;

    /**
     * 关键字
     */
    @FXML
    private ClearableTextField kw;

    /**
     * 列表组件
     */
    @FXML
    private ShellTermHistoryListView listView;

    /**
     * 初始化历史
     */
    private void initList() {
        try {
            ShellServerExec serverExec = this.client.serverExec();
            // 持久化命令
            serverExec.persistentCommand();
            // 关键字
            String kw = this.kw.getText();
            // 获取最近200条历史
            List<String> histories = serverExec.history(200, kw);
            // 初始化数据
            this.listView.init(histories);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.kw.addTextChangeListener((observableValue, s, t1) -> {
            ThreadUtil.start(this::initList);
        });
        this.listView.setOnItemPicked(() -> {
            String history = this.listView.getPickedItem();
            this.submit(history);
            this.closeWindow();
        });
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.client = this.getProp("client");
        this.initList();
    }
}