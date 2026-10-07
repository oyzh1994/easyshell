package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口客户端
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialClient implements ShellBaseClient {

    /**
     * 串口对象
     */
    private SerialPort serialPort;

    /**
     * 连接
     */
    private final ShellConnect shellConnect;

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>(ShellConnState.NOT_INITIALIZED);

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    /**
     * 构造函数
     *
     * @param shellConnect 连接配置
     */
    public ShellSerialClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    /**
     * 初始化串口
     */
    protected void initClient() {
        // 获取指定名称的串口
        this.serialPort = SerialPort.getCommPort(this.shellConnect.getSerialPortName());

        // 设置串口参数
        this.serialPort.setParity(this.shellConnect.getSerialParityBits());
        this.serialPort.setBaudRate(this.shellConnect.getSerialBaudRate());
        this.serialPort.setNumDataBits(this.shellConnect.getSerialNumDataBits());
        this.serialPort.setNumStopBits(this.shellConnect.getSerialNumStopBits());
        this.serialPort.setFlowControl(this.shellConnect.getSerialFlowControl());
    }

    @Override
    public void start(int timeout) throws IOException {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        try {
            this.initClient();
            this.state.set(ShellConnState.CONNECTING);
            this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, timeout, timeout);
            // 打开串口
            if (!this.serialPort.openPort(timeout)) {
                JulLog.warn("无法打开串口:{}, 错误码:{} 位置:{} ",
                        this.getPortName(),
                        this.getLastErrorCode(),
                        this.getLastErrorLocation()
                );
                this.state.set(ShellConnState.FAILED);
            } else {
                this.state.set(ShellConnState.CONNECTED);
                // 添加到状态监听器队列
                ShellClientChecker.push(this);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            JulLog.warn("Serial client start error", ex);
            this.state.set(ShellConnState.FAILED);
            throw ex;
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
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
                this.serialPort = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
            //            this.shellConnect = null;
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("Serial client close error.", ex);
        }
    }

    /**
     * 获取输出流
     *
     * @return 输出流
     */
    public OutputStream getOutputStream() {
        return this.serialPort == null ? null : this.serialPort.getOutputStream();
    }

    /**
     * 获取输入流
     *
     * @return 输入流
     */
    public InputStream getInputStream() {
        return this.serialPort == null ? null : this.serialPort.getInputStream();
    }

    /**
     * 添加数据监听器
     *
     * @param listener 监听器
     */
    public void addDataListener(ShellSerialDataListener listener) {
        if (this.serialPort != null) {
            this.serialPort.addDataListener(listener);
        }
    }

    /**
     * 获取串口对象
     *
     * @return 串口对象
     */
    public SerialPort getSerialPort() {
        return serialPort;
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    @Override
    public boolean isConnected() {
        return this.serialPort != null && this.serialPort.isOpen();
    }

    /**
     * 获取端口名称
     *
     * @return 端口名称
     */
    public String getPortName() {
        return this.serialPort == null ? this.shellConnect.getSerialPortName() : this.serialPort.getSystemPortName();
    }

    /**
     * 获取最后错误码
     *
     * @return 错误码
     */
    public Integer getLastErrorCode() {
        return this.serialPort == null ? null : this.serialPort.getLastErrorCode();
    }

    /**
     * 获取最后错误位置
     *
     * @return 错误位置
     */
    public Integer getLastErrorLocation() {
        return this.serialPort == null ? null : this.serialPort.getLastErrorLocation();
    }
}
