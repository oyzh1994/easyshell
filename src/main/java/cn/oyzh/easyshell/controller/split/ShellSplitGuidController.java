package cn.oyzh.easyshell.controller.split;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.split.ShellSplitListView;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 终端分屏引导业务
 *
 * @author oyzh
 * @since 2025/05/29
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "split/shellSplitGuid.fxml"
)
public class ShellSplitGuidController extends StageController {

    /**
     * 步骤1
     */
    @FXML
    private FXVBox step1;

    /**
     * 步骤2
     */
    @FXML
    private FXVBox step2;

    /**
     * tab组件
     */
    @FXML
    private FXToggleGroup type;

    /**
     * 连接列表
     */
    @FXML
    private ShellSplitListView splitListView;

    /**
     * 步骤1
     */
    @FXML
    private void showStep1() {
        this.splitListView.unSelectAll();
        this.step2.disappear();
        this.step1.display();
    }

    /**
     * 步骤2
     */
    @FXML
    private void showStep2() {
        String type = this.type.selectedUserData();
        int maxSize = 0;
        if (StringUtil.equalsAny(type, "type1", "type2")) {
            maxSize = 2;
        } else if (StringUtil.equalsAny(type, "type3", "type4")) {
            maxSize = 3;
        } else if (StringUtil.equalsAny(type, "type5", "type6")) {
            maxSize = 4;
        } else if (StringUtil.equalsAny(type, "type7")) {
            maxSize = 6;
        } else if (StringUtil.equalsAny(type, "type8")) {
            maxSize = 9;
        }
        this.splitListView.setMaxSelected(maxSize);
        this.step1.disappear();
        this.step2.display();
    }

    /**
     * 终端分屏
     */
    @FXML
    private void toSplit() {
        try {
            String type = this.type.selectedUserData();
            ShellEventUtil.showSplit(type, this.splitListView.getSelectedConnects());
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.termSplitView();
    }
}
