package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

public class ShellSSHTtyConnector extends TtyStreamConnector {

    private ShellSSHClient client;

    private Reader outputReader;

    public ShellSSHTtyConnector(ShellSSHClient client) throws Exception {
        super(client.getCharset());
        this.client = client;
        client.openShell();
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
                if (this.reader != null) {
                    IOUtil.close(this.outputReader);
                    IOUtil.close(this.reader);
                    IOUtil.close(this.writer);
                    this.outputReader = null;
                    this.reader = null;
                    this.writer = null;
                }
                ShellSSHShell shell;
                if (this.isConnected()) {
                    shell = this.client.getShell();
                } else {
                    shell = this.client.reopenShell();
                    this.client.waitShellReady(1000);
                }
                this.reader = new InputStreamReader(shell.getInputStream(), this.charset());
                this.writer = new OutputStreamWriter(shell.getOutputStream(), this.charset());
                String remoteCharset = this.client.getRemoteCharset();
                if (this.client.isWindows()
                        && ("gbk".equalsIgnoreCase(remoteCharset) || "gb18030".equalsIgnoreCase(remoteCharset))) {
                    this.outputReader = new ShellWindowsBomReader(this.reader);
                } else {
                    this.outputReader = this.reader;
                }
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
        IOUtil.close(this.outputReader);
        this.outputReader = null;
        super.close();
        IOUtil.close(this.client);
        this.client = null;
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        this.ready();
        int read = this.outputReader.read(buffer, offset, length);
        if (read > 0) {
            this.doRead(buffer, offset, read);
        }
        return read;
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
