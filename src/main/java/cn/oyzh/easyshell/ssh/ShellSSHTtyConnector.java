package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import cn.oyzh.ssh.util.SSHUtil;
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

    private String lastRead;
    private StringBuilder builder = new StringBuilder();

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        builder.append(str);
        System.out.println(builder);
        String s1="\u001B[A\r";
        String s2="\r";
       this.lastRead= SSHUtil.removeAnsi(this.lastRead);
        while (true){
            if(this.lastRead.contains("\b")){
                this.lastRead=this.lastRead.replace("\b","");
            }
            if(this.lastRead.contains("\\a")){
                this.lastRead=this.lastRead.replace("\\a","");
            }

            if(this.lastRead.contains("\r")){
                this.lastRead=this.lastRead.replace("\r","");
            }
            if(this.lastRead.contains("\t")){
                this.lastRead=this.lastRead.replace("\t","");
            }
            if(this.lastRead.contains("\n")){
                this.lastRead=this.lastRead.replace("\n","");
            }
            break;
        }if (StringUtil.equalsAny(str,"\r") && "rz".equals(this.lastRead)) {

            System.out.println("拦截rz-1");
        } else if (StringUtil.containsAny(builder.toString(), "rz\r", "rz\n")) {
            System.out.println("拦截rz");
        } else {
            this.shellWriter.write(str);
            this.shellWriter.flush();
        }
        if (str.endsWith("\r") || str.endsWith("\n")) {
            builder.setLength(0);
        }
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
            ThreadUtil.startVirtual(() -> this.client.resolveWorkerDir(new String(buf, offset, len)));
        }
        lastRead = str;
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