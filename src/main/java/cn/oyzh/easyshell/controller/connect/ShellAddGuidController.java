package cn.oyzh.easyshell.controller.connect;

import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 连接新增引导业务
 *
 * @author oyzh
 * @since 2025/04/24
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/shellAddGuid.fxml"
)
public class ShellAddGuidController extends StageController {

    /**
     * tab组件
     */
    @FXML
    private FXToggleGroup type;

    /**
     * 分组
     */
    private ShellGroup group;

    /**
     * 添加连接
     */
    @FXML
    private void toAdd() {
        try {
            if ("ssh".equals(this.type.selectedUserData())) {
                ShellViewFactory.addConnect(group);
            } else if ("local".equals(this.type.selectedUserData())) {
                ShellViewFactory.addLocalConnect(group);
            } else if ("telnet".equals(this.type.selectedUserData())) {
                ShellViewFactory.addTelnetConnect(group);
            } else if ("sftp".equals(this.type.selectedUserData())) {
                ShellViewFactory.addSftpConnect(group);
            } else {
                ShellViewFactory.addSerialConnect(group);
            }
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addGuid();
    }
}
