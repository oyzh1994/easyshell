package cn.oyzh.easyssh.controller.docker;

import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import cn.oyzh.fx.rich.richtextfx.json.RichJsonTextAreaPane;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * docker日志信息业务
 *
 * @author oyzh
 * @since 2025/03/13
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/dockerLogs.fxml"
)
public class DockerLogsController extends StageController {

    /**
     * 日志
     */
    @FXML
    private RichDataTextAreaPane logs;

    @FXML
    private void copyLogs() {
        ClipboardUtil.copy(this.logs.getText());
        MessageBox.okToast(I18nHelper.operationSuccess());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        String logs = this.getWindowProp("logs");
        this.logs.setText(logs);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.logs();
    }
}
