package cn.oyzh.easyshell.serial;

import cn.oyzh.common.thread.ThreadUtil;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class SerialDataListener implements SerialPortDataListener {

    private final WeakReference<SerialPort> serialPort;

    private final Queue<Character> characters = new ConcurrentLinkedQueue<Character>();

    public SerialDataListener(SerialPort serialPort) {
        this.serialPort = new WeakReference<>(serialPort);
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        SerialPort serialPort = this.serialPort.get();
        if (serialPort == null) {
            return;
        }
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            return;
        }
        // 同样使用循环读取法读取所有数据
        while (serialPort.bytesAvailable() != 0) {
            byte[] newData = new byte[serialPort.bytesAvailable()];
            int numRead = serialPort.readBytes(newData, newData.length);
            ThreadUtil.sleep(5);
            String str = new String(newData);
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
        return this.characters.poll();
    }
}
