package cn.oyzh.easyssh.controller.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.domain.SSHGroup;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.store.SSHConnectStore;
import cn.oyzh.easyssh.util.SSHConnectUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.ArrayList;

/**
 * ssh连接新增业务
 *
 * @author oyzh
 * @since 2020/9/15
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/sshAddConnect.fxml"
)
public class SSHAddConnectController extends StageController {

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
     * 分组
     */
    private SSHGroup group;

    /**
     * ssh连接储存对象
     */
    private final SSHConnectStore connectStore = SSHConnectStore.INSTANCE;

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
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
        } else {
            // 创建ssh连接
            SSHConnect sshConnect = new SSHConnect();
            sshConnect.setHost(host);
            sshConnect.setConnectTimeOut(3);
            SSHConnectUtil.testConnect(this.stage, sshConnect);
        }
    }

    /**
     * 添加ssh信息
     */
    @FXML
    private void add() {
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
            SSHConnect sshConnect = new SSHConnect();
            sshConnect.setName(name);
            Number connectTimeOut = this.connectTimeOut.getValue();

            sshConnect.setHost(host.trim());
            sshConnect.setUser(userName.trim());
            sshConnect.setPassword(password.trim());
            sshConnect.setRemark(this.remark.getTextTrim());
            sshConnect.setGroupId(this.group == null ? null : this.group.getGid());
            sshConnect.setConnectTimeOut(connectTimeOut.intValue());
            // 保存数据
            if (this.connectStore.replace(sshConnect)) {
                SSHEventUtil.connectAdded(sshConnect);
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
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getWindowProp("group");
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
    }
}
