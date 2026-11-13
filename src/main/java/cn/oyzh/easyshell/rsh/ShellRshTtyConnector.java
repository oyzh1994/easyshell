package cn.oyzh.easyshell.rsh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;
import com.jediterm.core.util.TermSize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * rsh终端tty连接器
 *
 * @author oyzh
 * @since 2026-11-14
 */
public class ShellRshTtyConnector extends TtyStreamConnector {

    /**
     * rsh客户端
     */
    private volatile ShellRshClient client;

    /**
     * 构造rsh终端tty连接器
     *
     * @param client rsh客户端
     */
    public ShellRshTtyConnector(ShellRshClient client) {
        super(client.getCharset());
        this.client = client;
    }

    @Override
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    @Override
    public boolean ready() throws IOException {
        if (this.client == null) {
            throw new IOException("rsh tty connector is closed");
        }
        if (this.reader == null) {
            this.reader = new InputStreamReader(this.client.getInputStream(), this.charset());
            this.writer = new OutputStreamWriter(this.client.getOutputStream(), this.charset());
        }
        return super.ready();
    }

    @Override
    public String getName() {
        return "rsh-tty";
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        super.resize(termSize);
        ShellRshClient current = this.client;
        if (current == null || !current.isPtyResizeSupported() || this.writer == null) {
            return;
        }
        try {
            this.writer.write(RshPtySupport.resizeSequence(termSize.getRows(), termSize.getColumns()));
            this.writer.flush();
        } catch (IOException ex) {
            JulLog.debug("rsh resize failed", ex);
        }
    }

    @Override
    public synchronized void close() {
        if (this.client == null) {
            return;
        }
        ShellRshClient current = this.client;
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
