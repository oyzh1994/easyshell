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
public class ShellTelnetTtyConnector extends ShellDefaultTtyConnector {

    private ShellTelnetClient client;

    private InputStreamReader shellReader;

    private OutputStreamWriter shellWriter;

    public ShellTelnetClient getClient() {
        return client;
    }

    public void init(ShellTelnetClient client) {
        this.client = client;
        this.shellReader = new InputStreamReader(client.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(client.getOutputStream(), this.myCharset);
    }

    public ShellTelnetTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
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

        // 用户名
        if (!this.inputUser && StringUtil.containsAnyIgnoreCase(line, "login:")) {
            this.inputUser = true;
            String user = this.client.getShellConnect().getUser();
            if (StringUtil.isNotBlank(user)) {
                this.shellWriter.write(user + "\n");
                this.shellWriter.flush();
            }
        }

        // 密码
        if (!this.inputPasswd && StringUtil.containsAnyIgnoreCase(line, "Password:", "密码:")) {
            this.inputPasswd = true;
            String password = this.client.getShellConnect().getPassword();
            if (StringUtil.isNotBlank(password)) {
                this.shellWriter.write(password + "\n");
            } else {
                this.shellWriter.write("\n");
            }
            this.shellWriter.flush();
        }
    }

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        if (this.shellWriter != null) {
            this.shellWriter.write(str);
            this.shellWriter.flush();
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
        if (this.shellWriter != null) {
            this.shellWriter.write(str);
            this.shellWriter.flush();
        }
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && this.client.isConnected();
    }

    @Override
    public void close() {
        super.close();
        this.client.close();
        this.shellReader = null;
        this.shellWriter = null;
    }
}
