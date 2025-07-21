package cn.oyzh.easyshell.popups;

import atlantafx.base.controls.Popover;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.fx.snippet.ShellSnippetListView;
import cn.oyzh.easyshell.store.ShellSnippetStore;
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
     * 组件
     */
    @FXML
    private ShellSnippetListView root;

    /**
     * 片段存储
     */
    private final ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        try {
            List<ShellSnippet> snippets = this.snippetStore.selectList();
            this.root.init(snippets);
            this.root.setOnItemPicked(() -> {
                ShellSnippet snippet = this.root.getPickedItem();
                this.submit(snippet);
                this.closeWindow();
            });
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }
}