package cn.oyzh.easyshell.controller.connect;

import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.internal.ShellPrototype;
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
        stageStyle = FXStageStyle.EXTENDED,
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
            if (ShellPrototype.SSH.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSSHConnect(group);
            } else if (ShellPrototype.LOCAL.equalsIgnoreCase(userData)) {
                ShellViewFactory.addLocalConnect(group);
            } else if (ShellPrototype.TELNET.equalsIgnoreCase(userData)) {
                ShellViewFactory.addTelnetConnect(group);
            } else if (ShellPrototype.SFTP.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSFTPConnect(group);
            } else if (ShellPrototype.FTP.equalsIgnoreCase(userData)) {
                ShellViewFactory.addFTPConnect(group);
            } else if (ShellPrototype.S3.equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, null);
            } else if ("s3_cos".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "cos");
            } else if ("s3_obs".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "obs");
            } else if ("s3_oss".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "oss");
            } else if ("s3_minio".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "minio");
            } else if (ShellPrototype.SERIAL.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSerialConnect(group);
            } else if (ShellPrototype.VNC.equalsIgnoreCase(userData)) {
                ShellViewFactory.addVNCConnect(group);
            } else if (ShellPrototype.RLOGIN.equalsIgnoreCase(userData)) {
                ShellViewFactory.addRLoginConnect(group);
            } else if (ShellPrototype.SMB.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSMBConnect(group);
            } else if (ShellPrototype.REDIS.equalsIgnoreCase(userData)) {
                ShellViewFactory.addRedisConnect(group);
            } else if (ShellPrototype.ZOOKEEPER.equalsIgnoreCase(userData)) {
                ShellViewFactory.addZKConnect(group);
            } else if (ShellPrototype.RDP.equalsIgnoreCase(userData)) {
                ShellViewFactory.addRDPConnect(group);
            } else if (ShellPrototype.WEBDAV.equalsIgnoreCase(userData)) {
                ShellViewFactory.addWebdavConnect(group);
            } else if (ShellPrototype.MYSQL.equalsIgnoreCase(userData)) {
                ShellViewFactory.addMysqlConnect(group);
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
