package cn.oyzh.easyshell.controller.ftp;

import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.ftp.ShellFTPFile;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.apache.commons.net.ftp.FTPFile;

/**
 * ssh文件权限业务
 *
 * @author oyzh
 * @since 2025/03/28
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "ftp/shellFTPFilePermission.fxml"
)
public class ShellFTPFilePermissionController extends StageController {

    /**
     * 远程文件
     */
    private ShellFTPFile file;

    /**
     * ssh客户端
     */
    private ShellFTPClient client;

    /**
     * 文件名称
     */
    @FXML
    private ReadOnlyTextField fileName;

    /**
     * 拥有者名称
     */
    @FXML
    private ReadOnlyTextField ownerName;

    /**
     * 分组名称
     */
    @FXML
    private ReadOnlyTextField groupName;

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
                StringBuilder perms = new StringBuilder();
                if (this.ownerR.isSelected()) {
                    perms.append("r");
                } else {
                    perms.append("-");
                }
                if (this.ownerW.isSelected()) {
                    perms.append("w");
                } else {
                    perms.append("-");
                }
                if (this.ownerE.isSelected()) {
                    perms.append("x");
                } else {
                    perms.append("-");
                }
                if (this.groupsR.isSelected()) {
                    perms.append("r");
                } else {
                    perms.append("-");
                }
                if (this.groupsW.isSelected()) {
                    perms.append("w");
                } else {
                    perms.append("-");
                }
                if (this.groupsE.isSelected()) {
                    perms.append("x");
                } else {
                    perms.append("-");
                }
                if (this.othersR.isSelected()) {
                    perms.append("r");
                } else {
                    perms.append("-");
                }
                if (this.othersW.isSelected()) {
                    perms.append("w");
                } else {
                    perms.append("-");
                }
                if (this.othersE.isSelected()) {
                    perms.append("x");
                } else {
                    perms.append("-");
                }
                int permission = ShellUtil.permissionToInt(perms.toString());
                if (this.client.chmod(permission, this.file.getFilePath())) {
                    this.file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, this.ownerR.isSelected());
                    this.file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, this.ownerW.isSelected());
                    this.file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, this.ownerE.isSelected());

                    this.file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, this.groupsR.isSelected());
                    this.file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, this.groupsW.isSelected());
                    this.file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, this.groupsE.isSelected());

                    this.file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, this.othersR.isSelected());
                    this.file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, this.othersW.isSelected());
                    this.file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, this.othersE.isSelected());
                    this.closeWindow();
                } else {
                    MessageBox.warn(I18nHelper.operationFail());
                }
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
        this.file = this.getProp("file");
        this.client = this.getProp("client");
        this.fileName.setText(this.file.getFileName());
        this.ownerName.setText(this.file.getOwner());
        this.groupName.setText(this.file.getGroup());
        if (this.file.hasOwnerReadPermission()) {
            this.ownerR.setSelected(true);
        }
        if (this.file.hasOwnerWritePermission()) {
            this.ownerW.setSelected(true);
        }
        if (this.file.hasOwnerExecutePermission()) {
            this.ownerE.setSelected(true);
        }
        if (this.file.hasGroupsReadPermission()) {
            this.groupsR.setSelected(true);
        }
        if (this.file.hasGroupsWritePermission()) {
            this.groupsW.setSelected(true);
        }
        if (this.file.hasGroupsExecutePermission()) {
            this.groupsE.setSelected(true);
        }
        if (this.file.hasOthersReadPermission()) {
            this.othersR.setSelected(true);
        }
        if (this.file.hasOthersWritePermission()) {
            this.othersW.setSelected(true);
        }
        if (this.file.hasOthersExecutePermission()) {
            this.othersE.setSelected(true);
        }
        this.appendTitle("-" + this.file.getFileName());
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.filePermission();
    }
}
