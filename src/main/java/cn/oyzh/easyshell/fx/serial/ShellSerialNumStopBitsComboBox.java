package cn.oyzh.easyshell.fx.serial;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import com.fazecast.jSerialComm.SerialPort;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialNumStopBitsComboBox extends FXComboBox<String> {

    {
        super.addItem("1");
        super.addItem("1.5");
        super.addItem("2");
        this.selectFirst();
    }

    public int getNumStopBits() {
        if (this.getSelectedIndex() == 0) {
            return SerialPort.ONE_STOP_BIT;
        }
        if (this.getSelectedIndex() == 1) {
            return SerialPort.ONE_POINT_FIVE_STOP_BITS;
        }
        return SerialPort.TWO_STOP_BITS;
    }

    public void init(int val) {
        if (val == SerialPort.ONE_STOP_BIT) {
            this.select(0);
        } else if (val == SerialPort.ONE_POINT_FIVE_STOP_BITS) {
            this.select(1);
        } else if (val == SerialPort.TWO_STOP_BITS) {
            this.select(2);
        } else {
            this.selectFirst();
        }
    }
}
