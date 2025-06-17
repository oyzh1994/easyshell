package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import cn.oyzh.easyshell.zmodem.ZModemPtyConnectorAdaptor;
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

    private ZModemPtyConnectorAdaptor connectorAdaptor;

    public ShellSSHClient getClient() {
        return client;
    }

    public ZModemPtyConnectorAdaptor getConnectorAdaptor() {
        return connectorAdaptor;
    }

    public void setConnectorAdaptor(ZModemPtyConnectorAdaptor connectorAdaptor) {
        this.connectorAdaptor = connectorAdaptor;
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
        if (connectorAdaptor != null) {
            connectorAdaptor.read(buf, offset, length);
        }
        int len;
        if (this.shellReader == null) {
            len = super.read(buf, offset, length);
        } else {
            len = this.shellReader.read(buf, offset, length);
        }
        if (len > 0) {
            this.doRead(buf, offset, len);
        }
        return len;
    }

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
        if (connectorAdaptor != null) {
            connectorAdaptor.write(str);
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
        if (connectorAdaptor != null) {
            connectorAdaptor.write(bytes);
        }
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
    protected void doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        if (this.client != null) {
            ThreadUtil.startVirtual(() -> this.client.resolveWorkerDir(new String(buf, offset, len)));
        }
    }

    @Override
    public InputStream input() throws IOException {
        return this.client.getShell().getInputStream();
    }

    @Override
    public OutputStream output() throws IOException {
        return this.client.getShell().getOutputStream();
    }
}