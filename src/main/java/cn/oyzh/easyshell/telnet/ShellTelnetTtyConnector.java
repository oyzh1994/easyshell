package cn.oyzh.easyshell.telnet;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.terminal.ShellTtyAuthUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * telnet终端tty连接器
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellTelnetTtyConnector extends TtyStreamConnector {

    /**
     * telnet客户端
     */
    private volatile ShellTelnetClient client;

    /**
     * 获取telnet客户端
     *
     * @return telnet客户端
     */
    public ShellTelnetClient getClient() {
        return client;
    }

    /**
     * 构造telnet终端tty连接器
     *
     * @param client telnet客户端
     */
    public ShellTelnetTtyConnector(ShellTelnetClient client) {
        super(client.getCharset());
        this.client = client;
    }

    private final ShellTtyAuthUtil authUtil = new ShellTtyAuthUtil();

    @Override
    protected int doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        ShellTelnetClient current = this.client;
        if (current == null) {
            return len;
        }
        this.authUtil.authenticate(buf, offset, len,
                current.getShellConnect().getUser(),
                current.getShellConnect().getPassword(),
                this.writer, "\r\n");
        return len;
    }

    @Override
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    @Override
    public boolean ready() throws IOException {
        if (this.client == null) {
            throw new IOException("telnet tty connector is closed");
        }
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
    public synchronized void close() {
        if (this.client == null) {
            return;
        }
        ShellTelnetClient current = this.client;
        this.client = null;
        IOUtil.close(current);
        super.close();
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
