package cn.oyzh.easyshell.telnet;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellTelnetTtyConnector extends TtyStreamConnector {

    private ShellTelnetClient client;

    //    private InputStreamReader shellReader;
    //
    //    private OutputStreamWriter shellWriter;

    public ShellTelnetClient getClient() {
        return client;
    }

    //    public void init(ShellTelnetClient client) {
    //        this.client = client;
    //        //        this.reader = new InputStreamReader(client.getInputStream(), this.charset());
    //        //        this.writer = new OutputStreamWriter(client.getOutputStream(), this.charset());
    //    }

    //    public ShellTelnetTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
    //        super(process, charset, commandLines);
    //    }

    public ShellTelnetTtyConnector(ShellTelnetClient client) {
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
    //        } catch (IOException ex) {
    //            throw ex;
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //        }
    //        return 0;
    //    }

    /**
     * 是否已输入用户名
     */
    private boolean inputUser;

    /**
     * 是否已输入密码
     */
    private boolean inputPasswd;

    @Override
    protected int doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        String line = new String(buf, offset, len);

        // 用户名
        if (!this.inputUser && StringUtil.containsAnyIgnoreCase(line, "login:", "Username:", "用户:", "User:")) {
            this.inputUser = true;
            String user = this.client.getShellConnect().getUser();
            if (StringUtil.isNotBlank(user)) {
                this.writer.write(user + "\r\n");
                this.writer.flush();
            }
        }

        // 密码
        if (!this.inputPasswd && StringUtil.containsAnyIgnoreCase(line, "Password:", "密码:")) {
            this.inputPasswd = true;
            String password = this.client.getShellConnect().getPassword();
            if (StringUtil.isNotBlank(password)) {
                this.writer.write(password + "\r\n");
                this.writer.flush();
                // } else {
                //     this.shellWriter.write("\r");
            }
        }
        return len;
    }

    //    @Override
    //    public void write(String str) throws IOException {
    //        JulLog.debug("shell write : {}", str);
    //        if (this.shellWriter != null) {
    //            this.shellWriter.write(str);
    //            this.shellWriter.flush();
    //        }
    //    }
    //
    //    @Override
    //    public void write(byte[] bytes) throws IOException {
    //        String str = new String(bytes, this.charset());
    //        JulLog.debug("shell write : {}", str);
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
        return "telnet-tty";
    }

    @Override
    public void close() {
        super.close();
        IOUtil.close(this.client);
        this.client = null;
        //        this.client.close();
        //        IOUtil.close(this.shellReader);
        //        IOUtil.close(this.shellWriter);
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
