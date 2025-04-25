package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class SSHTermWidget extends ShellDefaultTermWidget {

    @Override
    public SSHTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new SSHTtyConnector(process, charset, Arrays.asList(command));
    }
}
