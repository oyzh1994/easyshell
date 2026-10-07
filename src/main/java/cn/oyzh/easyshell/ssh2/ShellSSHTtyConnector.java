package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;
import org.apache.sshd.client.channel.ChannelShell;

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

    /**
     * ssh客户端
     */
    private ShellSSHClient client;

    //    /**
    //     * 读取器
    //     */
    //    private InputStreamReader shellReader;
    //
    //    /**
    //     * 写入器
    //     */
    //    private OutputStreamWriter shellWriter;

    /**
     * 获取ssh客户端
     *
     * @return ssh客户端
     */
    public ShellSSHClient getClient() {
        return client;
    }

    //    public void init(ShellSSHClient client) throws Exception {
    //        this.client = client;
    //
    //    }

    //    public ShellSSHTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
    //        super(process, charset, commandLines);
    //    }

    /**
     * 构造ssh终端tty连接器
     *
     * @param client ssh客户端
     * @throws Exception 异常
     */
    public ShellSSHTtyConnector(ShellSSHClient client) throws Exception {
        super(client.getCharset());
        this.client = client;
        client.openShell();
    }

    //    @Override
    //    public int read(char[] buf, int offset, int length) throws IOException {
    //        int len = this.shellReader.read(buf, offset, length);
    //        if (len > 0) {
    //            return this.doRead(buf, offset, len);
    //        }
    //        return len;
    //    }

    //    @Override
    //    public void write(String str) throws IOException {
    //        if (JulLog.isDebugEnabled()) {
    //            JulLog.debug("shell write : {}", str);
    //        }
    //        this.shellWriter.write(str);
    //        this.shellWriter.flush();
    //    }

    @Override
    public boolean isConnected() {
        return this.client.isConnected();
    }

    @Override
    public boolean ready() throws IOException {
        if (this.reader == null) {
            try {
                ChannelShell shell = this.client.getShell();
                this.client.waitShellReady(1000);
                this.reader = new InputStreamReader(shell.getInvertedOut(), this.charset());
                this.writer = new OutputStreamWriter(shell.getInvertedIn(), this.charset());
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

    //    @Override
    //    public void write(byte[] bytes) throws IOException {
    //        String str = new String(bytes, this.charset());
    //        this.write(str);
    //    }

    @Override
    public void close() {
        super.close();
        IOUtil.close(this.client);
        this.client = null;
        //        IOUtil.close(this.shellReader);
        //        IOUtil.close(this.shellWriter);
        //        if (this.shellReader != null) {
        //            IOUtil.close(this.shellReader);
        //            this.shellReader = null;
        //        }
        //        if (this.shellWriter != null) {
        //            IOUtil.close(this.shellWriter);
        //            this.shellWriter = null;
        //        }
    }

    @Override
    protected int doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        String str = new String(buf, offset, len);
        if (this.client != null) {
            ThreadUtil.start(() -> this.client.resolveWorkerDir(str));
        }
        return len;
    }

    @Override
    public InputStream input() {
        return this.client.getShell().getInvertedOut();
    }

    @Override
    public OutputStream output() {
        return this.client.getShell().getInvertedIn();
    }
}