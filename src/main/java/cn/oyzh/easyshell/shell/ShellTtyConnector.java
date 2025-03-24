package cn.oyzh.easyshell.shell;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.terminal.DefaultTtyConnector;
import com.pty4j.PtyProcess;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellTtyConnector extends DefaultTtyConnector {

    private InputStreamReader shellReader;

    private OutputStreamWriter shellWriter;

    public void initShell(ShellShell shell) throws IOException {
        this.shellReader = new InputStreamReader(shell.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getOutputStream(), this.myCharset);
    }

    public ShellTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
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
            this.doRead(buf, offset, len);
        }
        return len;
    }

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
    }
}