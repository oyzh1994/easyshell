package cn.oyzh.easyshell.controller.jump;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.fx.ssh.ShellSSHAuthTypeComboBox;
import cn.oyzh.easyshell.fx.key.ShellKeyComboBox;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.eclipse.jgit.internal.transport.sshd.agent.connector.PageantConnector;
import org.eclipse.jgit.internal.transport.sshd.agent.connector.UnixDomainSocketConnector;

/**
 * ssh跳板编辑业务
 *
 * @author oyzh
 * @since 2025/04/15
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "jump/shellUpdateJump.fxml"
)
public class ShellUpdateJumpController extends StageController {

    /**
     * 跳板名称
     */
    @FXML
    private ClearableTextField sshName;

    /**
     * ssh主机地址
     */
    @FXML
    private ClearableTextField sshHost;

    /**
     * ssh主机端口
     */
    @FXML
    private PortTextField sshPort;

    /**
     * ssh主机端口
     */
    @FXML
    private NumberTextField sshTimeout;

    /**
     * ssh主机用户
     */
    @FXML
    private ClearableTextField sshUser;

    /**
     * ssh主机密码
     */
    @FXML
    private PasswordTextField sshPassword;

    /**
     * ssh密钥
     */
    @FXML
    private ShellKeyComboBox sshKey;

    /**
     * ssh agent
     */
    @FXML
    private ReadOnlyTextField sshAgent;

    /**
     * ssh认证方式
     */
    @FXML
    private ShellSSHAuthTypeComboBox sshAuthMethod;

    /**
     * ssh证书
     */
    @FXML
    private ChooseFileTextField sshCertificate;

    /**
     * ssh证书密码
     */
    @FXML
    private PasswordTextField sshCertificatePwd;

    /**
     * 跳板配置
     */
    private ShellJumpConfig config;

    /**
     * 是否启用
     */
    @FXML
    private FXToggleSwitch enable;

    /**
     * forwardAgent
     */
    @FXML
    private FXCheckBox forwardAgent;

    /**
     * 获取连接地址
     *
     * @return 连接地址
     */
    private String getHost() {
        String hostText;
        if (!this.sshPort.validate() || !this.sshHost.validate()) {
            return null;
        }
        String hostIp = this.sshHost.getTextTrim();
        hostText = hostIp + ":" + this.sshPort.getValue();
        return hostText;
    }

    /**
     * 测试连接
     */
    @FXML
    private void testConnect() {
        // 检查连接地址
        String host = this.getHost();
        if (!StringUtil.isBlank(host)) {
            // 创建ssh信息
            ShellConnect shellConnect = new ShellConnect();
            shellConnect.setHost(host);
            shellConnect.setForwardAgent(this.forwardAgent.isSelected());
            // 认证信息
            shellConnect.setUser(this.sshUser.getTextTrim());
            shellConnect.setPassword(this.sshPassword.getPassword());
            shellConnect.setAuthMethod(this.sshAuthMethod.getAuthType());
            shellConnect.setCertificate(this.sshCertificate.getTextTrim());
            shellConnect.setCertificatePwd(this.sshCertificatePwd.getPassword());
            ShellConnectUtil.testConnect(this.stage, shellConnect);
        }
    }

    /**
     * 修改跳板信息
     */
    @FXML
    private void update() {
        String name = this.sshName.getTextTrim();
        if (!this.sshName.validate()) {
            return;
        }
        String userName = this.sshUser.getTextTrim();
        if (!this.sshUser.validate()) {
            return;
        }
        String password = this.sshPassword.getPassword();
        if (this.sshAuthMethod.isPasswordAuth() && StringUtil.isBlank(password)) {
            ValidatorUtil.validFail(this.sshPassword);
            return;
        }
        String certificate = this.sshCertificate.getTextTrim();
        if (this.sshAuthMethod.isCertificateAuth() && StringUtil.isBlank(certificate)) {
            ValidatorUtil.validFail(this.sshCertificate);
            return;
        }
        ShellKey key = this.sshKey.getSelectedItem();
        if (this.sshAuthMethod.isManagerAuth() && key == null) {
            ValidatorUtil.validFail(this.sshKey);
            return;
        }
        try {
            int port = this.sshPort.getIntValue();
            String host = this.sshHost.getTextTrim();
            int timeout = this.sshTimeout.getIntValue();
            String authType = this.sshAuthMethod.getAuthType();
            boolean forwardAgent = this.forwardAgent.isSelected();
            String certificatePwd = this.sshCertificatePwd.getPassword();

            this.config.setName(name);
            this.config.setPort(port);
            this.config.setHost(host);
            this.config.setUser(userName);
            this.config.setPassword(password);
            this.config.setAuthMethod(authType);
            this.config.setTimeout(timeout * 1000);
            this.config.setForwardAgent(forwardAgent);
            // 按需设置为路径或者id
            if (this.sshAuthMethod.isManagerAuth()) {
                this.config.setCertificatePath(key.getId());
                this.config.setCertificatePwd(key.getPassword());
                this.config.setCertificatePubKey(key.getPublicKey());
                this.config.setCertificatePriKey(key.getPrivateKey());
            } else {
                this.config.setCertificatePath(certificate);
                this.config.setCertificatePwd(certificatePwd);
            }
            this.config.setEnabled(this.enable.isSelected());
            // 设置数据
            this.setProp("jumpConfig", this.config);
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // ssh认证方式
        this.sshAuthMethod.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (this.sshAuthMethod.isPasswordAuth()) {
                NodeGroupUtil.display(this.stage, "password");
                NodeGroupUtil.disappear(this.stage, "sshKey");
                NodeGroupUtil.disappear(this.stage, "sshAgent");
                NodeGroupUtil.disappear(this.stage, "certificate");
            } else if (this.sshAuthMethod.isCertificateAuth()) {
                NodeGroupUtil.display(this.stage, "certificate");
                NodeGroupUtil.disappear(this.stage, "sshKey");
                NodeGroupUtil.disappear(this.stage, "password");
                NodeGroupUtil.disappear(this.stage, "sshAgent");
            } else if (this.sshAuthMethod.isSSHAgentAuth()) {
                NodeGroupUtil.display(this.stage, "sshAgent");
                NodeGroupUtil.disappear(this.stage, "sshKey");
                NodeGroupUtil.disappear(this.stage, "password");
                NodeGroupUtil.disappear(this.stage, "certificate");
            } else {
                NodeGroupUtil.display(this.stage, "sshKey");
                NodeGroupUtil.disappear(this.stage, "password");
                NodeGroupUtil.disappear(this.stage, "sshAgent");
                NodeGroupUtil.disappear(this.stage, "certificate");
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.config = this.getProp("config");
        this.sshName.setText(this.config.getName());
        this.sshHost.setText(this.config.getHost());
        this.sshUser.setText(this.config.getUser());
        this.sshPort.setValue(this.config.getPort());
        this.enable.setSelected(this.config.isEnabled());
        this.sshTimeout.setValue(this.config.getTimeoutSecond());
        this.sshPassword.setText(this.config.getPassword());
        if (this.config.isPasswordAuth()) {
            this.sshAuthMethod.selectFirst();
        } else if (this.config.isCertificateAuth()) {
            this.sshAuthMethod.select(1);
            this.sshCertificate.setText(this.config.getCertificatePath());
            this.sshCertificatePwd.setText(this.config.getCertificatePwd());
        } else if (this.config.isSSHAgentAuth()) {
            this.sshAuthMethod.select(2);
        } else if (this.config.isKeyAuth()) {
            this.sshAuthMethod.selectLast();
            this.sshKey.selectById(this.config.getCertificatePath());
        }
        this.forwardAgent.setSelected(this.config.isForwardAgent());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateJumpHost();
    }

    // /**
    //  * 选择ssh证书
    //  */
    // @FXML
    // private void chooseSSHCertificate() {
    //     File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
    //     if (file != null) {
    //         this.sshCertificate.setText(file.getPath());
    //     }
    // }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        if (OSUtil.isWindows()) {
            this.sshAgent.setText(PageantConnector.DESCRIPTOR.getIdentityAgent());
        } else {
            this.sshAgent.setText(UnixDomainSocketConnector.DESCRIPTOR.getIdentityAgent());
        }
    }
}
