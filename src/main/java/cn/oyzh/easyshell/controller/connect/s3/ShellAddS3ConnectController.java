package cn.oyzh.easyshell.controller.connect.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.fx.proxy.ShellProxyAuthTypeComboBox;
import cn.oyzh.easyshell.fx.proxy.ShellProxyProtocolComboBox;
import cn.oyzh.easyshell.fx.s3.ShellS3RegionTextField;
import cn.oyzh.easyshell.fx.s3.ShellS3TypeCombobox;
import cn.oyzh.easyshell.s3.ShellS3Util;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
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
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import software.amazon.awssdk.regions.Region;

/**
 * s3连接新增业务
 *
 * @author oyzh
 * @since 2025/05/23
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/s3/shellAddS3Connect.fxml"
)
public class ShellAddS3ConnectController extends StageController {

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
     * appid
     */
    @FXML
    private ClearableTextField appId;

    /**
     * 类型
     */
    @FXML
    private ShellS3TypeCombobox type;

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
     * 连接地址
     */
    @FXML
    private ClearableTextField host;

    /**
     * 字符集
     */
    @FXML
    private CharsetComboBox charset;

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
     * 区域
     */
    @FXML
    private ShellS3RegionTextField region;

    /**
     * 分组
     */
    private ShellGroup group;

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
        this.tabPane.select(0);
        if (!this.host.validate()) {
            this.tabPane.select(0);
            return null;
        }
        return this.host.getTextTrim();
    }

    /**
     * 获取代理配置信息
     *
     * @return 代理配置信息
     */
    private ShellProxyConfig getProxyConfig() {
        ShellProxyConfig proxyConfig = new ShellProxyConfig();
        proxyConfig.setHost(this.proxyHost.getText());
        proxyConfig.setPort(this.proxyPort.getIntValue());
        proxyConfig.setUser(this.proxyUser.getTextTrim());
        proxyConfig.setPassword(this.proxyPassword.getPassword());
        proxyConfig.setAuthType(this.proxyAuthType.getAuthType());
        proxyConfig.setProtocol(this.proxyProtocol.getSelectedItem());
        return proxyConfig;
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
            shellConnect.setType("s3");
            shellConnect.setHost(host);
            shellConnect.setConnectTimeOut(3);
            // 认证信息
            shellConnect.setUser(this.userName.getTextTrim());
            shellConnect.setPassword(this.password.getPassword());
            // 代理
            shellConnect.setProxyConfig(this.getProxyConfig());
            shellConnect.setEnableProxy(this.enableProxy.isSelected());
            // s3独有
            shellConnect.setS3Type(this.type.getType());
            shellConnect.setRegion(this.region.getText());
            shellConnect.setS3AppId(this.appId.getTextTrim());
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
        // 名称未填，则直接以host为名称
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            ShellConnect shellConnect = new ShellConnect();
            String type = this.type.getType();
            String name = this.name.getTextTrim();
            String region = this.region.getText();
            String appId = this.appId.getTextTrim();
            String remark = this.remark.getTextTrim();
            String osType = this.osType.getSelectedItem();
            String charset = this.charset.getCharsetName();
            int connectTimeOut = this.connectTimeOut.getIntValue();

            shellConnect.setName(name);
            shellConnect.setOsType(osType);
            shellConnect.setRemark(remark);
            shellConnect.setCharset(charset);
            shellConnect.setHost(host.trim());
            shellConnect.setConnectTimeOut(connectTimeOut);
            // 认证信息
            shellConnect.setUser(userName.trim());
            shellConnect.setPassword(password.trim());
            // 代理配置
            shellConnect.setProxyConfig(this.getProxyConfig());
            shellConnect.setEnableProxy(this.enableProxy.isSelected());
            // s3独有
            shellConnect.setS3Type(type);
            shellConnect.setRegion(region);
            shellConnect.setS3AppId(appId);
            // 分组及类型
            shellConnect.setType("s3");
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
        this.host.addTextChangeListener((observableValue, s, t1) -> {
            // 处理区域
            if (t1 != null && t1.contains(":")) {
                try {
                    String region = ShellS3Util.parseRegion(t1);
                    if (region != null) {
                        this.region.setText(region);
                    }
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
        String s3Type = this.getProp("s3Type");
        this.initS3Type(s3Type);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    /**
     * 初始化s3类型
     *
     * @param s3Type s3类型
     */
    private void initS3Type(String s3Type) {
        if (StringUtil.isBlank(s3Type) || StringUtil.equalsIgnoreCase(s3Type, "s3")) {
            this.type.select("S3");
            this.osType.select("S3");
        } else if (StringUtil.equalsIgnoreCase(s3Type, "Minio")) {
            this.type.select("Minio");
            this.osType.select("Minio");
            this.region.select(Region.US_EAST_1);
        } else if (StringUtil.equalsIgnoreCase(s3Type, "Cos")) {
            this.type.select("Tencent");
            this.osType.select("Tencent Cloud");
            this.host.setText("http://cos.ap-guangzhou.myqcloud.com");
            this.region.setText("ap-guangzhou");
            NodeGroupUtil.display(this.stage, "appId");
        } else if (StringUtil.equalsIgnoreCase(s3Type, "Obs")) {
            this.type.select("Huawei");
            this.osType.select("Huawei Cloud");
            this.host.setText("https://obs.cn-north-4.myhuaweicloud.com");
            this.region.setText("cn-north-4");
        } else if (StringUtil.equalsIgnoreCase(s3Type, "Oss")) {
            this.type.select("Alibaba");
            this.osType.select("Alibaba Cloud");
            this.host.setText("https://oss-cn-hangzhou.aliyuncs.com");
            this.region.setText("oss-cn-hangzhou");
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
    }
}
