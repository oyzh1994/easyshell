package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CharsetUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class SerialClient implements AutoCloseable {

    private SerialPort serialPort;

    private final ShellConnect shellConnect;

    public SerialClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;

    }

    protected void iniSerialPort() {
        // 获取指定名称的串口
        this.serialPort = SerialPort.getCommPort(shellConnect.getPortName());

        // 设置串口参数
        this.serialPort.setBaudRate(shellConnect.getBaudRate());
        this.serialPort.setParity(shellConnect.getParityBits());
        this.serialPort.setNumDataBits(shellConnect.getNumDataBits());
        this.serialPort.setNumStopBits(shellConnect.getNumStopBits());
        this.serialPort.setFlowControl(shellConnect.getFlowControl());
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, shellConnect.connectTimeOutMs(), shellConnect.connectTimeOutMs());
    }

    public boolean start() throws IOException {
        if (this.serialPort == null) {
            this.iniSerialPort();
        }
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

    /**
     * 写入数据
     *
     * @param data 数据
     */
    public void write(byte[] data) {
        if (data != null && this.serialPort != null) {
            this.serialPort.writeBytes(data, data.length);
        }
    }

    /**
     * 写入数据
     *
     * @param str 数据
     */
    public void write(String str) {
        if (str != null) {
            this.write(str.getBytes());
        }
    }

    @Override
    public void close() {
        try {
            if (this.serialPort != null) {
                this.serialPort.removeDataListener();
                IOUtil.close(this.serialPort.getInputStream());
                IOUtil.close(this.serialPort.getOutputStream());
                this.serialPort.flushDataListener();
                this.serialPort.closePort();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addDataListener(SerialDataListener listener) {
        if (this.serialPort != null) {
            this.serialPort.addDataListener(listener);
        }
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    public boolean isConnected() {
        if (this.serialPort != null) {
            return this.serialPort.isOpen();
        }
        return false;
    }

    public Charset getCharset() {
        return CharsetUtil.fromName(this.shellConnect.getCharset());
    }

    public Integer getLastErrorCode() {
        return this.serialPort == null ? null : this.serialPort.getLastErrorCode();
    }

    public Integer getLastErrorLocation() {
        return this.serialPort == null ? null : this.serialPort.getLastErrorLocation();
    }
}
