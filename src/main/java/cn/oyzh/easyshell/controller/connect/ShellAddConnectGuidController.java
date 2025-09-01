package cn.oyzh.easyshell.controller.connect;

import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
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
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/shellAddConnectGuid.fxml"
)
public class ShellAddConnectGuidController extends StageController {

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
            String userData = this.type.selectedUserData();
            if ("ssh".equalsIgnoreCase(userData)) {
                ShellViewFactory.addSSHConnect(group);
            } else if ("local".equalsIgnoreCase(userData)) {
                ShellViewFactory.addLocalConnect(group);
            } else if ("telnet".equalsIgnoreCase(userData)) {
                ShellViewFactory.addTelnetConnect(group);
            } else if ("sftp".equalsIgnoreCase(userData)) {
                ShellViewFactory.addSFTPConnect(group);
            } else if ("ftp".equalsIgnoreCase(userData)) {
                ShellViewFactory.addFTPConnect(group);
            } else if ("s3".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, null);
            } else if ("s3_cos".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "cos");
            } else if ("s3_obs".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "obs");
            } else if ("s3_oss".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "oss");
            } else if ("s3_minio".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "minio");
            } else if ("serial".equalsIgnoreCase(userData)) {
                ShellViewFactory.addSerialConnect(group);
            } else if ("vnc".equalsIgnoreCase(userData)) {
                ShellViewFactory.addVNCConnect(group);
            } else if ("rlogin".equalsIgnoreCase(userData)) {
                ShellViewFactory.addRLoginConnect(group);
            } else if ("smb".equalsIgnoreCase(userData)) {
                ShellViewFactory.addSMBConnect(group);
            } else if ("redis".equalsIgnoreCase(userData)) {
                ShellViewFactory.addRedisConnect(group);
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
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addGuid();
    }
}
