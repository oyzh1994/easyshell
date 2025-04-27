package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;

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
        this.serialPort = SerialPort.getCommPort(this.shellConnect.getSerialPortName());

        // 设置串口参数
        this.serialPort.setParity(this.shellConnect.getSerialParityBits());
        this.serialPort.setBaudRate(this.shellConnect.getSerialBaudRate());
        this.serialPort.setNumDataBits(this.shellConnect.getSerialNumDataBits());
        this.serialPort.setNumStopBits(this.shellConnect.getSerialNumDataBits());
        this.serialPort.setFlowControl(this.shellConnect.getSerialFlowControl());
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
                    this.getPortName(),
                    this.getLastErrorCode(),
                    this.getLastErrorLocation()
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
                IOUtil.close(this.getInputStream());
                IOUtil.close(this.getOutputStream());
                this.serialPort.closePort();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public InputStream getOutputStream() {
        return this.serialPort == null ? null : this.serialPort.getInputStream();
    }

    public AutoCloseable getInputStream() {
        return this.serialPort == null ? null : this.serialPort.getOutputStream();
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

    public String getPortName() {
        return this.serialPort == null ? this.shellConnect.getSerialPortName() : this.serialPort.getSystemPortName();
    }

    public Integer getLastErrorCode() {
        return this.serialPort == null ? null : this.serialPort.getLastErrorCode();
    }

    public Integer getLastErrorLocation() {
        return this.serialPort == null ? null : this.serialPort.getLastErrorLocation();
    }
}
