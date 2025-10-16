package cn.oyzh.easyshell.controller.connect.ssh;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellAuthTypeComboBox;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.fx.jump.ShellJumpTableView;
import cn.oyzh.easyshell.fx.key.ShellKeyComboBox;
import cn.oyzh.easyshell.fx.proxy.ShellProxyAuthTypeComboBox;
import cn.oyzh.easyshell.fx.proxy.ShellProxyProtocolComboBox;
import cn.oyzh.easyshell.fx.term.ShellTermBackspaceTypeCombobox;
import cn.oyzh.easyshell.fx.term.ShellTermTypeComboBox;
import cn.oyzh.easyshell.fx.tunneling.ShellTunnelingTableView;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.eclipse.jgit.internal.transport.sshd.agent.connector.PageantConnector;
import org.eclipse.jgit.internal.transport.sshd.agent.connector.UnixDomainSocketConnector;

/**
 * ssh连接新增业务
 *
 * @author oyzh
 * @since 2025/03/15
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/ssh/shellAddSSHConnect.fxml"
)
public class ShellAddSSHConnectController extends StageController {

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
     * 连接超时时间
     */
    @FXML
    private NumberTextField connectTimeOut;

    /**
     * x11转发
     */
    @FXML
    private FXToggleSwitch x11forwarding;

    /**
     * x11面板
     */
    @FXML
    private FXTab x11Tab;

    /**
     * x11地址
     */
    @FXML
    private ClearableTextField x11Host;

    /**
     * x11端口
     */
    @FXML
    private PortTextField x11Port;

    /**
     * x11 cookie
     */
    @FXML
    private ClearableTextField x11Cookie;

    /**
     * x11 cookie加载按钮
     */
    @FXML
    private FXButton x11CookieBth;

    /**
     * 环境
     */
    @FXML
    private FXTextArea env;

    /**
     * 认证方式
     */
    @FXML
    private ShellAuthTypeComboBox authMethod;

    /**
     * 系统类型
     */
    @FXML
    private ShellOsTypeComboBox osType;

    /**
     * 开启背景
     */
    @FXML
    private FXToggleSwitch enableBackground;

    /**
     * 背景面板
     */
    @FXML
    private FXTab backgroundTab;

    /**
     * 背景图片
     */
    @FXML
    private ChooseFileTextField backgroundImage;

    /**
     * 分组
     */
    private ShellGroup group;

    /**
     * 开启代理
     */
    @FXML
    private FXToggleSwitch enableProxy;

    /**
     * 代理面板
     */
    @FXML
    private FXTab proxyTab;

    /**
     * 代理地址
     */
    @FXML
    private ClearableTextField proxyHost;

    /**
     * 代理端口
     */
    @FXML
    private NumberTextField proxyPort;

    /**
     * 代理信息组件
     */
    @FXML
    private FXHBox proxyAuthInfoBox;

    /**
     * 代理用户
     */
    @FXML
    private ClearableTextField proxyUser;

    /**
     * 代理密码
     */
    @FXML
    private PasswordTextField proxyPassword;

    /**
     * 代理协议
     */
    @FXML
    private ShellProxyProtocolComboBox proxyProtocol;

    /**
     * 代理认证方式
     */
    @FXML
    private ShellProxyAuthTypeComboBox proxyAuthType;

    /**
     * 跳板机配置
     */
    @FXML
    private ShellJumpTableView jumpTableView;

    /**
     * 隧道配置
     */
    @FXML
    private ShellTunnelingTableView tunnelingTableView;

    /**
     * 启用压缩
     */
    @FXML
    private FXCheckBox enableCompress;

    /**
     * 启用ZModem
     */
    @FXML
    private FXCheckBox enableZModem;

    /**
     * forwardAgent
     */
    @FXML
    private FXCheckBox forwardAgent;

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
     * 获取x11配置信息
     *
     * @return x11配置信息
     */
    private ShellX11Config getX11Config() {
        ShellX11Config config = new ShellX11Config();
        config.setHost(this.x11Host.getText());
        config.setPort(this.x11Port.getIntValue());
        config.setCookie(this.x11Cookie.getText());
        return config;
    }

    /**
     * 获取代理配置信息
     *
     * @return 代理配置信息
     */
    private ShellProxyConfig getProxyConfig() {
        ShellProxyConfig config = new ShellProxyConfig();
        config.setHost(this.proxyHost.getText());
        config.setPort(this.proxyPort.getIntValue());
        config.setUser(this.proxyUser.getTextTrim());
        config.setPassword(this.proxyPassword.getPassword());
        config.setAuthType(this.proxyAuthType.getAuthType());
        config.setProtocol(this.proxyProtocol.getSelectedItem());
        return config;
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
            // 创建ssh信息
            ShellConnect shellConnect = new ShellConnect();
            shellConnect.setType("ssh");
            shellConnect.setHost(host);
            shellConnect.setConnectTimeOut(3);
            // 认证信息
            shellConnect.setKeyId(this.key.getKeyId());
            shellConnect.setUser(this.userName.getTextTrim());
            shellConnect.setPassword(this.password.getPassword());
            shellConnect.setAuthMethod(this.authMethod.getAuthType());
            shellConnect.setCertificate(this.certificate.getTextTrim());
            shellConnect.setCertificatePwd(this.certificatePwd.getPassword());
            // 跳板机配置
            shellConnect.setJumpConfigs(this.jumpTableView.getItems());
            // 客户端转发
            shellConnect.setForwardAgent(this.forwardAgent.isSelected());
            // 代理
            shellConnect.setProxyConfig(this.getProxyConfig());
            shellConnect.setEnableProxy(this.enableProxy.isSelected());
            ShellConnectUtil.testConnect(this.stage, shellConnect);
        }
    }

    /**
     * 添加信息
     */
    @FXML
    private void add() {
        String host = this.getHost();
        if (host == null) {
            return;
        }
        String userName = this.userName.getTextTrim();
        if (!this.userName.validate()) {
            return;
        }
        String password = this.password.getPassword();
        if (this.authMethod.isPasswordAuth() && StringUtil.isBlank(password)) {
            ValidatorUtil.validFail(this.password);
            return;
        }
        String certificate = this.certificate.getTextTrim();
        if (this.authMethod.isCertificateAuth() && StringUtil.isBlank(certificate)) {
            ValidatorUtil.validFail(this.certificate);
            return;
        }
        String keyId = this.key.getKeyId();
        if (this.authMethod.isManagerAuth() && StringUtil.isBlank(keyId)) {
            ValidatorUtil.validFail(this.key);
            return;
        }
        // 检查背景配置
        if (this.x11forwarding.isSelected()) {
            if (!this.x11Host.validate() || !this.x11Port.validate()) {
                this.tabPane.select(this.x11Tab);
                return;
            }
        }
        // 检查背景配置
        if (this.enableBackground.isSelected()) {
            if (!this.backgroundImage.validate()) {
                this.tabPane.select(this.backgroundTab);
                return;
            }
        }
        // 检查代理配置
        if (this.enableProxy.isSelected()) {
            if (!this.proxyHost.validate() || !this.proxyPort.validate()) {
                this.tabPane.select(this.proxyTab);
                return;
            }
            if (!this.proxyAuthType.validate() && (!this.proxyUser.validate() || !this.proxyPassword.validate())) {
                this.tabPane.select(this.proxyTab);
                return;
            }
        }
        // 名称未填，则直接以host为名称
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            ShellConnect shellConnect = new ShellConnect();
            String name = this.name.getTextTrim();
            String remark = this.remark.getTextTrim();
            String osType = this.osType.getSelectedItem();
            String charset = this.charset.getCharsetName();
            String termType = this.termType.getSelectedItem();
            boolean forwardAgent = this.forwardAgent.isSelected();
            boolean enableZModem = this.enableZModem.isSelected();
            int connectTimeOut = this.connectTimeOut.getIntValue();
            String backgroundImage = this.backgroundImage.getText();
            int backspaceType = this.backspaceType.getSelectedIndex();
            boolean enableCompress = this.enableCompress.isSelected();
            String certificatePwd = this.certificatePwd.getPassword();
            boolean enableBackground = this.enableBackground.isSelected();

            shellConnect.setName(name);
            shellConnect.setOsType(osType);
            shellConnect.setRemark(remark);
            shellConnect.setCharset(charset);
            shellConnect.setHost(host.trim());
            shellConnect.setTermType(termType);
            shellConnect.setBackspaceType(backspaceType);
            shellConnect.setConnectTimeOut(connectTimeOut);
            shellConnect.setEnvironment(this.env.getTextTrim());
            // 客户端转发
            shellConnect.setForwardAgent(forwardAgent);
            // 启用ZModem
            shellConnect.setEnableZModem(enableZModem);
            // 启用压缩
            shellConnect.setEnableCompress(enableCompress);
            // 认证信息
            shellConnect.setKeyId(keyId);
            shellConnect.setUser(userName.trim());
            shellConnect.setPassword(password.trim());
            shellConnect.setCertificate(certificate);
            shellConnect.setCertificatePwd(certificatePwd);
            shellConnect.setAuthMethod(this.authMethod.getAuthType());
            // 跳板机配置
            shellConnect.setJumpConfigs(this.jumpTableView.getItems());
            // 隧道配置
            shellConnect.setTunnelingConfigs(this.tunnelingTableView.getItems());
            // 背景配置
            shellConnect.setBackgroundImage(backgroundImage);
            shellConnect.setEnableBackground(enableBackground);
            // x11配置
            shellConnect.setX11Config(this.getX11Config());
            shellConnect.setX11forwarding(this.x11forwarding.isSelected());
            // 代理配置
            shellConnect.setProxyConfig(this.getProxyConfig());
            shellConnect.setEnableProxy(this.enableProxy.isSelected());
            // 分组及类型
            shellConnect.setType("ssh");
            shellConnect.setGroupId(this.group == null ? null : this.group.getGid());
            // 保存数据
            if (this.connectStore.replace(shellConnect)) {
                ShellEventUtil.connectAdded(shellConnect);
                MessageBox.okToast(I18nHelper.operationSuccess());
                this.setProp("connect", shellConnect);
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
        // 背景配置
        this.enableBackground.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeGroupUtil.enable(this.backgroundTab, "background");
            } else {
                NodeGroupUtil.disable(this.backgroundTab, "background");
            }
        });
        // 代理配置
        this.enableProxy.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeGroupUtil.enable(this.proxyTab, "proxy");
                if (this.proxyAuthType.isPasswordAuth()) {
                    this.proxyAuthInfoBox.enable();
                } else {
                    this.proxyAuthInfoBox.disable();
                }
            } else {
                NodeGroupUtil.disable(this.proxyTab, "proxy");
            }
        });
        // 代理认证配置
        this.proxyAuthType.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (this.proxyAuthType.isPasswordAuth()) {
                if (this.enableProxy.isSelected()) {
                    this.proxyAuthInfoBox.enable();
                }
            } else {
                this.proxyAuthInfoBox.disable();
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
        // linux隐藏x11
        if (OSUtil.isLinux()) {
            NodeGroupUtil.disappear(this.getStage(), "x11");
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.x11Host.disableProperty().bind(this.x11forwarding.selectedProperty().not());
        this.x11Port.disableProperty().bind(this.x11forwarding.selectedProperty().not());
        this.x11Cookie.disableProperty().bind(this.x11forwarding.selectedProperty().not());
        this.x11CookieBth.disableProperty().bind(this.x11forwarding.selectedProperty().not());
        if (OSUtil.isWindows()) {
            this.sshAgent.setText(PageantConnector.DESCRIPTOR.getIdentityAgent());
        } else {
            this.sshAgent.setText(UnixDomainSocketConnector.DESCRIPTOR.getIdentityAgent());
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
    }

    /**
     * 下载或者开启x11服务
     */
    @FXML
    private void downloadX11() {
        try {
            String url = "";
            if (OSUtil.isWindows()) {
                url = "https://sourceforge.net/projects/vcxsrv/";
            } else if (OSUtil.isMacOS()) {
                url = "https://www.xquartz.org/";
            } else if (OSUtil.isLinux()) {
                url = this.getClass().getResource("/doc/enable_x11.txt").toExternalForm();
            }
            FXUtil.showDocument(url);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 加载x11的cookie
     */
    @FXML
    private void loadX11Cookie() {
        if (OSUtil.isLinux()) {
            String str = RuntimeUtil.execForStr("xauth list");
            if (StringUtil.isNotBlank(str)) {
                String[] arr = str.split("\n")[0].split("\\s+");
                this.x11Cookie.setText(ArrayUtil.last(arr));
            } else {
                this.x11Cookie.clear();
            }
        }
    }

    ///**
    // * 选择证书
    // */
    //@FXML
    // private void chooseCertificate() {
    //    File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
    //    if (file != null) {
    //        this.certificate.setText(file.getPath());
    //    }
    //}

    // /**
    //  * 选择背景图片
    //  */
    // @FXML
    // private void chooseBackgroundImage() {
    //     File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), new FileExtensionFilter("Types", "*.jpeg", "*.jpg", "*.png", "*.gif"));
    //     if (file != null) {
    //         this.backgroundImage.setText(file.getPath());
    //     }
    // }

    /**
     * 添加主机
     */
    @FXML
    private void addHost() {
        // StageAdapter adapter = StageManager.parseStage(ShellAddHostController.class);
        // adapter.showAndWait();
        StageAdapter adapter = ShellViewFactory.addHost(null);
        if (adapter == null) {
            return;
        }
        ShellJumpConfig jumpConfig = adapter.getProp("jumpConfig");
        if (jumpConfig != null) {
            this.jumpTableView.addItem(jumpConfig);
            this.jumpTableView.updateOrder();
        }
    }

    /**
     * 添加跳板
     */
    @FXML
    private void addJump() {
        StageAdapter adapter = ShellViewFactory.addJump();
        if (adapter == null) {
            return;
        }
        ShellJumpConfig jumpConfig = adapter.getProp("jumpConfig");
        if (jumpConfig != null) {
            this.jumpTableView.addItem(jumpConfig);
            this.jumpTableView.updateOrder();
        }
    }

    /**
     * 编辑跳板
     */
    @FXML
    private void updateJump() {
        ShellJumpConfig config = this.jumpTableView.getSelectedItem();
        if (config == null) {
            return;
        }
        StageAdapter adapter = ShellViewFactory.updateJump(config);
        if (adapter == null) {
            return;
        }
        ShellJumpConfig jumpConfig = adapter.getProp("jumpConfig");
        if (jumpConfig != null) {
            this.jumpTableView.refresh();
            this.jumpTableView.updateOrder();
        }
    }

    /**
     * 删除跳板
     */
    @FXML
    private void deleteJump() {
        this.jumpTableView.removeSelectedItem();
        this.jumpTableView.updateOrder();
    }

    /**
     * 上移跳板
     */
    @FXML
    private void moveJumpUp() {
        TableViewUtil.moveUp(this.jumpTableView);
        this.jumpTableView.refresh();
        this.jumpTableView.updateOrder();
    }

    /**
     * 下移跳板
     */
    @FXML
    private void moveJumpDown() {
        TableViewUtil.moveDown(this.jumpTableView);
        this.jumpTableView.refresh();
        this.jumpTableView.updateOrder();
    }

    /**
     * 添加隧道
     */
    @FXML
    private void addTunneling() {
        StageAdapter adapter = ShellViewFactory.addTunneling();
        if (adapter == null) {
            return;
        }
        ShellTunnelingConfig tunnelingConfig = adapter.getProp("tunnelingConfig");
        if (tunnelingConfig != null) {
            this.tunnelingTableView.addItem(tunnelingConfig);
        }
    }

    /**
     * 编辑隧道
     */
    @FXML
    private void updateTunneling() {
        ShellTunnelingConfig config = this.tunnelingTableView.getSelectedItem();
        if (config == null) {
            return;
        }
        StageAdapter adapter = ShellViewFactory.updateTunneling(config);
        if (adapter == null) {
            return;
        }
        ShellTunnelingConfig tunnelingConfig = adapter.getProp("tunnelingConfig");
        if (tunnelingConfig != null) {
            this.tunnelingTableView.refresh();
        }
    }

    /**
     * 删除隧道
     */
    @FXML
    private void deleteTunneling() {
        this.tunnelingTableView.removeSelectedItem();
    }
}
