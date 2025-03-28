package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import cn.oyzh.fx.rich.richtextfx.data.RichDataType;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTypeComboBox;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpATTRS;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;

/**
 * ssh文件权限业务
 *
 * @author oyzh
 * @since 2025/03/28
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "sftp/shellSftpFilePermission.fxml"
)
public class ShellSftpFilePermissionController extends StageController {

    /**
     * 远程文件
     */
    private SftpFile file;

    /**
     * ssh客户端
     */
    private ShellClient client;

    /**
     * 数据
     */
    @FXML
    private ReadOnlyTextField fileName;

    /**
     * 拥有者读取
     */
    @FXML
    private FXCheckBox ownerR;

    /**
     * 拥有者写入
     */
    @FXML
    private FXCheckBox ownerW;

    /**
     * 拥有者执行
     */
    @FXML
    private FXCheckBox ownerE;

    /**
     * 分组读取
     */
    @FXML
    private FXCheckBox groupsR;

    /**
     * 分组写入
     */
    @FXML
    private FXCheckBox groupsW;

    /**
     * 分组执行
     */
    @FXML
    private FXCheckBox groupsE;

    /**
     * 其他读取
     */
    @FXML
    private FXCheckBox othersR;

    /**
     * 其他写入
     */
    @FXML
    private FXCheckBox othersW;

    /**
     * 其他执行
     */
    @FXML
    private FXCheckBox othersE;

    /**
     * 保存文件
     */
    @FXML
    private void save() {
        StageManager.showMask(() -> {
            try {
                ShellEventUtil.fileSaved(this.file);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.file = this.getWindowProp("file");
        this.client = this.getWindowProp("client");
        if (this.file.hasOwnerReadPermission()) {
            this.ownerR.setSelected(true);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.filePermission();
    }

}
