package cn.oyzh.easyshell.controller.connect;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.easyshell.domain.ShellSSHConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellSSHConfigStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import lombok.NonNull;

/**
 * ssh连接修改业务
 *
 * @author oyzh
 * @since 2020/9/15
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/sshUpdateConnect.fxml"
)
public class ShellUpdateConnectController extends StageController {

    /**
     * 用户名
     */
    @FXML
    private ClearableTextField userName;

    /**
     * 密码
     */
    @FXML
    private ClearableTextField password;

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
     * ssh面板
     */
    @FXML
    private FXTab sshTab;

    /**
     * 开启ssh
     */
    @FXML
    private FXToggleSwitch sshForward;

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
    private ClearableTextField sshPassword;

    /**
     * ssh连接储存对象
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * ssh连接储存对象
     */
    private final ShellSSHConfigStore sshConfigStore = ShellSSHConfigStore.INSTANCE;

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
     * 获取ssh信息
     *
     * @return ssh连接信息
     */
    private ShellSSHConfig getSSHConfig() {
        ShellSSHConfig sshConfig = new ShellSSHConfig();
        sshConfig.setIid(this.shellConnect.getId());
        sshConfig.setHost(this.sshHost.getText());
        sshConfig.setUser(this.sshUser.getText());
        sshConfig.setPort(this.sshPort.getIntValue());
        sshConfig.setPassword(this.sshPassword.getText());
        sshConfig.setTimeout(this.sshTimeout.getIntValue());
        return sshConfig;
    }

    /**
     * 获取x11配置信息
     *
     * @return x11配置信息
     */
    private ShellX11Config getX11Config() {
        ShellX11Config sshConfig = new ShellX11Config();
        sshConfig.setIid(this.shellConnect.getId());
        sshConfig.setHost(this.x11Host.getText());
        sshConfig.setPort(this.x11Port.getIntValue());
        return sshConfig;
    }

    /**
     * 测试连接
     */
    @FXML
    private void testConnect() {
        // 检查连接地址
        String host = this.getHost();
        if (StringUtil.isBlank(host) || StringUtil.isBlank(host.split(":")[0])) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
        } else {
            // 创建ssh信息
            ShellConnect shellConnect = new ShellConnect();
            shellConnect.setHost(host);
            shellConnect.setConnectTimeOut(3);
            shellConnect.setId(this.shellConnect.getId());
            shellConnect.setUser(this.userName.getTextTrim());
            shellConnect.setPassword(this.password.getTextTrim());
            shellConnect.setSshForward(this.sshForward.isSelected());
            if (shellConnect.isSSHForward()) {
                shellConnect.setSshConfig(this.getSSHConfig());
            }
            ShellConnectUtil.testConnect(this.stage, shellConnect);
        }
    }

    /**
     * 修改ssh信息
     */
    @FXML
    private void update() {
        String host = this.getHost();
        if (host == null) {
            return;
        }
        String userName = this.userName.getTextTrim();
        if (StringUtil.isBlank(userName)) {
            this.userName.requestFocus();
            return;
        }
        String password = this.password.getTextTrim();
        if (StringUtil.isBlank(password)) {
            this.password.requestFocus();
            return;
        }
        // 名称未填，则直接以host为名称
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            String name = this.name.getTextTrim();
            this.shellConnect.setName(name);
            Number connectTimeOut = this.connectTimeOut.getValue();

            this.shellConnect.setHost(host.trim());
            this.shellConnect.setUser(userName.trim());
            this.shellConnect.setPassword(password.trim());
            this.shellConnect.setRemark(this.remark.getTextTrim());
            this.shellConnect.setConnectTimeOut(connectTimeOut.intValue());
            // ssh配置
            this.shellConnect.setSshConfig(this.getSSHConfig());
            this.shellConnect.setSshForward(this.sshForward.isSelected());
            // x11配置
            this.shellConnect.setX11Config(this.getX11Config());
            this.shellConnect.setX11forwarding(this.x11forwarding.isSelected());
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
        // ssh配置
        this.sshForward.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeGroupUtil.enable(this.sshTab, "ssh");
            } else {
                NodeGroupUtil.disable(this.sshTab, "ssh");
            }
        });
    }

    @Override
    public void onWindowShown(@NonNull WindowEvent event) {
        super.onWindowShown(event);
        this.shellConnect = this.getWindowProp("sshConnect");
        this.name.setText(this.shellConnect.getName());
        this.hostIp.setText(this.shellConnect.hostIp());
        this.remark.setText(this.shellConnect.getRemark());
        this.userName.setText(this.shellConnect.getUser());
        this.hostPort.setValue(this.shellConnect.hostPort());
        this.password.setText(this.shellConnect.getPassword());
        this.connectTimeOut.setValue(this.shellConnect.getConnectTimeOut());
        this.x11forwarding.setSelected(this.shellConnect.isX11forwarding());
        // ssh配置
        this.sshForward.setSelected(this.shellConnect.isSSHForward());
        ShellSSHConfig sshConfig = this.sshConfigStore.getByIid(this.shellConnect.getId());
        if (sshConfig != null) {
            this.sshHost.setText(sshConfig.getHost());
            this.sshUser.setText(sshConfig.getUser());
            this.sshPort.setValue(sshConfig.getPort());
            this.sshTimeout.setValue(sshConfig.getTimeout());
            this.sshPassword.setText(sshConfig.getPassword());
        }
        // x11配置
        ShellX11Config x11Config = this.shellConnect.getX11Config();
        if (x11Config != null) {
            this.x11Host.setValue(x11Config.getHost());
            this.x11Port.setValue(x11Config.getPort());
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.x11Host.disableProperty().bind(this.x11forwarding.selectedProperty().not());
        this.x11Port.disableProperty().bind(this.x11forwarding.selectedProperty().not());
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectUpdateTitle();
    }

    @FXML
    private void downloadX11() {
        String url = "";
        if (OSUtil.isWindows()) {
            url = "https://sourceforge.net/projects/vcxsrv/";
        } else if (OSUtil.isMacOS()) {
            url = "https://www.xquartz.org/";
        }
        FXUtil.showDocument(url);
    }
}
