package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class TelnetTermWidget extends ShellDefaultTermWidget {

    @Override
    public TelnetTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new TelnetTtyConnector(process, charset, Arrays.asList(command));
    }
}
