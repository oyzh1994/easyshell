package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.easyshell.fx.ShellDataTextAreaPane;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * docker信息业务
 *
 * @author oyzh
 * @since 2025/03/13
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerVersion.fxml"
)
public class ShellDockerVersionController extends StageController {

    /**
     * 信息
     */
    @FXML
    private ShellDataTextAreaPane version;

    @FXML
    private void copyVersion() {
        ClipboardUtil.copy(this.version.getText());
        MessageBox.okToast(I18nHelper.operationSuccess());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        String inspect = this.getProp("version");
        this.version.setText(inspect);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return "Docker Version";
    }
}
