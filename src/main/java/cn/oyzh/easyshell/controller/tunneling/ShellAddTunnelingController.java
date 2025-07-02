package cn.oyzh.easyshell.controller.tunneling;

import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.fx.tunneling.ShellTunnelingTypeComboBox;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * ssh隧道新增业务
 *
 * @author oyzh
 * @since 2025/04/16
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "tunneling/shellAddTunneling.fxml"
)
public class ShellAddTunnelingController extends StageController {

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
    private ShellTunnelingTypeComboBox tunnelingType;

    /**
     * 是否启用
     */
    @FXML
    private FXToggleSwitch enable;

    /**
     * 添加隧道信息
     */
    @FXML
    private void add() {
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
            boolean enable = this.enable.isSelected();
            int localPort = this.localPort.getIntValue();
            int remotePort = this.remotePort.getIntValue();
            String name = this.tunnelingName.getTextTrim();
            String localHost = this.localHost.getTextTrim();
            String remoteHost = this.remoteHost.getTextTrim();
            String tunnelingType = this.tunnelingType.getTunnelingType();
            ShellTunnelingConfig config = new ShellTunnelingConfig();
            config.setName(name);
            config.setEnabled(enable);
            config.setType(tunnelingType);
            config.setLocalHost(localHost);
            config.setLocalPort(localPort);
            config.setRemoteHost(remoteHost);
            config.setRemotePort(remotePort);
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
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addTunneling();
    }
}
