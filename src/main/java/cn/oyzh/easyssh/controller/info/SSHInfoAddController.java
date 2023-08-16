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
import cn.oyzh.easyssh.domain.SSHGroup;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.parser.SSHExceptionParser;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.store.SSHInfoStore;
import cn.oyzh.easyssh.util.SSHConnectUtil;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 添加ssh信息业务
 *
 * @author oyzh
 * @since 2023/06/16
 */
@Slf4j
@FXWindow(
        title = "SSH连接新增",
        modality = Modality.WINDOW_MODAL,
        iconUrls = SSHConst.ICON_PATH,
        cssUrls = SSHStyle.COMMON,
        value = SSHConst.FXML_BASE_PATH + "info/sshInfoAdd.fxml"
)
public class SSHInfoAddController extends FXController {

    /**
     * tab组件
     */
    @FXML
    private FlexTabPane tabPane;

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
     * 备注
     */
    @FXML
    private FlexTextArea remark;

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
     * 连接超时
     */
    @FXML
    private NumberField connectTimeOut;

    /**
     * 分组
     */
    private SSHGroup group;

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
     * 添加ssh信息
     */
    @FXML
    private void add() {
        String host = this.getHost();
        if (host == null) {
            return;
        }
        // 名称未填，则直接以host为名称
        if (StrUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(host.replace(":", "_"));
        }
        try {
            String name = this.name.getTextTrim();
            SSHInfo sshInfo = new SSHInfo();
            sshInfo.setName(name);
            // 检查名称是否存在
            if (this.infoStore.exist(sshInfo)) {
                FXAlertUtil.warn("此名称已存在！");
                return;
            }

            Number connectTimeOut = this.connectTimeOut.getValue();

            sshInfo.setHost(host);
            sshInfo.setUser(this.user.getText());
            sshInfo.setRemark(this.remark.getTextTrim());
            sshInfo.setPassword(this.password.getText());
            sshInfo.setGroupId(this.group == null ? null : this.group.getGid());
            sshInfo.setConnectTimeOut(connectTimeOut == null ? 5 : connectTimeOut.intValue());
            // 保存数据
            boolean result = this.infoStore.add(sshInfo);
            if (result) {
                EventUtil.fire(SSHEvents.SSH_INFO_ADD, sshInfo);
                FXToastUtil.ok("新增ssh信息成功!");
                this.closeView();
            } else {
                FXAlertUtil.warn("新增ssh信息失败！");
            }
        } catch (Exception ex) {
            FXAlertUtil.warn(ex, SSHExceptionParser.INSTANCE);
        }
    }

    @Override
    public void onViewShown(WindowEvent event) {
        super.onViewShown(event);
        TabSwitchHandler.init(this.view);
        this.group = this.getViewProp("group");
        this.view.hideOnEscape();
    }

    @Override
    public void onViewHidden(WindowEvent event) {
        TabSwitchHandler.destroy(this.view);
        super.onViewHidden(event);
    }
}
