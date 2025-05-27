package cn.oyzh.easyshell.rlogin;

import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-05-27
 */
public class ShellRLoginTermWidget extends ShellDefaultTermWidget {

    @Override
    public ShellRLoginTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new ShellRLoginTtyConnector(process, charset, Arrays.asList(command));
    }
}
