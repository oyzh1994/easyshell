package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialDataListener implements SerialPortDataListener {

    /**
     * 数据队列
     */
    private final Queue<Character> characters = new ArrayDeque<>();

    /**
     * 字符集
     */
    private final Charset charset;

    public ShellSerialDataListener() {
        this(Charset.defaultCharset());
    }

    public ShellSerialDataListener(Charset charset) {
        this.charset = charset;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        SerialPort serialPort = (SerialPort) event.getSource();
        if (serialPort == null) {
            return;
        }
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            return;
        }
        // 同样使用循环读取法读取所有数据
        while (serialPort.bytesAvailable() != 0) {
            byte[] bytes = new byte[serialPort.bytesAvailable()];
            serialPort.readBytes(bytes, bytes.length);
            ThreadUtil.sleep(1);
            String str = new String(bytes, this.charset);
            if (JulLog.isInfoEnabled()) {
                JulLog.info("串口返回数据:{}", str);
            }
            char[] chars = str.toCharArray();
            for (char aChar : chars) {
                this.characters.add(aChar);
            }
        }
    }

    public boolean isEmpty() {
        return this.characters.isEmpty();
    }

    public Character takeChar() {
        try {
            return this.characters.poll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
