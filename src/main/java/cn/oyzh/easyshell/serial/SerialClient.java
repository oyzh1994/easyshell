package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class SerialClient {

    private SerialPort serialPort;

    public SerialClient(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public boolean open() throws IOException {
        // 打开串口
        if (!this.serialPort.openPort()) {
            JulLog.warn("无法打开串口:{}, 错误码:{} 位置:{} ",
                    this.serialPort.getSystemPortName(),
                    this.serialPort.getLastErrorCode(),
                    this.serialPort.getLastErrorLocation()
            );
            return false;
        }
        return true;
    }
}
