package zmodem;


import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import org.apache.commons.net.io.CopyStreamListener;
import zmodem.util.FileAdapter;
import zmodem.xfer.zm.util.ZModemReceive;
import zmodem.xfer.zm.util.ZModemSend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


public class ZModem {

    private final InputStream netIs;
    private final OutputStream netOs;
    private final ShellDefaultTtyConnector connector;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);


    public ZModem(InputStream netin, OutputStream netout, ShellDefaultTtyConnector connector) {
        netIs = netin;
        netOs = netout;
        this.connector = connector;
    }

    public void receive(Supplier<FileAdapter> destDir, CopyStreamListener listener) throws IOException {
        ZModemReceive sender = new ZModemReceive(destDir, netIs, netOs);
        sender.addCopyStreamListener(listener);
        sender.receive(isCancelled::get);
        netOs.flush();
    }

    public void send(Supplier<List<FileAdapter>> filesSupplier, CopyStreamListener listener) throws Exception {
        ZModemSend sender = new ZModemSend(filesSupplier, netIs, netOs);
        sender.addCopyStreamListener(listener);
        sender.send(isCancelled::get);
        // ThreadUtil.sleep(300);
        // netOs.write("stty sane\n".getBytes());
        // netOs.write(("stty cooked echo icrnl onlcr\n").getBytes());
        // 启动 screen 会话
        // netOs.write(("screen -S rz_session\n").getBytes());
// 退出 screen 会话并恢复终端
//         netOs.write(("exit\n").getBytes());
//         netOs.write(("stty sane\n").getBytes());
//         System.setProperty("test111", "1");

        // int i = 5;
        // while (i-- > 0) {
        //     while (netIs.available() > 0) {
        //         netIs.read();
        //     }
        //     Thread.sleep(100);
        // }

        // connector.output().write(new byte[]{0x18, 'B', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '\r', (byte) 0x8a});
        // netOs.write("\u0018B00000000000000\r\u008a".getBytes());
        // netOs.write(new byte[]{0x18, 'B', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '\r', (byte) 0x8a});
        netOs.flush();



// Ctrl+C


//         for (int j = 0; j < 5; j++) {
//             netOs.write(3);   // 0x03
//             netOs.flush();
//             Thread.sleep(100);
//             netOs.write(26);   // 0x03
//             netOs.flush();
//             Thread.sleep(100);
//             netOs.write("\n\n\n".getBytes(StandardCharsets.UTF_8));
//             netOs.flush();
//             Thread.sleep(100);
//         }
//
//
// // 再发恢复
//         netOs.write("stty sane\n".getBytes(StandardCharsets.UTF_8));
//         netOs.flush();
//         Thread.sleep(100);
//         netOs.write("reset\n".getBytes(StandardCharsets.UTF_8));
//         netOs.flush();
//         connector.write("reset--1");

        connector.resetTtyConnector();

        System.out.println("rz finish");
    }

    public void cancel() {
        isCancelled.compareAndSet(false, true);
    }

    public InputStream getNetIs() {
        return netIs;
    }

    public OutputStream getNetOs() {
        return netOs;
    }

    public AtomicBoolean getIsCancelled() {
        return isCancelled;
    }
}
