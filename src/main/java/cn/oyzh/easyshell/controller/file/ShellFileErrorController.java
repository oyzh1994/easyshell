package cn.oyzh.easyshell.controller.file;

import cn.oyzh.easyshell.file.ShellFileTask;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 文件任务错误信息
 *
 * @author oyzh
 * @since 2025/06/26
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "file/shellFileError.fxml"
)
public class ShellFileErrorController extends StageController {

    /**
     * 错误
     */
    @FXML
    private ReadOnlyTextArea error;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        ShellFileTask task = this.getProp("task");
        this.error.setText(task.getErrorMsg());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.errorInfo();
    }
}
