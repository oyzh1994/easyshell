package cn.oyzh.easyshell.popups;

import atlantafx.base.controls.Popover;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.fx.snippet.ShellSnippetListView;
import cn.oyzh.easyshell.store.ShellSnippetStore;
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
 * 片段列表弹窗
 *
 * @author oyzh
 * @since 2025/07/21
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "shellSnippetPopup.fxml",
        arrowLocation = Popover.ArrowLocation.BOTTOM_LEFT,
        anchorLocation = PopupWindow.AnchorLocation.WINDOW_TOP_LEFT
)
public class ShellSnippetPopupController extends PopupController {

    /**
     * 关键字
     */
    @FXML
    private ClearableTextField kw;

    /**
     * 列表组件
     */
    @FXML
    private ShellSnippetListView listView;

    /**
     * 片段存储
     */
    private final ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;

    /**
     * 初始化列表
     */
    private void initList() {
        try {
            // 关键字
            String kw = this.kw.getText();
            List<ShellSnippet> snippets = this.snippetStore.listByName(kw);
            this.listView.init(snippets);
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
            ShellSnippet snippet = this.listView.getPickedItem();
            this.submit(snippet);
            this.closeWindow();
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.initList();
    }
}