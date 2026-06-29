package cn.oyzh.easyshell.controller.connect.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.event.mongo.MongoEventUtil;
import cn.oyzh.easyshell.fx.jump.ShellJumpTableView;
import cn.oyzh.easyshell.fx.proxy.ShellProxyAuthTypeComboBox;
import cn.oyzh.easyshell.fx.proxy.ShellProxyProtocolComboBox;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.internal.ShellPrototype;
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
 * MongoDB 连接新增业务
 *
 * @author oyzh
 * @since 2023/12/22
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "connect/mongo/mongoConnectAdd.fxml"
)
public class MongoConnectAddController extends StageController {

    @FXML
    private FXTabPane tabPane;

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

    private ShellGroup group;

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
        ShellProxyConfig config = new ShellProxyConfig();
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
            ShellConnect shellConnect = new ShellConnect();
            shellConnect.setHost(host);
            shellConnect.setConnectTimeOut(3);
            shellConnect.setType(ShellPrototype.MONGO);
            shellConnect.setUser(this.user.getText());
//            shellConnect.setMongoAuthType(this.authMethod.getType());
            shellConnect.setMongoAuthDatabase(this.authDatabase.getTextTrim());
            shellConnect.setPassword(this.password.getPassword());
            ShellConnectUtil.testConnect(this.stage, shellConnect);
        }
    }

    @FXML
    private void add() {
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

            ShellConnect shellConnect = new ShellConnect();
            shellConnect.setName(name);
            shellConnect.setHost(host);
//            shellConnect.setMongoAuthType(authType);
            shellConnect.setType(ShellPrototype.MONGO);
            shellConnect.setMongoAuthDatabase(authDatabase);
            shellConnect.setUser(this.user.getText());
            shellConnect.setRemark(this.remark.getTextTrim());
            shellConnect.setPassword(this.password.getPassword());
            shellConnect.setGroupId(this.group == null ? null : this.group.getGid());
            shellConnect.setConnectTimeOut(connectTimeOut == null ? 5 : connectTimeOut.intValue());
            shellConnect.setReadonly(this.readonly.isSelected());
            // 代理配置
            shellConnect.setEnableProxy(this.enableProxy.isSelected());
            shellConnect.setProxyConfig(this.getProxyConfig());
            // 跳板机配置
            shellConnect.setJumpConfigs(this.jumpTableView.getItems());
            boolean result = this.connectStore.replace(shellConnect);
            if (result) {
                ShellEventUtil.connectAdded(shellConnect);
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
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
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
