package cn.oyzh.easyshell.controller.connect.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
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
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * ftps连接新增业务
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
            ShellConnectUtil.testConnect(this.stage, shellConnect);
            // s3独有
            shellConnect.setS3Type(this.type.getType());
            shellConnect.setS3AppId(this.appId.getTextTrim());
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
            shellConnect.setRegion(region);
            shellConnect.setRemark(remark);
            shellConnect.setCharset(charset);
            shellConnect.setHost(host.trim());
            shellConnect.setConnectTimeOut(connectTimeOut);
            // 认证信息
            shellConnect.setUser(userName.trim());
            shellConnect.setPassword(password.trim());
            // s3独有
            shellConnect.setS3Type(type);
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
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
        this.osType.select("S3");
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
    }
}
