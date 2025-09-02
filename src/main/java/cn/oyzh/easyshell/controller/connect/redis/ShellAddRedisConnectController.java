package cn.oyzh.easyshell.controller.connect.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.jump.ShellAddJumpController;
import cn.oyzh.easyshell.controller.jump.ShellUpdateJumpController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.fx.jump.ShellJumpTableView;
import cn.oyzh.easyshell.fx.proxy.ShellProxyAuthTypeComboBox;
import cn.oyzh.easyshell.fx.proxy.ShellProxyProtocolComboBox;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellJumpConfigStore;
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
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * telnet连接新增业务
 *
 * @author oyzh
 * @since 2025/04/24
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/redis/shellAddRedisConnect.fxml"
)
public class ShellAddRedisConnectController extends StageController {

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
     * 执行超时时间
     */
    @FXML
    private NumberTextField executeTimOut;

    /**
     * 连接超时时间
     */
    @FXML
    private NumberTextField connectTimeOut;

    /**
     * 系统类型
     */
    @FXML
    private ShellOsTypeComboBox osType;

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
     * 分组
     */
    private ShellGroup group;

    /**
     * 跳板机配置
     */
    @FXML
    private ShellJumpTableView jumpTableView;

    /**
     * 只读模式
     */
    @FXML
    private FXCheckBox readonlyMode;

    /**
     * ssh连接储存对象
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * ssh跳板储存对象
     */
    private final ShellJumpConfigStore jumpConfigStore = ShellJumpConfigStore.INSTANCE;

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
            shellConnect.setType("Redis");
            shellConnect.setHost(host);
            shellConnect.setConnectTimeOut(3);
            // 认证信息
            shellConnect.setUser(this.userName.getTextTrim());
            shellConnect.setPassword(this.password.getPassword());
            // 跳板机配置
            shellConnect.setJumpConfigs(this.jumpTableView.getItems());
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
//        if (!this.userName.validate()) {
//            return;
//        }
        String password = this.password.getPassword();
        // 名称未填，则直接以host为名称
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            ShellConnect shellConnect = new ShellConnect();
            String name = this.name.getTextTrim();
            String remark = this.remark.getTextTrim();
            String osType = this.osType.getSelectedItem();
            int executeTimOut = this.executeTimOut.getIntValue();
            int connectTimeOut = this.connectTimeOut.getIntValue();

            shellConnect.setName(name);
            shellConnect.setOsType(osType);
            shellConnect.setRemark(remark);
            // 超时设置
            shellConnect.setExecuteTimeOut(executeTimOut);
            shellConnect.setConnectTimeOut(connectTimeOut);
            // 认证信息
            shellConnect.setUser(userName.trim());
            shellConnect.setPassword(password.trim());
            // 跳板机配置
            shellConnect.setJumpConfigs(this.jumpTableView.getItems());
            // 代理配置
            shellConnect.setProxyConfig(this.getProxyConfig());
            shellConnect.setEnableProxy(this.enableProxy.isSelected());
            // 分组及类型
            shellConnect.setType("Redis");
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
                this.proxyAuthInfoBox.enable();
            } else {
                this.proxyAuthInfoBox.disable();
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
        this.osType.select("Redis");
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
    }

    /**
     * 添加主机
     */
    @FXML
    private void addHost() {
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
        StageAdapter adapter = StageManager.parseStage(ShellAddJumpController.class);
        adapter.showAndWait();
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
        StageAdapter adapter = StageManager.parseStage(ShellUpdateJumpController.class);
        adapter.setProp("config", config);
        adapter.showAndWait();
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
        ShellJumpConfig config = this.jumpTableView.removeSelectedItem();
        this.jumpConfigStore.delete(config);
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

}
