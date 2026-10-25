package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * ssh终端tty连接器
 *
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellSSHTtyConnector extends TtyStreamConnector {

    private ShellSSHClient client;

    public ShellSSHTtyConnector(ShellSSHClient client) throws Exception {
        super(client.getCharset());
        this.client = client;
    }

    public ShellSSHClient getClient() {
        return this.client;
    }

    @Override
    public boolean isConnected() {
        ShellSSHShell shell = this.client == null ? null : this.client.getShell();
        return this.client != null && this.client.isConnected() && shell != null && shell.isConnected();
    }

    @Override
    public boolean ready() throws IOException {
        if (this.reader == null || !this.isConnected()) {
            try {
                ShellSSHShell shell = this.client.openShell();
                this.reader = new InputStreamReader(shell.getInputStream(), this.charset());
                this.writer = new OutputStreamWriter(shell.getOutputStream(), this.charset());
                String remoteCharset = this.client.getRemoteCharset();
                if (this.client.isWindows() && StringUtil.equalsAnyIgnoreCase(remoteCharset, "gbk", "gb18030")) {
                    this.reader = new ShellWindowsBomReader(shell.getInputStream(), this.reader);
                }
                this.client.scheduleOscWorkerDirIntegration();
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "ssh-tty";
    }

    @Override
    public void close() {
        super.close();
        IOUtil.close(this.client);
        this.client = null;
    }

    @Override
    protected int doRead(char[] buffer, int offset, int length) throws IOException {
        int read = super.doRead(buffer, offset, length);
        if (read > 0 && this.client != null) {
            String output = new String(buffer, offset, read);
            this.client.resolveWorkerDir(output);
        }
        return read;
    }

    @Override
    public InputStream input() {
        try {
            return this.client.getShell().getInputStream();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public OutputStream output() {
        try {
            return this.client.getShell().getOutputStream();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
