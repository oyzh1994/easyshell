package cn.oyzh.easyshell.test;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.jcraft.jsch.ChannelShell;
import com.pty4j.PtyProcess;
import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellTestTtyConnector extends ShellDefaultTtyConnector {

    /**
     * ssh客户端
     */
    private ShellSSHClient client;

    private InputStreamReader shellReader;

    private OutputStreamWriter shellWriter;

    private Session.Shell shell;

    public void init(Session.Shell shell) throws IOException {
        this.shell = shell;
        this.shellReader = new InputStreamReader(shell.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getOutputStream(), this.myCharset);
    }

    private ChannelShell shell1;

    public void init(ChannelShell shell) throws IOException {
        this.shell1 = shell;
        this.shellReader = new InputStreamReader(shell.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getOutputStream(), this.myCharset);
    }

    public ShellTestTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        int len;
        if (this.shellReader == null) {
            len = super.read(buf, offset, length);
        } else {
            len = this.shellReader.read(buf, offset, length);
        }
        if (len > 0) {
            return this.doRead(buf, offset, len);
        }
        return len;
    }

    private Runnable reset;

    public Runnable getReset() {
        return reset;
    }

    public void setReset(Runnable reset) {
        this.reset = reset;
    }

    @Override
    public void write(String str) throws IOException {
        if(str.equals("reset--1")){
            // reset.run();

            try {
                // shell1.resetPty("xterm");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }
        JulLog.warn("shell write : {}", str);
        //super.write(str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        this.write(str);
    }

    @Override
    public void close() {
        super.close();
        this.client = null;
        if (this.shellReader != null) {
            IOUtil.close(this.shellReader);
            this.shellReader = null;
        }
        if (this.shellWriter != null) {
            IOUtil.close(this.shellWriter);
            this.shellWriter = null;
        }
    }

    @Override
    protected int doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        String str = new String(buf, offset, len);
        if (this.client != null) {
            ThreadUtil.startVirtual(() -> this.client.resolveWorkerDir(str));
        }
        return len;
    }

    @Override
    public InputStream input() throws IOException {
        if (this.shell1 != null) {
            return this.shell1.getInputStream();
        }
        return this.shell.getInputStream();
    }

    @Override
    public OutputStream output() throws IOException {
        if (this.shell1 != null) {
            return this.shell1.getOutputStream();
        }
        return this.shell.getOutputStream();
    }

}