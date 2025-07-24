package cn.oyzh.easyshell.fx.serial;

import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import com.fazecast.jSerialComm.SerialPort;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialPortNameTextFiled extends SelectTextFiled<String> {

    {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            this.addItem(port.getSystemPortName());
        }
        if (ports.length > 0) {
            this.setText(ports[0].getSystemPortName());
        }
    }
}
