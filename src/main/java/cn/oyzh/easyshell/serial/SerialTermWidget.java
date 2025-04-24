package cn.oyzh.easyshell.serial;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class SerialTermWidget extends ShellDefaultTermWidget {

    @Override
    public SerialTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new SerialTtyConnector(process, charset, Arrays.asList(command));
    }

    @Override
    public void close() {
        super.close();
        SystemUtil.gc();
    }
}
