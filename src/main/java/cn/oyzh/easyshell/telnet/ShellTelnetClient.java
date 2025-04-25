package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellTelnetClient extends TelnetClient implements BaseClient {

//    private static final int IAC = 255;
//    private static final int WILL = 251;
//    private static final int SB = 250;
//    private static final int SE = 240;
//    private static final int NAWS = 31;

    private final ShellConnect shellConnect;

    public ShellTelnetClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    @Override
    public void start(int timeout) throws IOException {
        this.setConnectTimeout(timeout);
        this.setCharset(BaseClient.super.getCharset());
        super.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort());
    }

    @Override
    public void close() {
        try {
            this.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPtySize(int cols, int rows) throws IOException {
        OutputStream out = this.getOutputStream();
//        // 发送 WILL NAWS 命令
//        out.write(new byte[]{(byte) IAC, (byte) WILL, NAWS});
//        out.flush();
//        // 发送新的窗口大小信息
//        byte[] nawsData = new byte[]{
//                (byte) IAC, (byte) SB, NAWS,
//                (byte) (rows >> 8), (byte) (rows & 0xFF),
//                (byte) (cols >> 8), (byte) (cols & 0xFF),
//                (byte) IAC, (byte) SE
//        };
//        out.write(nawsData);
//        out.flush();

        String msg = "stty cols " + cols + " rows " + rows + "\r\n";
        out.write(msg.getBytes());
        out.flush();
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }
}
