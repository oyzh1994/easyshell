package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * ssh文件信息业务
 *
 * @author oyzh
 * @since 2025/06
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "sftp/sshSftpFileInfo.fxml"
)
public class ShellSftpFileInfoController extends StageController {

    /**
     * 分组
     */
    @FXML
    private ReadOnlyTextField group;

    /**
     * 拥有者
     */
    @FXML
    private ReadOnlyTextField owner;

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 大小
     */
    @FXML
    private ReadOnlyTextField size;

    /**
     * 权限
     */
    @FXML
    private ReadOnlyTextField permissions;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        SftpFile file = this.getWindowProp("file");
        this.group.setText(file.getGroup());
        this.owner.setText(file.getOwner());
        this.name.setText(file.getName());
        this.size.setText(file.getSize());
        this.permissions.setText(file.getPermissions());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.fileInfo();
    }
}
