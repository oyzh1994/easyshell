package cn.oyzh.easyshell.controller.tunneling;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.fx.tunneling.ShellTunnelingTypeCombobox;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.combobox.SSHAuthTypeCombobox;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;

/**
 * ssh隧道编辑业务
 *
 * @author oyzh
 * @since 2025/04/16
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "tunneling/shellUpdateTunneling.fxml"
)
public class ShellUpdateTunnelingController extends StageController {

    /**
     * 隧道名称
     */
    @FXML
    private ClearableTextField tunnelingName;

    /**
     * 本地地址
     */
    @FXML
    private ClearableTextField localHost;

    /**
     * 本地端口
     */
    @FXML
    private PortTextField localPort;

    /**
     * 远程地址
     */
    @FXML
    private ClearableTextField remoteHost;

    /**
     * 远程端口
     */
    @FXML
    private PortTextField remotePort;

    /**
     * 隧道类型
     */
    @FXML
    private ShellTunnelingTypeCombobox tunnelingType;

    /**
     * 隧道设置
     */
    private ShellTunnelingConfig config;

    /**
     * 修改隧道信息
     */
    @FXML
    private void update() {
        if (!this.tunnelingName.validate()) {
            return;
        }
        if (!this.localHost.validate()) {
            return;
        }
        if (!this.tunnelingType.validate()) {
            return;
        }
        if (!this.tunnelingType.isDynamicAuth() && !this.remoteHost.validate()) {
            return;
        }
        try {
            String localHost = this.localHost.getTextTrim();
            String remoteHost = this.remoteHost.getTextTrim();
            int localPort = this.localPort.getIntValue();
            int remotePort = this.remotePort.getIntValue();
            String tunnelingType = this.tunnelingType.getTunnelingType();
            String name = this.tunnelingName.getTextTrim();
            this.config.setName(name);
            this.config.setType(tunnelingType);
            this.config.setLocalHost(localHost);
            this.config.setLocalPort(localPort);
            this.config.setRemoteHost(remoteHost);
            this.config.setRemotePort(remotePort);
            // 设置数据
            this.setProp("tunnelingConfig", config);
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void bindListeners() {
        // 隧道类型
        this.tunnelingType.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (this.tunnelingType.isDynamicAuth()) {
                this.remoteHost.disable();
                this.remotePort.disable();
            } else {
                this.remoteHost.enable();
                this.remotePort.enable();
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.config = this.getWindowProp("config");
        this.tunnelingName.setText(this.config.getName());
        this.tunnelingType.setType(this.config.getType());
        this.localHost.setText(this.config.getLocalHost());
        this.localPort.setValue(this.config.getLocalPort());
        this.remoteHost.setText(this.config.getRemoteHost());
        this.remotePort.setValue(this.config.getRemotePort());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateTunneling();
    }
}
