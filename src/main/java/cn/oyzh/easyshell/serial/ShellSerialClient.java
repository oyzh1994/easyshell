package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialClient implements BaseClient {

    private SerialPort serialPort;

    private final ShellConnect shellConnect;

    public ShellSerialClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    /**
     * 初始化串口
     */
    protected void iniSerialPort() {
        // 获取指定名称的串口
        this.serialPort = SerialPort.getCommPort(shellConnect.getPortName());

        // 设置串口参数
        this.serialPort.setBaudRate(shellConnect.getBaudRate());
        this.serialPort.setParity(shellConnect.getParityBits());
        this.serialPort.setNumDataBits(shellConnect.getNumDataBits());
        this.serialPort.setNumStopBits(shellConnect.getNumStopBits());
        this.serialPort.setFlowControl(shellConnect.getFlowControl());
    }

    @Override
    public void start(int timeout) throws IOException {
        if (this.serialPort == null) {
            this.iniSerialPort();
        }
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, timeout, timeout);
        // 打开串口
        if (!this.serialPort.openPort()) {
            JulLog.warn("无法打开串口:{}, 错误码:{} 位置:{} ",
                    this.serialPort.getSystemPortName(),
                    this.serialPort.getLastErrorCode(),
                    this.serialPort.getLastErrorLocation()
            );
        }
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
                this.serialPort.flushDataListener();
                this.serialPort.removeDataListener();
                IOUtil.close(this.serialPort.getInputStream());
                IOUtil.close(this.serialPort.getOutputStream());
                this.serialPort.closePort();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addDataListener(ShellSerialDataListener listener) {
        if (this.serialPort != null) {
            this.serialPort.addDataListener(listener);
        }
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    public boolean isConnected() {
        if (this.serialPort != null) {
            return this.serialPort.isOpen();
        }
        return false;
    }

    public Integer getLastErrorCode() {
        return this.serialPort == null ? null : this.serialPort.getLastErrorCode();
    }

    public Integer getLastErrorLocation() {
        return this.serialPort == null ? null : this.serialPort.getLastErrorLocation();
    }
}
