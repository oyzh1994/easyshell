package cn.oyzh.easyshell.controller.split;

import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;

/**
 * 终端分屏引导业务
 *
 * @author oyzh
 * @since 2025/05/29
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "split/shellSplitGuid.fxml"
)
public class ShellSplitGuidController extends StageController {

    /**
     * tab组件
     */
    @FXML
    private FXToggleGroup type;

    /**
     * 终端分屏
     */
    @FXML
    private void toSplit() {
        try {
            String type = this.type.selectedUserData();
            ShellEventUtil.showSplit(type);
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.termSplitView();
    }
}
