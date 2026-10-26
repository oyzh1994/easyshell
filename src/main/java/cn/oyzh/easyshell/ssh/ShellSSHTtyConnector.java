package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

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
                // 开启osc
                this.client.scheduleOscWorkerDirIntegration();
                // 远端元数据探测需要额外建立 exec 通道，不能阻塞终端首个输出块的读取。
                this.installWindowsBomReader(reader);
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
        return true;
    }

    /**
     * 探测远端字符集，仅在 Windows GBK 控制台替换读取器。
     *
     * @param reader 原始读取器
     */
    private void installWindowsBomReader(Reader reader) {
        ThreadUtil.startVirtual(() -> {
            try {
                if (this.client.isWindowsGbkCharset()) {
                    synchronized (this) {
                        // 探测期间可能已经关闭或重连，旧任务不能覆盖新会话。
                        this.reader = new ShellWindowsBomReader(reader);
                    }
                }
            } catch (Exception ex) {
                JulLog.debug("detect windows gbk charset failed", ex);
            }
        });
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
