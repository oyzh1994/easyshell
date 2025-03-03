//package cn.oyzh.easyssh.controller.info;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyssh.SSHConst;
//import cn.oyzh.easyssh.domain.SSHGroup;
//import cn.oyzh.easyssh.domain.SSHConnect;
//import cn.oyzh.easyssh.ssh.SSHEvents;
//import cn.oyzh.easyssh.store.SSHConnectStore;
//import cn.oyzh.easyssh.util.SSHConnectUtil;
//import cn.oyzh.fx.gui.text.field.NumberTextField;
//import cn.oyzh.fx.gui.text.field.PortTextField;
//import cn.oyzh.fx.plus.controller.StageController;
//import cn.oyzh.fx.plus.controls.tab.FXTabPane;
//import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
//import cn.oyzh.fx.plus.controls.text.field.FXTextField;
//import cn.oyzh.fx.plus.handler.TabSwitchHandler;
//import cn.oyzh.fx.plus.information.MessageBox;
//import cn.oyzh.fx.plus.window.StageAttribute;
//import javafx.fxml.FXML;
//import javafx.stage.Modality;
//import javafx.stage.WindowEvent;
//
///**
// * 添加ssh信息业务
// *
// * @author oyzh
// * @since 2023/06/16
// */
//@StageAttribute(
//        title = "SSH连接新增",
//        modality = Modality.WINDOW_MODAL,
//        value = SSHConst.FXML_BASE_PATH + "info/sshInfoAdd.fxml"
//)
//public class SSHConnectAddController extends StageController {
//
//    /**
//     * tab组件
//     */
//    @FXML
//    private FXTabPane tabPane;
//
//    /**
//     * 名称
//     */
//    @FXML
//    private FXTextField name;
//
//    /**
//     * 用户名
//     */
//    @FXML
//    private FXTextField user;
//
//    /**
//     * 认证密码
//     */
//    @FXML
//    private FXTextField password;
//
//    /**
//     * 备注
//     */
//    @FXML
//    private FXTextArea remark;
//
//    /**
//     * 连接ip
//     */
//    @FXML
//    private FXTextField hostIp;
//
//    /**
//     * 连接端口
//     */
//    @FXML
//    private PortTextField hostPort;
//
//    /**
//     * 连接超时
//     */
//    @FXML
//    private NumberTextField connectTimeOut;
//
//    /**
//     * 分组
//     */
//    private SSHGroup group;
//
//    /**
//     * ssh连接储存对象
//     */
//    private final SSHConnectStore infoStore = SSHConnectStore.INSTANCE;
//
//    /**
//     * 获取连接地址
//     *
//     * @return 连接地址
//     */
//    private String getHost() {
//        String hostText;
//        String hostIp = this.hostIp.getTextTrim();
//        this.tabPane.select(0);
//        if (!this.hostPort.validate()) {
//            this.tabPane.select(0);
//            return null;
//        }
//        if (!this.hostIp.validate()) {
//            this.tabPane.select(0);
//            return null;
//        }
//        hostText = hostIp + ":" + this.hostPort.getValue();
//        return hostText;
//    }
//
//    /**
//     * 测试连接
//     */
//    @FXML
//    private void testConnect() {
//        // 检查连接地址
//        String host = this.getHost();
//        if (StringUtil.isNotBlank(host)) {
//            SSHConnectUtil.testConnect(this.stage, this.user.getText(), host, this.password.getText(), 3);
//        }
//    }
//
//    /**
//     * 添加ssh信息
//     */
//    @FXML
//    private void add() {
//        String host = this.getHost();
//        if (host == null) {
//            return;
//        }
//        // 名称未填，则直接以host为名称
//        if (StringUtil.isBlank(this.name.getTextTrim())) {
//            this.name.setText(host.replace(":", "_"));
//        }
//        try {
//            String name = this.name.getTextTrim();
//            SSHConnect sshInfo = new SSHConnect();
//            sshInfo.setName(name);
//            // 检查名称是否存在
//            if (this.infoStore.exist(sshInfo)) {
//                MessageBox.warn("此名称已存在！");
//                return;
//            }
//
//            Number connectTimeOut = this.connectTimeOut.getValue();
//
//            sshInfo.setHost(host);
//            sshInfo.setUser(this.user.getText());
//            sshInfo.setRemark(this.remark.getTextTrim());
//            sshInfo.setPassword(this.password.getText());
//            sshInfo.setGroupId(this.group == null ? null : this.group.getGid());
//            sshInfo.setConnectTimeOut(connectTimeOut == null ? 5 : connectTimeOut.intValue());
//            // 保存数据
//            boolean result = this.infoStore.add(sshInfo);
//            if (result) {
//                EventUtil.fire(SSHEvents.SSH_INFO_ADD, sshInfo);
//                MessageBox.okToast("新增ssh信息成功!");
//                this.closeWindow();
//            } else {
//                MessageBox.warn("新增ssh信息失败！");
//            }
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }
//
//    @Override
//    public void onWindowShown(WindowEvent event) {
//        super.onWindowShown(event);
//        TabSwitchHandler.init(this.view);
//        this.group = this.getWindowProp("group");
//        this.stage.hideOnEscape();
//    }
//
//}
