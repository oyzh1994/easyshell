package cn.oyzh.easyshell.fx.serial;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import com.fazecast.jSerialComm.SerialPort;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialParityBitsComboBox extends FXComboBox<String> {

    {
        super.addItem("None");
        super.addItem("Event");
        super.addItem("Odd");
        super.addItem("Mark");
        super.addItem("Space");
        this.selectFirst();
    }

    public int getParityBits() {
        if (this.getSelectedIndex() == 0) {
            return SerialPort.NO_PARITY;
        }
        if (this.getSelectedIndex() == 1) {
            return SerialPort.EVEN_PARITY;
        }
        if (this.getSelectedIndex() == 2) {
            return SerialPort.ODD_PARITY;
        }
        if (this.getSelectedIndex() == 3) {
            return SerialPort.MARK_PARITY;
        }
        return SerialPort.SPACE_PARITY;
    }
}
