package cn.oyzh.easyshell.rlogin;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
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
 * @since 2025-05-27
 */
public class ShellRLoginTtyConnector extends TtyStreamConnector {

    /**
     * rlogin客户端
     */
    private ShellRLoginClient client;
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

    /**
     * 是否已输入密码
     */
    private int inputPasswd;

    @Override
    protected int doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        String line = new String(buf, offset, len);
        // 说明结束了
        if (line.contains("#")) {
            this.inputPasswd = Integer.MAX_VALUE;
            return len;
        }
        // 自动输入密码，第一次可能失败，最多重试3次
        String password = this.client.getShellConnect().getPassword();
        if (StringUtil.isNotBlank(password) && this.inputPasswd < 3 && StringUtil.containsAnyIgnoreCase(line, "Password:", "密码:")) {
            this.inputPasswd++;
            this.writer.write(password + "\r");
            this.writer.flush();
        }
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
        return this.client.isConnected();
    }

    @Override
    public boolean ready() throws IOException {
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
    public void close() {
        super.close();
        IOUtil.close(this.client);
        this.client = null;
        //        this.shellReader = null;
        //        this.shellWriter = null;
    }

    @Override
    public InputStream input() {
        return this.client.getInputStream();
    }

    @Override
    public OutputStream output() {
        return this.client.getOutputStream();
    }
}
