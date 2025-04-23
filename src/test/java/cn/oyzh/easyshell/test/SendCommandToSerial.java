package cn.oyzh.easyshell.test;

import cn.oyzh.common.thread.DownLatch;
import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;

public class SendCommandToSerial {
    public static void main(String[] args) {
        // 替换为你的虚拟串口设备路径
        String portName = "/dev/pts/2";

        // 获取指定名称的串口
        SerialPort serialPort = SerialPort.getCommPort(portName);

        // 设置串口参数
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        // 打开串口
        if (!serialPort.openPort()) {
            System.err.println("无法打开串口: " + portName);
            return;
        }
        System.out.println("成功打开串口: " + portName);

        try {
            // 获取输出流
            OutputStream outputStream = serialPort.getOutputStream();
            // 要执行的 shell 命令
            String command = "ls /dev\n";
            // 发送命令
            outputStream.write(command.getBytes());
            outputStream.flush();
            System.out.println("命令已发送: " + command);

            // 获取输入流
            BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            // 超时时间（毫秒）
            long timeout = 5000;
            long startTime = System.currentTimeMillis();
            StringBuilder response = new StringBuilder();
            String line;

            while (System.currentTimeMillis() - startTime < timeout) {
                if (reader.ready()) {
                    line = reader.readLine();
                    if (line != null) {
                        response.append(line).append("\n");
                    }
                }
            }

            if (response.length() > 0) {
                System.out.println("命令执行结果：\n" + response.toString());
            } else {
                System.out.println("未收到命令执行结果。");
            }

        } catch (IOException e) {
            System.err.println("通信时出错: " + e.getMessage());
        } finally {
//            // 关闭串口
//            serialPort.closePort();
//            System.out.println("串口已关闭");
        }
    }
}    