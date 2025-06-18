package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.pty4j.PtyProcess;

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
public class ShellSSHTtyConnector extends ShellDefaultTtyConnector {

    /**
     * ssh客户端
     */
    private ShellSSHClient client;

    private InputStreamReader shellReader;

    private OutputStreamWriter shellWriter;

    public ShellSSHClient getClient() {
        return client;
    }

    public void init(ShellSSHClient client) throws IOException {
        this.client = client;
        ShellSSHShell shell = client.getShell();
        this.shellReader = new InputStreamReader(shell.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getOutputStream(), this.myCharset);
    }

    public ShellSSHTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
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

    //private ShellZModemProcessor zModemProcessor=new ShellZModemProcessor();

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        //if (this.zModemProcessor.isRZ(str)) {
        //    System.out.println("拦截rz");
        //    this.zModemProcessor.doSend(this.client,()->{
        //        try {
        //            this.shellWriter.write("\nclear\r");
        //        this.shellWriter.flush();
        //        } catch (IOException e) {
        //            throw new RuntimeException(e);
        //        }
        //    });
        //} else {
        this.shellWriter.write(str);
        this.shellWriter.flush();
        //}
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
        //this.zModemProcessor.setRead(str);
        return len;
    }

    @Override
    public InputStream input() throws IOException {
        return this.client.getShell().getInputStream();
    }

    @Override
    public OutputStream output() throws IOException {
        return this.client.getShell().getOutputStream();
    }

    public void reset() throws Exception {
    }
}