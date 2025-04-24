package cn.oyzh.easyshell.local;

import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class LocalTermWidget extends ShellDefaultTermWidget {

    @Override
    public LocalTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new LocalTtyConnector(process, charset, Arrays.asList(command));
    }

}
