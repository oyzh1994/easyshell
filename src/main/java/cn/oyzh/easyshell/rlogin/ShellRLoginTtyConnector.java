package cn.oyzh.easyshell.rlogin;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.terminal.ShellTtyAuthUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * rlogin终端tty连接器
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellRLoginTtyConnector extends TtyStreamConnector {

    /**
     * rlogin客户端
     */
    private volatile ShellRLoginClient client;
    //
    //    private InputStreamReader shellReader;
    //
    //    private OutputStreamWriter shellWriter;

//    public void init(ShellRLoginClient client) {
//        this.client = client;
//        //        this.shellReader = new InputStreamReader(client.getInputStream(), this.myCharset);
//        //        this.shellWriter = new OutputStreamWriter(client.getOutputStream(), this.myCharset);
//    }

    //    public ShellRLoginTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
    //        super(process, charset, commandLines);
    //    }

    /**
     * 构造rlogin终端tty连接器
     *
     * @param client rlogin客户端
     */
    public ShellRLoginTtyConnector(ShellRLoginClient client) {
        super(client.getCharset());
        this.client = client;
    }

    //    @Override
    //    public int read(char[] buf, int offset, int length) throws IOException {
    //        try {
    //            int len;
    //            if (this.shellReader == null) {
    //                len = super.read(buf, offset, length);
    //            } else {
    //                len = this.shellReader.read(buf, offset, length);
    //            }
    //            if (len > 0) {
    //                this.doRead(buf, offset, len);
    //            }
    //            return len;
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //        }
    //        return 0;
    //    }

    private final ShellTtyAuthUtil authUtil = new ShellTtyAuthUtil();

    @Override
    protected int doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        ShellRLoginClient current = this.client;
        if (current == null) {
            return len;
        }
        this.authUtil.authenticate(buf, offset, len,
                current.getShellConnect().getUser(),
                current.getShellConnect().getPassword(),
                this.writer, "\r");
        return len;
    }

    //    @Override
    //    public void write(String str) throws IOException {
    //        if (JulLog.isDebugEnabled()) {
    //            JulLog.debug("shell write : {}", str);
    //        }
    //        if (this.shellWriter != null) {
    //            this.shellWriter.write(str);
    //            this.shellWriter.flush();
    //        }
    //    }
    //
    //    @Override
    //    public void write(byte[] bytes) throws IOException {
    //        String str = new String(bytes, this.myCharset);
    //        if (JulLog.isDebugEnabled()) {
    //            JulLog.debug("shell write : {}", str);
    //        }
    //        if (this.shellWriter != null) {
    //            this.shellWriter.write(str);
    //            this.shellWriter.flush();
    //        }
    //    }

    @Override
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    @Override
    public boolean ready() throws IOException {
        if (this.client == null) {
            throw new IOException("rlogin tty connector is closed");
        }
        if (this.reader == null) {
            this.reader = new InputStreamReader(this.client.getInputStream(), this.charset());
            this.writer = new OutputStreamWriter(this.client.getOutputStream(), this.charset());
        }
        return super.ready();
    }

    @Override
    public String getName() {
        return "rlogin-tty";
    }

    @Override
    public synchronized void close() {
        if (this.client == null) {
            return;
        }
        ShellRLoginClient current = this.client;
        this.client = null;
        IOUtil.close(current);
        super.close();
        //        this.shellReader = null;
        //        this.shellWriter = null;
    }

    @Override
    public InputStream input() {
        return this.client == null ? null : this.client.getInputStream();
    }

    @Override
    public OutputStream output() {
        return this.client == null ? null : this.client.getOutputStream();
    }
}
