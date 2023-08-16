package cn.oyzh.easyssh.controller.info;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.easyfx.controller.FXController;
import cn.oyzh.easyfx.controls.FlexTabPane;
import cn.oyzh.easyfx.controls.FlexTextArea;
import cn.oyzh.easyfx.controls.FlexTextField;
import cn.oyzh.easyfx.controls.NumberField;
import cn.oyzh.easyfx.controls.PortField;
import cn.oyzh.easyfx.event.EventUtil;
import cn.oyzh.easyfx.handler.TabSwitchHandler;
import cn.oyzh.easyfx.information.FXAlertUtil;
import cn.oyzh.easyfx.information.FXToastUtil;
import cn.oyzh.easyfx.view.FXWindow;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.easyssh.SSHStyle;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.store.SSHInfoStore;
import cn.oyzh.easyssh.util.SSHConnectUtil;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * SSH信息修改业务
 *
 * @author oyzh
 * @since 2022/06/16
 */
@Slf4j
@FXWindow(
        title = "SSH连接修改",
        modality = Modality.WINDOW_MODAL,
        iconUrls = SSHConst.ICON_PATH,
        cssUrls = SSHStyle.COMMON,
        value = SSHConst.FXML_BASE_PATH + "info/sshInfoUpdate.fxml"
)
public class SSHInfoUpdateController extends FXController {

    /**
     * tab组件
     */
    @FXML
    private FlexTabPane tabPane;

    /**
     * ssh信息
     */
    private SSHInfo sshInfo;

    /**
     * 名称
     */
    @FXML
    private FlexTextField name;

    /**
     * 用户名
     */
    @FXML
    private FlexTextField user;

    /**
     * 认证密码
     */
    @FXML
    private FlexTextField password;

    /**
     * 连接ip
     */
    @FXML
    private FlexTextField hostIp;

    /**
     * 连接端口
     */
    @FXML
    private PortField hostPort;

    /**
     * 备注
     */
    @FXML
    private FlexTextArea remark;

    /**
     * 连接超时
     */
    @FXML
    private NumberField connectTimeOut;

    /**
     * ssh连接储存对象
     */
    private final SSHInfoStore infoStore = SSHInfoStore.INSTANCE;

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
        if (StrUtil.isNotBlank(host)) {
            SSHConnectUtil.testConnect(this.view, this.user.getText(), host, this.password.getText(), 3);
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
        // 名称未填，则直接以host为名称
        if (StrUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        String name = this.name.getTextTrim();
        this.sshInfo.setName(name);
        // 检查名称
        if (this.infoStore.exist(this.sshInfo)) {
            this.tabPane.select(0);
            FXAlertUtil.warn("此名称已存在！");
            return;
        }
        Number connectTimeOut = this.connectTimeOut.getValue();
        this.sshInfo.setHost(host.trim());
        this.sshInfo.setUser(this.user.getText());
        this.sshInfo.setRemark(this.remark.getTextTrim());
        this.sshInfo.setPassword(this.password.getText());
        this.sshInfo.setConnectTimeOut(connectTimeOut == null ? 5 : connectTimeOut.intValue());
        // 保存数据
        if (this.infoStore.update(this.sshInfo)) {
            EventUtil.fire(SSHEvents.SSH_INFO_UPDATED, this.sshInfo);
            FXToastUtil.ok("修改信息成功!");
            this.closeView();
        } else {
            FXAlertUtil.warn("修改信息失败！");
        }
    }

    @Override
    public void onViewShown(@NonNull WindowEvent event) {
        super.onViewShown(event);
        this.sshInfo = this.getViewProp("sshInfo");
        this.name.setText(this.sshInfo.getName());
        this.user.setText(this.sshInfo.getUser());
        this.remark.setText(this.sshInfo.getRemark());
        this.hostIp.setText(this.sshInfo.hostIp());
        this.password.setText(this.sshInfo.getPassword());
        this.connectTimeOut.setValue(this.sshInfo.getConnectTimeOut());
        this.hostPort.setText(String.valueOf(this.sshInfo.hostPort()));
        TabSwitchHandler.init(this.view);
        this.view.hideOnEscape();
    }

    @Override
    public void onViewHidden(WindowEvent event) {
        TabSwitchHandler.destroy(this.view);
        super.onViewHidden(event);
    }
}
