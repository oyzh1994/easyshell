package cn.oyzh.easyshell.controller.connect.mosh;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.fx.key.ShellKeyComboBox;
import cn.oyzh.easyshell.fx.ssh.ShellSSHAuthTypeComboBox;
import cn.oyzh.easyshell.fx.term.ShellTermBackspaceTypeCombobox;
import cn.oyzh.easyshell.fx.term.ShellTermTypeComboBox;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
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
 * mosh连接修改业务
 *
 * @author oyzh
 * @since 2025/03/15
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/mosh/shellUpdateMoshConnect.fxml"
)
public class ShellUpdateMoshConnectController extends StageController {

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
     * 证书
     */
    @FXML
    private ChooseFileTextField certificate;

    /**
     * 证书密码
     */
    @FXML
    private PasswordTextField certificatePwd;

    /**
     * ssh agent
     */
    @FXML
    private ReadOnlyTextField sshAgent;

    /**
     * 密钥
     */
    @FXML
    private ShellKeyComboBox key;

    /**
     * tab组件
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * ssh信息
     */
    private ShellConnect shellConnect;

    /**
     * 名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 备注
     */
    @FXML
    private FXTextArea remark;

    /**
     * 连接ip
     */
    @FXML
    private ClearableTextField hostIp;

    /**
     * 连接端口
     */
    @FXML
    private PortTextField hostPort;

    /**
     * 字符集
     */
    @FXML
    private CharsetComboBox charset;

    /**
     * 终端类型
     */
    @FXML
    private ShellTermTypeComboBox termType;

    /**
     * 终端退格类型
     */
    @FXML
    private ShellTermBackspaceTypeCombobox backspaceType;

    /**
     * alt修饰
     */
    @FXML
    private FXCheckBox altSendsEscape;

    /**
     * 连接超时时间
     */
    @FXML
    private NumberTextField connectTimeOut;

    /**
     * 环境
     */
    @FXML
    private FXTextArea env;

    /**
     * 认证方式
     */
    @FXML
    private ShellSSHAuthTypeComboBox authMethod;

    /**
     * 系统类型
     */
    @FXML
    private ShellOsTypeComboBox osType;

//    /**
//     * 开启背景
//     */
//    @FXML
//    private FXToggleSwitch enableBackground;
//
//    /**
//     * 背景面板
//     */
//    @FXML
//    private FXTab backgroundTab;
//
//    /**
//     * 背景图片
//     */
//    @FXML
//    private ChooseFileTextField backgroundImage;

    /**
     * mosh key
     */
    @FXML
    private ClearableTextField moshKey;

    /**
     * ssh连接储存对象
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * 获取连接地址
     *
     * @return 连接地址
     */
    private String getHost() {
        String hostText;
        String hostIp = this.hostIp.getTextTrim();
        this.tabPane.select(0);
        if (!this.hostPort.validate()) {
            this.tabPane.select(0);
            return null;
        }
        if (!this.hostIp.validate()) {
            this.tabPane.select(0);
            return null;
        }
        hostText = hostIp + ":" + this.hostPort.getValue();
        return hostText;
    }

    /**
     * 测试连接
     */
    @FXML
    private void testConnect() {
        // 检查连接地址
        String host = this.getHost();
        if (StringUtil.isBlank(host) || StringUtil.isBlank(host.split(":")[0])) {
            //            MessageBox.warn(I18nHelper.contentCanNotEmpty());
        } else {
            int timeout = this.connectTimeOut.getIntValue();
            // 创建ssh信息
            ShellConnect shellConnect = new ShellConnect();
            shellConnect.setHost(host);
            shellConnect.setConnectTimeOut(timeout);
            shellConnect.setType(ShellPrototype.MOSH);
            shellConnect.setId(this.shellConnect.getId());
            // 认证信息
            shellConnect.setKeyId(this.key.getKeyId());
            shellConnect.setUser(this.userName.getTextTrim());
            shellConnect.setPassword(this.password.getPassword());
            shellConnect.setAuthMethod(this.authMethod.getAuthType());
            shellConnect.setCertificate(this.certificate.getTextTrim());
            shellConnect.setCertificatePwd(this.certificatePwd.getPassword());
            ShellConnectUtil.testConnect(this.stage, shellConnect, timeout * 1000);
        }
    }

    /**
     * 修改信息
     */
    @FXML
    private void update() {
        String host = this.getHost();
        if (host == null) {
            return;
        }
        String userName = this.userName.getTextTrim();
        String password = null, certificate = null, keyId = null;
        if (StringUtil.isNotBlank(userName)) {
            password = this.password.getPassword();
            if (this.authMethod.isPasswordAuth() && StringUtil.isBlank(password)) {
                ValidatorUtil.validFail(this.password);
                return;
            }
            certificate = this.certificate.getTextTrim();
            if (this.authMethod.isCertificateAuth() && StringUtil.isBlank(certificate)) {
                ValidatorUtil.validFail(this.certificate);
                return;
            }
            keyId = this.key.getKeyId();
            if (this.authMethod.isManagerAuth() && StringUtil.isBlank(keyId)) {
                ValidatorUtil.validFail(this.key);
                return;
            }
        }
//        // 检查背景配置
//        if (this.enableBackground.isSelected()) {
//            if (!this.backgroundImage.validate()) {
//                this.tabPane.select(this.backgroundTab);
//                return;
//            }
//        }
        // 名称未填，则直接以host为名称
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            String name = this.name.getTextTrim();
            String remark = this.remark.getTextTrim();
            String osType = this.osType.getSelectedItem();
            String charset = this.charset.getCharsetName();
            String termType = this.termType.getSelectedItem();
            int connectTimeOut = this.connectTimeOut.getIntValue();
//            String backgroundImage = this.backgroundImage.getText();
            int backspaceType = this.backspaceType.getSelectedIndex();
            String certificatePwd = this.certificatePwd.getPassword();
            boolean altSendsEscape = this.altSendsEscape.isSelected();
//            boolean enableBackground = this.enableBackground.isSelected();
            String moshKey = this.moshKey.getTextTrim();

            this.shellConnect.setName(name);
            this.shellConnect.setOsType(osType);
            this.shellConnect.setRemark(remark);
            this.shellConnect.setCharset(charset);
            this.shellConnect.setHost(host.trim());
            this.shellConnect.setTermType(termType);
            this.shellConnect.setBackspaceType(backspaceType);
            this.shellConnect.setAltSendsEscape(altSendsEscape);
            this.shellConnect.setConnectTimeOut(connectTimeOut);
            this.shellConnect.setEnvironment(this.env.getTextTrim());
            // mosh认证
            this.shellConnect.setMoshKey(moshKey);
            // 认证信息
            this.shellConnect.setKeyId(keyId);
            this.shellConnect.setUser(userName.trim());
            this.shellConnect.setPassword(password.trim());
            this.shellConnect.setCertificate(certificate);
            this.shellConnect.setCertificatePwd(certificatePwd);
            this.shellConnect.setAuthMethod(this.authMethod.getAuthType());
//            // 背景配置
//            this.shellConnect.setBackgroundImage(backgroundImage);
//            this.shellConnect.setEnableBackground(enableBackground);
            // 保存数据
            if (this.connectStore.replace(this.shellConnect)) {
                ShellEventUtil.connectUpdated(this.shellConnect);
                MessageBox.okToast(I18nHelper.operationSuccess());
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 连接ip处理
        this.hostIp.addTextChangeListener((observableValue, s, t1) -> {
            // 内容包含“:”，则直接切割字符为ip端口
            if (t1 != null && t1.contains(":")) {
                try {
                    this.hostIp.setText(t1.split(":")[0]);
                    this.hostPort.setValue(Integer.parseInt(t1.split(":")[1]));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // 认证方式
        this.authMethod.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (this.authMethod.isPasswordAuth()) {
                NodeGroupUtil.display(this.tabPane, "password");
                NodeGroupUtil.disappear(this.tabPane, "sshKey");
                NodeGroupUtil.disappear(this.tabPane, "sshAgent");
                NodeGroupUtil.disappear(this.tabPane, "certificate");
            } else if (this.authMethod.isCertificateAuth()) {
                NodeGroupUtil.display(this.tabPane, "certificate");
                NodeGroupUtil.disappear(this.tabPane, "sshKey");
                NodeGroupUtil.disappear(this.tabPane, "sshAgent");
                NodeGroupUtil.disappear(this.tabPane, "password");
            } else if (this.authMethod.isSSHAgentAuth()) {
                NodeGroupUtil.display(this.tabPane, "sshAgent");
                NodeGroupUtil.disappear(this.tabPane, "sshKey");
                NodeGroupUtil.disappear(this.tabPane, "password");
                NodeGroupUtil.disappear(this.tabPane, "certificate");
            } else {
                NodeGroupUtil.display(this.tabPane, "sshKey");
                NodeGroupUtil.disappear(this.tabPane, "sshAgent");
                NodeGroupUtil.disappear(this.tabPane, "password");
                NodeGroupUtil.disappear(this.tabPane, "certificate");
            }
        });
//        // 背景配置
//        this.enableBackground.selectedChanged((observable, oldValue, newValue) -> {
//            if (newValue) {
//                NodeGroupUtil.enable(this.backgroundTab, "background");
//            } else {
//                NodeGroupUtil.disable(this.backgroundTab, "background");
//            }
//        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.shellConnect = this.getProp("shellConnect");
        this.name.setText(this.shellConnect.getName());
        this.hostIp.setText(this.shellConnect.hostIp());
        this.remark.setText(this.shellConnect.getRemark());
        this.osType.selectType(this.shellConnect.getOsType());
        this.env.setText(this.shellConnect.getEnvironment());
        this.hostPort.setValue(this.shellConnect.hostPort());
        this.charset.setValue(this.shellConnect.getCharset());
        this.termType.select(this.shellConnect.getTermType());
        this.connectTimeOut.setValue(this.shellConnect.getConnectTimeOut());
        // 认证处理
        this.userName.setText(this.shellConnect.getUser());
        this.password.setText(this.shellConnect.getPassword());
        if (this.shellConnect.isPasswordAuth()) {
            this.authMethod.selectFirst();
        } else if (this.shellConnect.isCertificateAuth()) {
            this.authMethod.select(1);
            this.certificate.setText(this.shellConnect.getCertificate());
            this.certificatePwd.setText(this.shellConnect.getCertificatePwd());
        } else if (this.shellConnect.isSSHAgentAuth()) {
            this.authMethod.select(2);
        } else if (this.shellConnect.isManagerAuth()) {
            this.authMethod.selectLast();
            // 选中密钥
            this.key.selectById(this.shellConnect.getKeyId());
        }
        // 退格
        this.backspaceType.selectType(this.shellConnect.getBackspaceType());
        // alt修饰
        this.altSendsEscape.setSelected(this.shellConnect.isAltSendsEscape());
//        // 背景配置
//        this.backgroundImage.setText(this.shellConnect.getBackgroundImage());
//        this.enableBackground.setSelected(this.shellConnect.isEnableBackground());
        // mosh key
        this.moshKey.setText(this.shellConnect.getMoshKey());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        if (OSUtil.isWindows()) {
            this.sshAgent.setText(PageantConnector.DESCRIPTOR.getIdentityAgent());
        } else {
            this.sshAgent.setText(UnixDomainSocketConnector.DESCRIPTOR.getIdentityAgent());
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectUpdateTitle();
    }
}
