package cn.oyzh.easyshell.test;

import com.fazecast.jSerialComm.*;

public class SerialPortDemo {  
    public static void main(String[] args) {  
        // 获取所有可用串口  
        SerialPort[] ports = SerialPort.getCommPorts();  
        for (SerialPort port : ports) {  
            System.out.println("Port: " + port.getSystemPortName());  
        }  

        // 选择指定串口（以 Linux 的 /dev/ttyUSB0 为例）  
        SerialPort serialPort = SerialPort.getCommPort("/dev/tty.debug-console");
        serialPort.setBaudRate(9600);      // 波特率  
        serialPort.setNumDataBits(8);       // 数据位  
        serialPort.setNumStopBits(1);       // 停止位  
        serialPort.setParity(0);            // 校验位（0=NONE）  

        // 打开串口  
        if (serialPort.openPort()) {  
            System.out.println("Serial port opened.");  

            // 发送数据  
            String command = "ls /\r\n";
            byte[] buffer = command.getBytes();  
            serialPort.writeBytes(buffer, buffer.length);  

            // 接收数据（异步监听）  
            serialPort.addDataListener(new SerialPortDataListener() {  
                @Override  
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }  

                @Override  
                public void serialEvent(SerialPortEvent event) {  
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;  
                    byte[] data = new byte[serialPort.bytesAvailable()];  
                    serialPort.readBytes(data, data.length);  
                    System.out.println("Received: " + new String(data));  
                }  
            });  

            // 保持程序运行（测试时使用）  
            try { Thread.sleep(5000); } catch (InterruptedException e) {}  

            // 关闭串口  
            serialPort.closePort();  
        } else {  
            System.err.println("Failed to open port.");  
        }  
    }  
}  
