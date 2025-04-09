package cn.oyzh.easyshell.serialPort;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.event.serialPort.SerialPortSetting;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * @author Iammm
 * 2025/4/7 15:56
 * 串口处理器
 */
public class SerialPortService {

    // 使用 volatile 确保多线程可见性，防止指令重排
    private static volatile SerialPortService instance;

    private final ArrayList<SerialPort> portList = new ArrayList<>();

    // 私有构造函数防止外部实例化
    private SerialPortService() {
        // 可添加初始化逻辑
    }

    public static SerialPortService getInstance() {
        if (instance == null) { // 第一次检查（非锁内）
            synchronized (SerialPortService.class) {
                if (instance == null) { // 第二次检查（锁内）
                    instance = new SerialPortService();
                }
            }
        }
        return instance;
    }


    public void closePort(SerialPortSetting serialPortSetting) {
        var comport = serialPortSetting.serialPort();
        if (comport.isOpen()) {
            comport.closePort();
            portList.remove(comport);
        } else {
            JulLog.error(comport.getSystemPortName() + "串口未打开,错误的调用closePort方法");
        }
    }

    public Function<byte[], Boolean> openPort(SerialPortSetting serialPortSetting, Function<byte[], Void> flush) {
        var comport = serialPortSetting.serialPort();
        portList.add(comport);
        comport.setBaudRate(serialPortSetting.baudRate());
        comport.setNumStopBits(serialPortSetting.stopBits());
        comport.setNumDataBits(serialPortSetting.dataBits());
        comport.setParity(serialPortSetting.parity());
        comport.setFlowControl(serialPortSetting.flowControl());
        if (comport.openPort()) {
            JulLog.info("串口：{} 打开成功", comport.getSystemPortName());
        } else {
            throw new RuntimeException("串口：" + comport.getSystemPortName() + " 打开失败");
        }
        comport.addDataListener(new MySerialPortDataListener(comport, flush));
        return data -> {
            if (comport.isOpen()) {
                comport.writeBytes(data, data.length);
                return true;
            } else {
                JulLog.error("串口：{} 未打开,错误的调用write方法", comport.getSystemPortName());
                return false;
            }
        };
    }
}

class MySerialPortDataListener implements SerialPortDataListener {
    private final SerialPort comPort;
    private final Function<byte[], Void> flush;

    public MySerialPortDataListener(SerialPort comPort, Function<byte[], Void> flush) {
        this.comPort = comPort;
        this.flush = flush;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            JulLog.error("串口：{} 监听到数据，但不可用", comPort.getSystemPortName());
            return;
        }
        byte[] newData = new byte[comPort.bytesAvailable()];
        var numRead = comPort.readBytes(newData, newData.length);
        JulLog.debug("串口：{} 成功读取 {} 字节数据", comPort.getSystemPortName(), numRead);
        flush.apply(newData);
    }


}
