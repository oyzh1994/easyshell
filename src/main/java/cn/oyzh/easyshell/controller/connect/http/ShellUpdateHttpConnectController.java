package cn.oyzh.easyshell.controller.connect.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.proxy.ShellProxyAuthTypeComboBox;
import cn.oyzh.easyshell.fx.proxy.ShellProxyProtocolComboBox;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 修改 HTTP 工作区。
 *
 * @author oyzh
 * @since 2026-11-07
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/http/shellUpdateHttpConnect.fxml"
)
public class ShellUpdateHttpConnectController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 连接超时
     */
    @FXML
    private NumberTextField connectTimeOut;

    /**
     * 备注
     */
    @FXML
    private FXTextArea remark;

    /**
     * 主配置标签页
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 启用代理
     */
    @FXML
    private FXToggleSwitch enableProxy;

    /**
     * 代理配置页
     */
    @FXML
    private FXTab proxyTab;

    /**
     * 代理协议
     */
    @FXML
    private ShellProxyProtocolComboBox proxyProtocol;

    /**
     * 代理主机
     */
    @FXML
    private ClearableTextField proxyHost;

    /**
     * 代理端口
     */
    @FXML
    private NumberTextField proxyPort;

    /**
     * 代理认证方式
     */
    @FXML
    private ShellProxyAuthTypeComboBox proxyAuthType;

    /**
     * 代理认证信息
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
     * 连接
     */
    private ShellConnect shellConnect;

    /**
     * 连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.shellConnect = this.getProp("shellConnect");
        this.proxyProtocol.getItems().setAll("HTTP");
        this.proxyProtocol.select("HTTP");
        this.name.setText(this.shellConnect.getName());
        this.connectTimeOut.setValue(this.shellConnect.getConnectTimeOut());
        this.remark.setText(this.shellConnect.getRemark());
        this.enableProxy.setSelected(this.shellConnect.isEnableProxy());
        ShellProxyConfig proxyConfig = this.shellConnect.getProxyConfig();
        if (proxyConfig != null) {
            this.proxyHost.setValue(proxyConfig.getHost());
            this.proxyPort.setValue(proxyConfig.getPort());
            this.proxyAuthType.select(proxyConfig.isPasswordAuth() ? 1 : 0);
            this.proxyUser.setValue(proxyConfig.getUser());
            this.proxyPassword.setValue(proxyConfig.getPassword());
        }
        this.stage.hideOnEscape();
    }

    @FXML
    private void update() {
        String nameText = this.name.getTextTrim();
        if (StringUtil.isBlank(nameText)) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty(), this.getWindow());
            return;
        }
        this.shellConnect.setName(nameText);
        // HTTP 连接不再配置基础地址，请求使用完整 URL 或环境变量
        this.shellConnect.setHost(null);
        if (!this.validateProxy()) {
            return;
        }
        this.shellConnect.setConnectTimeOut(this.connectTimeOut.getIntValue());
        this.shellConnect.setRemark(this.remark.getTextTrim());
        this.shellConnect.setType(ShellPrototype.HTTP);
        this.shellConnect.setOsType(ShellPrototype.HTTP);
        this.shellConnect.setProxyConfig(this.getProxyConfig());
        this.shellConnect.setEnableProxy(this.enableProxy.isSelected());
        if (this.connectStore.replace(this.shellConnect)) {
            ShellEventUtil.connectUpdated(this.shellConnect);
            this.closeWindow();
        } else {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }

    /**
     * 获取代理配置
     *
     * @return 代理配置
     */
    private ShellProxyConfig getProxyConfig() {
        ShellProxyConfig proxyConfig = this.shellConnect.getProxyConfig();
        if (proxyConfig == null) {
            proxyConfig = new ShellProxyConfig();
            proxyConfig.setIid(this.shellConnect.getId());
        }
        proxyConfig.setHost(this.proxyHost.getTextTrim());
        proxyConfig.setPort(this.proxyPort.getIntValue());
        proxyConfig.setProtocol(this.proxyProtocol.getSelectedItem());
        proxyConfig.setAuthType(this.proxyAuthType.getAuthType());
        proxyConfig.setUser(this.proxyUser.getTextTrim());
        proxyConfig.setPassword(this.proxyPassword.getPassword());
        return proxyConfig;
    }

    /**
     * 校验代理配置
     *
     * @return 是否有效
     */
    private boolean validateProxy() {
        if (!this.enableProxy.isSelected()) {
            return true;
        }
        if (!this.proxyHost.validate() || !this.proxyPort.validate()) {
            this.tabPane.select(this.proxyTab);
            return false;
        }
        if (this.proxyAuthType.isPasswordAuth()
                && (!this.proxyUser.validate() || !this.proxyPassword.validate())) {
            this.tabPane.select(this.proxyTab);
            return false;
        }
        return true;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
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
        this.proxyAuthType.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (this.proxyAuthInfoBox == null) {
                return;
            }
            if (this.enableProxy.isSelected() && this.proxyAuthType.isPasswordAuth()) {
                this.proxyAuthInfoBox.enable();
            } else {
                this.proxyAuthInfoBox.disable();
            }
        });
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectUpdateTitle();
    }
}
