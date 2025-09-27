package cn.oyzh.easyshell.controller.zk.history;

import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * zk数据历史查看业务
 *
 * @author oyzh
 * @since 2025/09/06
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "zk/history/shellZKHistoryView.fxml"
)
public class ShellZKHistoryViewController extends StageController {

    /**
     * 数据组件
     */
    @FXML
    private ShellDataEditor editor;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
        byte[] bytes = this.getProp("data");
        this.editor.showData(bytes);
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.view1History();
    }
}
