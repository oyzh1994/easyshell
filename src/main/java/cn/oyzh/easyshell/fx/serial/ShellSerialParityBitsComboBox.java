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

    public void init(int val) {
        if (val == SerialPort.NO_PARITY) {
            this.select(0);
        } else if (val == SerialPort.EVEN_PARITY) {
            this.select(1);
        } else if (val == SerialPort.ODD_PARITY) {
            this.select(2);
        } else if (val == SerialPort.MARK_PARITY) {
            this.select(3);
        } else if (val == SerialPort.SPACE_PARITY) {
            this.select(4);
        } else {
            this.selectFirst();
        }
    }
}
