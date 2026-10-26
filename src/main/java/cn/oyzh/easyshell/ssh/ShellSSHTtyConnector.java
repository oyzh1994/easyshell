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

    private volatile ShellSSHClient client;

    /**
     * 当前连接器绑定的 shell。刷新时客户端会切换到新 shell，旧连接器必须继续持有原 shell 的流。
     */
    private ShellSSHShell shell;

    private InputStream shellInput;

    private OutputStream shellOutput;

    /**
     * 是否由当前连接器负责关闭 SSH 客户端。原位刷新时客户端会被新连接器继续复用。
     */
    private boolean ownsClient = true;

    /**
     * 是否已脱离共享客户端。脱离后不得再延迟打开或重新绑定客户端上的 shell。
     */
    private volatile boolean detached;

    public ShellSSHTtyConnector(ShellSSHClient client) throws Exception {
        super(client.getCharset());
        this.client = client;
    }

    public ShellSSHClient getClient() {
        return this.client;
    }

    /**
     * 解除与共享 SSH 客户端的关闭关系。
     */
    public synchronized void detachClient() {
        this.detached = true;
        this.ownsClient = false;
    }

    @Override
    public boolean isConnected() {
        return this.client != null && this.client.isConnected()
                && this.shell != null && this.shell.isConnected();
    }

    @Override
    public synchronized boolean ready() throws IOException {
        if (this.client == null || this.detached) {
            throw new IOException("ssh tty connector is closed");
        }
        if (this.reader == null || !this.isConnected()) {
            try {
                this.shell = this.client.openShell();
                this.shellInput = this.shell.getInputStream();
                this.shellOutput = this.shell.getOutputStream();
                this.reader = new InputStreamReader(this.shellInput, this.charset());
                this.writer = new OutputStreamWriter(this.shellOutput, this.charset());
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
                ShellSSHClient current = this.client;
                if (current != null && !this.detached && current.isWindowsGbkCharset()) {
                    synchronized (this) {
                        // 探测期间可能已经关闭或重连，旧任务不能覆盖新会话。
                        if (this.client != null && !this.detached) {
                            this.reader = new ShellWindowsBomReader(reader);
                        }
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
    public synchronized void close() {
        if (this.client == null) {
            return;
        }
        ShellSSHShell currentShell = this.shell;
        ShellSSHClient currentClient = this.client;
        this.shell = null;
        this.client = null;
        this.shellInput = null;
        this.shellOutput = null;
        // 先关闭 shell 通道，唤醒阻塞在 InputStreamReader 上的终端读取线程，再释放 reader。
        // 反向关闭会在部分平台上形成 reader 锁等待。
        IOUtil.close(currentShell);
        if (this.ownsClient) {
            IOUtil.close(currentClient);
        }
        super.close();
    }

    @Override
    protected int doRead(char[] buffer, int offset, int length) throws IOException {
        int read = super.doRead(buffer, offset, length);
        if (read > 0 && this.client != null && !this.detached) {
            String output = new String(buffer, offset, read);
            this.client.resolveWorkerDir(output);
        }
        return read;
    }

    @Override
    public InputStream input() {
        if (this.shellInput != null) {
            return this.shellInput;
        }
        if (this.detached || this.client == null) {
            return null;
        }
        try {
            ShellSSHShell current = this.client.getShell();
            if (current == null) {
                throw new IllegalStateException("ssh shell is closed");
            }
            return current.getInputStream();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public OutputStream output() {
        if (this.shellOutput != null) {
            return this.shellOutput;
        }
        if (this.detached || this.client == null) {
            return null;
        }
        try {
            ShellSSHShell current = this.client.getShell();
            if (current == null) {
                throw new IllegalStateException("ssh shell is closed");
            }
            return current.getOutputStream();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
