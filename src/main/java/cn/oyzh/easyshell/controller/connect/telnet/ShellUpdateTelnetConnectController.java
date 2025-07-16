package cn.oyzh.easyshell.controller.connect.telnet;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
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

/**
 * telnet连接修改业务
 *
 * @author oyzh
 * @since 2025/04/24
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/telnet/shellUpdateTelnetConnect.fxml"
)
public class ShellUpdateTelnetConnectController extends StageController {

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
            // 创建ssh信息
            ShellConnect shellConnect = new ShellConnect();
            shellConnect.setType("telnet");
            shellConnect.setHost(host);
            shellConnect.setConnectTimeOut(3);
            shellConnect.setId(this.shellConnect.getId());
            // 认证信息
            shellConnect.setUser(this.userName.getTextTrim());
            shellConnect.setPassword(this.password.getPassword());
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
//        if (!this.userName.validate()) {
//            return;
//        }
        String password = this.password.getPassword();
        // 检查背景配置
        if (this.enableBackground.isSelected()) {
            if (!this.backgroundImage.validate()) {
                this.tabPane.select(this.backgroundTab);
                return;
            }
        }
        // 名称未填，则直接以host为名称
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            String name = this.name.getTextTrim();
            String remark = this.remark.getTextTrim();
            String osType = this.osType.getSelectedItem();
            String charset = this.charset.getCharsetName();
            int connectTimeOut = this.connectTimeOut.getIntValue();
            String backgroundImage = this.backgroundImage.getText();
            boolean enableBackground = this.enableBackground.isSelected();

            this.shellConnect.setName(name);
            this.shellConnect.setOsType(osType);
            this.shellConnect.setRemark(remark);
            this.shellConnect.setCharset(charset);
            this.shellConnect.setHost(host.trim());
            this.shellConnect.setConnectTimeOut(connectTimeOut);
            // 认证信息
            this.shellConnect.setUser(userName.trim());
            this.shellConnect.setPassword(password.trim());
            // 背景配置
            this.shellConnect.setBackgroundImage(backgroundImage);
            this.shellConnect.setEnableBackground(enableBackground);
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
        // 背景配置
        this.enableBackground.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeGroupUtil.enable(this.backgroundTab, "background");
            } else {
                NodeGroupUtil.disable(this.backgroundTab, "background");
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.shellConnect = this.getProp("shellConnect");
        this.name.setText(this.shellConnect.getName());
        this.hostIp.setText(this.shellConnect.hostIp());
        this.remark.setText(this.shellConnect.getRemark());
        this.osType.select(this.shellConnect.getOsType());
        this.hostPort.setValue(this.shellConnect.hostPort());
        this.charset.setValue(this.shellConnect.getCharset());
        this.connectTimeOut.setValue(this.shellConnect.getConnectTimeOut());
        // 认证处理
        this.userName.setText(this.shellConnect.getUser());
        this.password.setText(this.shellConnect.getPassword());
        // 背景配置
        this.backgroundImage.setText(this.shellConnect.getBackgroundImage());
        this.enableBackground.setSelected(this.shellConnect.isEnableBackground());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectUpdateTitle();
    }

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
}
