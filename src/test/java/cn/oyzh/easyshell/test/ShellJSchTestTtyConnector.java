package cn.oyzh.easyshell.test;

import cn.oyzh.fx.tty.TtyStreamConnector;
import com.jcraft.jsch.ChannelShell;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class ShellJSchTestTtyConnector extends TtyStreamConnector {

    private ChannelShell shell;

    public ShellJSchTestTtyConnector(Charset charset) {
        super(charset);
    }

    public void init(ChannelShell shell) throws IOException {
        this.shell = shell;
        this.reader = new InputStreamReader(shell.getInputStream(), this.charset());
        this.writer = new OutputStreamWriter(shell.getOutputStream(), this.charset());
    }

    @Override
    public String getName() {
        return "jsch-test-tty";
    }

    @Override
    public boolean isConnected() {
        return this.shell != null && this.shell.isConnected();
    }

    @Override
    public InputStream input() {
        try {
            return this.shell == null ? null : this.shell.getInputStream();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public OutputStream output() {
        try {
            return this.shell == null ? null : this.shell.getOutputStream();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void close() {
        super.close();
        if (this.shell != null) {
            this.shell.disconnect();
            this.shell = null;
        }
    }
}
