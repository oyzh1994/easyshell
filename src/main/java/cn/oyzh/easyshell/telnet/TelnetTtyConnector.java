package cn.oyzh.easyshell.telnet;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class TelnetTtyConnector extends ShellDefaultTtyConnector {

    private TelnetClient client;

    private InputStreamReader shellReader;

    private OutputStreamWriter shellWriter;

    public void initTelnet(TelnetClient client) {
        this.client = client;
        this.shellReader = new InputStreamReader(client.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(client.getOutputStream(), this.myCharset);
    }

    public TelnetTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        try {
            int len;
            if (this.shellReader == null) {
                len = super.read(buf, offset, length);
            } else {
                len = this.shellReader.read(buf, offset, length);
            }
            if (len > 0) {
                this.doRead(buf, offset, len);
            }
            return len;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 是否已输入用户名
     */
    private boolean inputUser = false;

    /**
     * 是否已输入密码
     */
    private boolean inputPasswd = false;

    @Override
    protected void doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        String line = new String(buf, offset, len);
        if (!this.inputUser && StringUtil.containsAnyIgnoreCase(line, "login:")) {
            this.inputUser = true;
            this.shellWriter.write(this.client.getShellConnect().getUser() + "\r\n");
            this.shellWriter.flush();
        } else if (this.inputUser && !this.inputPasswd && StringUtil.containsAnyIgnoreCase(line, "Password:", "密码:")) {
            this.inputPasswd = true;
            this.shellWriter.write(this.client.getShellConnect().getPassword() + "\r\n");
            this.shellWriter.flush();
        }
    }

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && this.client.isConnected();
    }

    @Override
    public void close() {
        super.close();
        this.client.close();
    }
}