package cn.oyzh.easyshell.controller.tunneling;

import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.fx.tunneling.ShellTunnelingTypeCombobox;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * ssh隧道编辑业务
 *
 * @author oyzh
 * @since 2025/04/16
 */
@StageAttribute(
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
     * 是否启用
     */
    @FXML
    private FXToggleSwitch enable;

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
            boolean enable = this.enable.isSelected();
            int localPort = this.localPort.getIntValue();
            String name = this.tunnelingName.getTextTrim();
            int remotePort = this.remotePort.getIntValue();
            String localHost = this.localHost.getTextTrim();
            String remoteHost = this.remoteHost.getTextTrim();
            String tunnelingType = this.tunnelingType.getTunnelingType();
            this.config.setName(name);
            this.config.setEnabled(enable);
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
        this.config = this.getProp("config");
        this.enable.setSelected(this.config.isEnabled());
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
