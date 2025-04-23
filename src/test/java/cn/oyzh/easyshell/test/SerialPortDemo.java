package cn.oyzh.easyshell.test;

import com.fazecast.jSerialComm.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SerialPortDemo {
    public static void main(String[] args) throws IOException {
        // 获取所有可用串口
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            System.out.println("Port: " + port.getSystemPortName());
        }

        // 选择指定串口（以 Linux 的 /dev/ttyUSB0 为例）
        SerialPort serialPort = SerialPort.getCommPort("/dev/pts/2");

//        ReflectUtil.setFieldValue("comPort","/dev/pts/2",serialPort);
        serialPort.setBaudRate(9600);      // 波特率
        serialPort.setNumDataBits(8);       // 数据位
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);       // 停止位
        serialPort.setParity(SerialPort.NO_PARITY);            // 校验位（0=NONE）

        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
//        serialPort.setNumOverrunErrorsToIgnore(10);
//        serialPort.setNumParityErrorsToIgnore(10);
        // 打开串口
        if (serialPort.openPort()) {
            System.out.println("Serial port opened.");

//            // 发送数据
//            String command = "ls /dev\r\n";
//            byte[] buffer = command.getBytes();
//            serialPort.writeBytes(buffer, buffer.length);
//
//            // 接收数据（异步监听）
//            serialPort.addDataListener(new SerialPortDataListener() {
//                @Override
//                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
//
//                @Override
//                public void serialEvent(SerialPortEvent event) {
//                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
//                    byte[] data = new byte[serialPort.bytesAvailable()];
//                    serialPort.readBytes(data, data.length);
//                    System.out.println("Received: " + new String(data));
//                }
//            });

            // 获取输出流
            OutputStream outputStream = serialPort.getOutputStream();
            String message = "ls /\n";
            // 发送消息
            outputStream.write(message.getBytes());
            outputStream.flush();
            System.out.println("消息已发送: " + message);

            // 获取输入流
            BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            // 等待接收数据
            if (reader.ready()) {
                String receivedData = reader.readLine();
                System.out.println("收到回复: " + receivedData);
            } else {
                System.out.println("未收到回复");
            }



            // 保持程序运行（测试时使用）
            try { Thread.sleep(5000); } catch (InterruptedException e) {}

            // 关闭串口
            serialPort.closePort();
        } else {
            System.err.println("Failed to open port.");
            System.err.println("错误信息: " + serialPort.getLastErrorCode() + " - " + serialPort.getLastErrorLocation());

        }
    }
}
