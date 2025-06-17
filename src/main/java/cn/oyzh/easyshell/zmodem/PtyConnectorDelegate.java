//package cn.oyzh.easyshell.zmodem;
//
//import cn.oyzh.jeditermfx.app.pty.PtyProcessTtyConnector;
//import com.jediterm.terminal.TtyConnector;
//
//import java.io.IOException;
//
//public class PtyConnectorDelegate implements TtyConnector {
//
//    protected volatile PtyProcessTtyConnector ptyConnector;
//
//    public PtyConnectorDelegate(PtyProcessTtyConnector ptyConnector) {
//        this.ptyConnector = ptyConnector;
//    }
//
//    public PtyProcessTtyConnector getPtyConnector() {
//        return ptyConnector;
//    }
//
//    @Override
//    public int read(char[] buf, int offset, int length) throws IOException {
//        return this.ptyConnector.read(buf, offset, length);
//    }
//
//    @Override
//    public void write(byte[] bytes) throws IOException {
//         this.ptyConnector.write(bytes);
//
//    }
//
//    @Override
//    public void write(String string) throws IOException {
//         this.ptyConnector.write(string);
//
//    }
//
//    @Override
//    public boolean isConnected() {
//        return this.ptyConnector.isConnected();
//    }
//
//    @Override
//    public int waitFor() throws InterruptedException {
//        return ptyConnector != null ? ptyConnector.waitFor() : 0;
//    }
//
//    @Override
//    public boolean ready() throws IOException {
//        return this.ptyConnector.ready();
//    }
//
//    @Override
//    public String getName() {
//        return this.ptyConnector.getName();
//    }
//
//    @Override
//    public void close() {
//        if (ptyConnector != null) {
//            ptyConnector.close();
//        }
//        ptyConnector = null;
//    }
//}