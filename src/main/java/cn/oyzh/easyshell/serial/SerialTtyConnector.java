package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class SerialTtyConnector extends ShellDefaultTtyConnector {

    private SerialPort serialPort;

    private final Queue<Character> charsets = new ArrayDeque<Character>();

    public void initSerial() throws IOException {
        // 替换为你的虚拟串口设备路径
        String portName = "/dev/ttys001";

        // 获取指定名称的串口
        SerialPort serialPort = SerialPort.getCommPort(portName);
        this.serialPort = serialPort;

        // 设置串口参数
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.EVEN_PARITY);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_RTS_ENABLED |
                SerialPort.FLOW_CONTROL_CTS_ENABLED);

        // 打开串口
        if (!serialPort.openPort()) {
            System.err.println("无法打开串口: " + portName);
            return;
        }

        serialPort.addDataListener(new SerialPortDataListener() {

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    //同样使用循环读取法读取所有数据
                    while (serialPort.bytesAvailable() != 0) {
                        byte[] newData = new byte[serialPort.bytesAvailable()];
                        int numRead = serialPort.readBytes(newData, newData.length);
                        System.out.println(serialPort.getSystemPortName() + "接收到字节数:" + numRead);
                        System.out.println(serialPort.getSystemPortName() + "数据:" + new String(newData));
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                            byteArrayOutputStream.write(newData);
//                            write(newData);
                        String str = new String(newData);
                        char[] chars = str.toCharArray();
//                            read(chars, 0, chars.length);
//                            byteArrayOutputStream=new ByteArrayOutputStream();
//                            byteArrayOutputStream.write(newData);
                        for (char aChar : chars) {
                            charsets.add(aChar);
                        }
//                        FXUtil.runLater(()->{
//
//                            widget.getTerminal().writeUnwrappedString(str);
//                        });
                    }
//                    try {
//                        myOutputStream.flush();
////                        byteArrayOutputStream.flush();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
                }
            }
        });
//        // 终端输入 → 串口输出（添加缓冲）
//        BufferedReader terminalReader = new BufferedReader(new InputStreamReader(super.myInputStream));
//        BufferedWriter serialWriter = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));
//
//        new Thread(() -> {
//            String line;
//            try {
//                while ((line = terminalReader.readLine()) != null) {  // 按行读取
//                    serialWriter.write(line);
//                    serialWriter.newLine();
//                    serialWriter.flush();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//        super.myOutputStream=serialPort.getOutputStream();
//        super.myInputStream=serialPort.getInputStream();
//        this.shellReader = new InputStreamReader(byteArrayOutputStream, this.myCharset);
//        this.shellWriter = new OutputStreamWriter(serialPort.getOutputStream(), this.myCharset);
    }


    public SerialTtyConnector(PtyProcess process, Charset charset, List<String> commandLines ) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        try {
            int len = 0;
            while (!charsets.isEmpty()) {
                Character charset = charsets.poll();
                if (charset != null) {
                    buf[len++] = charset;
                } else {
                    break;
                }
                if (len >= length) {
                    break;
                }

            }
            if (len == 0) {
                Arrays.fill(buf, 0, buf.length, (char) 0);
            }
            return len == 0 ? 1 : len;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        this.serialPort.writeBytes(bytes, bytes.length);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        super.write(bytes);
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
        this.serialPort.writeBytes(bytes, bytes.length);

    }

    @Override
    public void close() {
        super.close();
        if (serialPort != null) {
            serialPort.closePort();
        }
    }
}