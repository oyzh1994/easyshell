package cn.oyzh.easyshell.controller.ssh;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.fx.key.ShellKeyComboBox;
import cn.oyzh.easyshell.fx.ssh.ShellSSHAuthTypeComboBox2;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * ssh认证业务
 *
 * @author oyzh
 * @since 2026/02/10
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "ssh/shellSSHAuth.fxml"
)
public class ShellSSHAuthController extends StageController {

    /**
     * 用户名
     */
    @FXML
    private ClearableTextField userName;

    /**
     * 密码
     */
    @FXML
    private PasswordTextField password;

    /**
     * 证书密码
     */
    @FXML
    private PasswordTextField certPwd;

    /**
     * 证书
     */
    @FXML
    private ChooseFileTextField certKey;

    /**
     * ssh认证类型
     */
    @FXML
    private ShellSSHAuthTypeComboBox2 authType;

    /**
     * 记住
     */
    @FXML
    private FXCheckBox remember;

    /**
     * ssh密钥
     */
    @FXML
    private ShellKeyComboBox sshKey;

    /**
     * 连接
     */
    private ShellConnect connect;

    /**
     * 连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * 执行认证
     */
    @FXML
    private void doAuth() {
        try {

            // 用户名
            String userName = this.userName.getTextTrim();

            String authType = this.authType.getAuthType();
            String password = null;
            String certKey = null;
            String certPwd = null;
            ShellKey sshKey = null;

            // 密码认证
            if (this.authType.isPasswordAuth()) {
                password = this.password.getPassword();
                if (StringUtil.isBlank(password)) {
                    this.password.validate();
                    return;
                }
            } else if (this.authType.isCertificateAuth()) {// 证书认证
                if (this.certKey.getFile() == null) {
                    this.certKey.validate();
                    return;
                }
                certKey = this.certKey.getTextTrim();
                certPwd = this.certPwd.getPassword();
            } else if (this.authType.isManagerAuth()) {// 密钥认证
                sshKey = this.sshKey.getSelectedItem();
                if (sshKey == null) {
                    this.sshKey.validate();
                    return;
                }
            }
            // 记住
            if (this.remember.isSelected()) {
                this.assembleInfo(this.connect, certKey, certPwd, password, userName, authType, sshKey);
                this.connectStore.update(this.connect);
                this.setProp("connect", this.connect);
            } else {
                ShellConnect connect = new ShellConnect();
                connect.copy(this.connect);
                this.assembleInfo(connect, certKey, certPwd, password, userName, authType, sshKey);
                this.setProp("connect", connect);
            }
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 组装信息
     *
     * @param connect  连接
     * @param certKey  证书
     * @param certPwd  证书密码
     * @param password 密码
     * @param userName 用户名
     * @param authType 认证类型
     * @param sshKey   ssh密钥
     */
    private void assembleInfo(ShellConnect connect,
                              String certKey,
                              String certPwd,
                              String password,
                              String userName,
                              String authType,
                              ShellKey sshKey) {
        if (StringUtil.isNotBlank(certKey)) {
            connect.setCertificate(certKey);
        }
        if (StringUtil.isNotBlank(certPwd)) {
            connect.setCertificatePwd(certPwd);
        }
        if (StringUtil.isNotBlank(password)) {
            connect.setPassword(password);
        }
        if (StringUtil.isNotBlank(userName)) {
            connect.setUser(userName);
        }
        if (sshKey != null) {
            connect.setKeyId(sshKey.getId());
        }
        connect.setAuthMethod(authType);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.authType.selectedIndexChanged((observableValue, number, t1) -> {
            if (this.authType.isPasswordAuth()) {
                NodeGroupUtil.display(this.stage, "password");
                NodeGroupUtil.disappear(this.stage, "sshKey");
                NodeGroupUtil.disappear(this.stage, "certificate");
            } else if (this.authType.isCertificateAuth()) {
                NodeGroupUtil.display(this.stage, "certificate");
                NodeGroupUtil.disappear(this.stage, "sshKey");
                NodeGroupUtil.disappear(this.stage, "password");
            } else if (this.authType.isManagerAuth()) {
                NodeGroupUtil.display(this.stage, "sshKey");
                NodeGroupUtil.disappear(this.stage, "password");
                NodeGroupUtil.disappear(this.stage, "certificate");
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.connect = this.getProp("connect");
        this.userName.setText(this.connect.getUser());
        // 默认清除
        this.removeProp("connect");
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.auth();
    }
}
