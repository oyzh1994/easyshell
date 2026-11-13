package cn.oyzh.easyshell.controller.connect.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
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
 * 新增 HTTP 工作区。
 *
 * @author oyzh
 * @since 2026-11-07
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/http/shellAddHttpConnect.fxml"
)
public class ShellAddHttpConnectController extends StageController {

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
     * 分组
     */
    private ShellGroup group;

    /**
     * 连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
        this.proxyProtocol.getItems().setAll("HTTP");
        this.proxyProtocol.select("HTTP");
        this.connectTimeOut.setValue(30);
        this.stage.hideOnEscape();
    }

    @FXML
    private void add() {
        if (!this.validateProxy()) {
            return;
        }
        ShellConnect connect = this.buildConnect();
        if (connect == null) {
            return;
        }
        connect.setGroupId(this.group == null ? null : this.group.getGid());
        if (this.connectStore.insert(connect)) {
            ShellEventUtil.connectAdded(connect);
            this.setProp("connect", connect);
            this.closeWindow();
        } else {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }

    /**
     * 构建连接
     *
     * @return 连接
     */
    private ShellConnect buildConnect() {
        String nameText = this.name.getTextTrim();
        if (StringUtil.isBlank(nameText)) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty(), this.getWindow());
            return null;
        }
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.HTTP);
        connect.setOsType(ShellPrototype.HTTP);
        connect.setName(nameText);
        connect.setConnectTimeOut(this.connectTimeOut.getIntValue());
        connect.setRemark(this.remark.getTextTrim());
        connect.setProxyConfig(this.getProxyConfig());
        connect.setEnableProxy(this.enableProxy.isSelected());
        return connect;
    }

    /**
     * 获取代理配置
     *
     * @return 代理配置
     */
    private ShellProxyConfig getProxyConfig() {
        ShellProxyConfig proxyConfig = new ShellProxyConfig();
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
        return I18nHelper.connectAddTitle();
    }
}
