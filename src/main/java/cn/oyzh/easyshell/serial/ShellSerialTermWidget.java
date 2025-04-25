package cn.oyzh.easyshell.serial;

import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialTermWidget extends ShellDefaultTermWidget {

    @Override
    public ShellSerialTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new ShellSerialTtyConnector(process, charset, Arrays.asList(command));
    }
}
