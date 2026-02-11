package cn.oyzh.easyshell.controller.jump;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.fx.connect.ShellConnectTextField;
import cn.oyzh.easyshell.ssh2.ShellSSHUtil;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.domain.SSHConnect;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * ssh跳板机新增业务
 *
 * @author oyzh
 * @since 2025/04/15
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "jump/shellAddHost.fxml"
)
public class ShellAddHostController extends StageController {

    /**
     * 跳板名称
     */
    @FXML
    private ClearableTextField sshName;

    /**
     * 主机
     */
    @FXML
    private ShellConnectTextField host;

    /**
     * 是否启用
     */
    @FXML
    private FXToggleSwitch enable;

    /**
     * 密钥存储
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    /**
     * 测试连接
     */
    @FXML
    private void testConnect() {
        // 检查连接地址
        ShellConnect connect = this.host.getSelectedItem();
        if (connect != null) {
            ShellConnectUtil.testConnect(this.stage, connect);
        }
    }

    /**
     * 添加跳板信息
     */
    @FXML
    private void add() {
        String name = this.sshName.getText();
        if (!this.sshName.validate()) {
            return;
        }
        if (!this.host.validate()) {
            return;
        }
        try {
            ShellConnect connect = this.host.getSelectedItem();
            ShellJumpConfig config = new ShellJumpConfig();
//            config.setName(name);
//            config.setHost(connect.hostIp());
//            config.setUser(connect.getUser());
//            config.setPort(connect.hostPort());
//            config.setPassword(connect.getPassword());
            config.setEnabled(this.enable.isSelected());
//            config.setTimeout(connect.getConnectTimeOut());
//            config.setCertificatePath(connect.getCertificate());
//            if (connect.isManagerAuth()) {
//                ShellKey key = this.keyStore.selectOne(connect.getKeyId());
//                config.setAuthMethod("key");
//                config.setCertificatePwd(key.getPassword());
//                config.setCertificatePubKey(key.getPublicKey());
//                config.setCertificatePriKey(key.getPrivateKey());
//            } else if (connect.isCertificateAuth()) {
//                config.setAuthMethod("certificate");
//                config.setCertificatePwd(connect.getCertificatePwd());
//            } else if (connect.isPasswordAuth()) {
//                config.setAuthMethod("password");
//            }
            // 转换对象
            SSHConnect sshConnect = ShellSSHUtil.convert(connect);
            // 复制对象
            config.copy(sshConnect);
            config.setName(name);

            // 设置数据
            this.setProp("jumpConfig", config);
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        // 移除当前连接
        ShellConnect connect = this.getProp("connect");
        if (connect != null) {
            this.host.removeItem(connect);
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addHost();
    }
}
