package cn.oyzh.easyshell.controller.connect.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.jump.ShellJumpTableView;
import cn.oyzh.easyshell.fx.proxy.ShellProxyAuthTypeComboBox;
import cn.oyzh.easyshell.fx.proxy.ShellProxyProtocolComboBox;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * MongoDB 连接修改业务
 *
 * @author oyzh
 * @since 2023/12/22
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "connect/mongo/mongoConnectUpdate.fxml"
)
public class MongoConnectUpdateController extends StageController {

    @FXML
    private FXTabPane tabPane;

    private ShellConnect mongoConnect;

    @FXML
    private ClearableTextField name;

    @FXML
    private ClearableTextField user;

    @FXML
    private PasswordTextField password;

    @FXML
    private FXTextArea remark;

    @FXML
    private ClearableTextField hostIp;

    @FXML
    private PortTextField hostPort;

//    @FXML
//    private MonogoAuthMethodComboBox authMethod;

    @FXML
    private ClearableTextField authDatabase;

    @FXML
    private NumberTextField connectTimeOut;

    @FXML
    private FXCheckBox readonly;

    // 代理
    @FXML
    private FXTab proxyTab;

    @FXML
    private FXToggleSwitch enableProxy;

    @FXML
    private ShellProxyProtocolComboBox proxyProtocol;

    @FXML
    private ClearableTextField proxyHost;

    @FXML
    private PortTextField proxyPort;

    @FXML
    private ShellProxyAuthTypeComboBox proxyAuthType;

    @FXML
    private FXHBox proxyAuthInfoBox;

    @FXML
    private ClearableTextField proxyUser;

    @FXML
    private PasswordTextField proxyPassword;

    // 跳板机
    @FXML
    private ShellJumpTableView jumpTableView;

    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    private String getHost() {
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
        return hostIp + ":" + this.hostPort.getValue();
    }

    private ShellProxyConfig getProxyConfig() {
        ShellProxyConfig config = this.mongoConnect.getProxyConfig();
        if (config == null) {
            config = new ShellProxyConfig();
            config.setIid(this.mongoConnect.getId());
        }
        config.setHost(this.proxyHost.getText());
        config.setPort(this.proxyPort.getIntValue());
        config.setUser(this.proxyUser.getTextTrim());
        config.setPassword(this.proxyPassword.getPassword());
        config.setAuthType(this.proxyAuthType.getAuthType());
        config.setProtocol(this.proxyProtocol.getSelectedItem());
        return config;
    }

    @FXML
    private void testConnect() {
        String host = this.getHost();
        if (StringUtil.isBlank(host) || StringUtil.isBlank(host.split(":")[0])) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
        } else {
            ShellConnect testConnect = new ShellConnect();
            testConnect.setHost(host);
            testConnect.setConnectTimeOut(3);
            testConnect.setId(this.mongoConnect.getId());
//            testConnect.setMongoAuthType(this.authMethod.getType());
            testConnect.setMongoAuthDatabase(this.authDatabase.getTextTrim());
            testConnect.setUser(this.user.getText());
            testConnect.setPassword(this.password.getPassword());
            ShellConnectUtil.testConnect(this.stage, testConnect);
        }
    }

    @FXML
    private void update() {
        String host = this.getHost();
        if (host == null) {
            return;
        }
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            String name = this.name.getTextTrim();
//            String authType = this.authMethod.getType();
            String authDatabase = this.authDatabase.getTextTrim();
            Number connectTimeOut = this.connectTimeOut.getValue();

            this.mongoConnect.setName(name);
            this.mongoConnect.setHost(host.trim());
//            this.mongoConnect.setMongoAuthType(authType);
            this.mongoConnect.setMongoAuthDatabase(authDatabase);
            this.mongoConnect.setUser(this.user.getText());
            this.mongoConnect.setRemark(this.remark.getTextTrim());
            this.mongoConnect.setPassword(this.password.getPassword());
            this.mongoConnect.setConnectTimeOut(connectTimeOut == null ? 5 : connectTimeOut.intValue());
            this.mongoConnect.setReadonly(this.readonly.isSelected());
            // 代理
            this.mongoConnect.setEnableProxy(this.enableProxy.isSelected());
            this.mongoConnect.setProxyConfig(this.getProxyConfig());
            // 跳板机
            this.mongoConnect.setJumpConfigs(this.jumpTableView.getItems());

            if (this.connectStore.replace(this.mongoConnect)) {
                ShellEventUtil.connectUpdated(this.mongoConnect);
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
        this.hostIp.addTextChangeListener((observableValue, s, t1) -> {
            if (t1 != null && t1.contains(":")) {
                try {
                    this.hostIp.setText(t1.split(":")[0]);
                    this.hostPort.setValue(Integer.parseInt(t1.split(":")[1]));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // 代理
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
        this.mongoConnect = this.getProp("shellConnect");
        this.name.setText(this.mongoConnect.getName());
        this.user.setText(this.mongoConnect.getUser());
        this.hostIp.setText(this.mongoConnect.hostIp());
        this.remark.setText(this.mongoConnect.getRemark());
        this.hostPort.setValue(this.mongoConnect.hostPort());
        this.password.setText(this.mongoConnect.getPassword());
        this.connectTimeOut.setValue(this.mongoConnect.getConnectTimeOut());
        this.readonly.setSelected(this.mongoConnect.isReadonly());
        // Auth
//        this.authMethod.select(this.mongoConnect.getMongoAuthType());
        this.authDatabase.setText(this.mongoConnect.getMongoAuthDatabase());
        // 代理
        this.enableProxy.setSelected(this.mongoConnect.isEnableProxy());
        ShellProxyConfig proxyConfig = this.mongoConnect.getProxyConfig();
        if (proxyConfig != null) {
            this.proxyHost.setValue(proxyConfig.getHost());
            this.proxyPort.setValue(proxyConfig.getPort());
            this.proxyUser.setValue(proxyConfig.getUser());
            this.proxyProtocol.select(proxyConfig.getProtocol());
            this.proxyPassword.setValue(proxyConfig.getPassword());
            if (proxyConfig.isPasswordAuth()) {
                this.proxyAuthType.select(1);
            }
        }
        // 跳板机
        this.jumpTableView.setItem(this.mongoConnect.getJumpConfigs());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectUpdateTitle();
    }

    @FXML
    private void addHost() {
        StageAdapter adapter = ShellViewFactory.addHost(null);
        if (adapter == null) return;
        ShellJumpConfig jumpConfig = adapter.getProp("jumpConfig");
        if (jumpConfig != null) {
            this.jumpTableView.addItem(jumpConfig);
            this.jumpTableView.updateOrder();
        }
    }

    @FXML
    private void addJump() {
        StageAdapter adapter = ShellViewFactory.addJump();
        if (adapter == null) return;
        ShellJumpConfig jumpConfig = adapter.getProp("jumpConfig");
        if (jumpConfig != null) {
            this.jumpTableView.addItem(jumpConfig);
            this.jumpTableView.updateOrder();
        }
    }

    @FXML
    private void updateJump() {
        ShellJumpConfig config = this.jumpTableView.getSelectedItem();
        if (config == null) return;
        StageAdapter adapter = ShellViewFactory.updateJump(config);
        if (adapter == null) return;
        ShellJumpConfig jumpConfig = adapter.getProp("jumpConfig");
        if (jumpConfig != null) {
            this.jumpTableView.refresh();
            this.jumpTableView.updateOrder();
        }
    }

    @FXML
    private void deleteJump() {
        ShellJumpConfig config = this.jumpTableView.getSelectedItem();
        if (MessageBox.confirm(I18nHelper.deleteJumpHost() + " " + config.getName() + " ?")) {
            this.jumpTableView.removeSelectedItem();
            this.jumpTableView.updateOrder();
        }
    }

    @FXML
    private void moveJumpUp() {
        TableViewUtil.moveUp(this.jumpTableView);
        this.jumpTableView.refresh();
        this.jumpTableView.updateOrder();
    }

    @FXML
    private void moveJumpDown() {
        TableViewUtil.moveDown(this.jumpTableView);
        this.jumpTableView.refresh();
        this.jumpTableView.updateOrder();
    }
}
