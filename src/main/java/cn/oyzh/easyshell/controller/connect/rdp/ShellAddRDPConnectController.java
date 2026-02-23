package cn.oyzh.easyshell.controller.connect.rdp;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * rdp连接新增业务
 *
 * @author oyzh
 * @since 2025/04/24
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/rdp/shellAddRDPConnect.fxml"
)
public class ShellAddRDPConnectController extends StageController {

    /**
     * 用户
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
     * 系统类型
     */
    @FXML
    private ShellOsTypeComboBox osType;

    /**
     * 分辨率
     */
    @FXML
    private ClearableTextField resolution;

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
            shellConnect.setType("rdp");
            shellConnect.setHost(host);
//            shellConnect.setConnectTimeOut(3);
            // 认证信息
            shellConnect.setUser(this.userName.getTextTrim());
            shellConnect.setPassword(this.password.getPassword());
            // 代理
            ShellConnectUtil.testConnect(this.stage, shellConnect, 3000);
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
        // 名称未填，则直接以host为名称
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            ShellConnect shellConnect = new ShellConnect();
            String name = this.name.getTextTrim();
            String remark = this.remark.getTextTrim();
            String osType = this.osType.getSelectedItem();
            String userName = this.userName.getTextTrim();
            String password = this.password.getPassword();
            String resolution = this.resolution.getTextTrim();

            shellConnect.setName(name);
            shellConnect.setOsType(osType);
            shellConnect.setRemark(remark);
            shellConnect.setHost(host.trim());
            shellConnect.setResolution(resolution);
            // 认证信息
            shellConnect.setUser(userName);
            shellConnect.setPassword(password.trim());
            // 分组及类型
            shellConnect.setType("rdp");
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
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
        this.osType.selectType(ShellPrototype.RDP);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
    }
}
