package cn.oyzh.easyshell.fx.serial;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import com.fazecast.jSerialComm.SerialPort;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialFlowControlComboBox extends FXComboBox<String> {

    {
        super.addItem("None");
        super.addItem("RTS/CTS");
        super.addItem("XON/XOFF");
        this.selectFirst();
    }

    public int getFlowControl() {
        if (this.getSelectedIndex() == 0) {
            return SerialPort.FLOW_CONTROL_DISABLED;
        }
        if (this.getSelectedIndex() == 1) {
            return SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED;
        }
        if (this.getSelectedIndex() == 2) {
            return SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
        }
        return SerialPort.FLOW_CONTROL_DISABLED;
    }

    public void init(int flowControl) {
        if (flowControl == SerialPort.FLOW_CONTROL_DISABLED) {
            this.select(0);
        } else if (flowControl == (SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED)) {
            this.select(1);
        } else if (flowControl == (SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED)) {
            this.select(2);
        } else {
            this.selectFirst();
        }
    }
}
