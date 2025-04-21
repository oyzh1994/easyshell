package cn.oyzh.easyshell.shell;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.jediterm.terminal.TtyConnector;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellTermWidget extends ShellDefaultTermWidget {

    @Override
    public TtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new ShellTtyConnector(process, charset, Arrays.asList(command));
    }

    @Override
    public void close() {
        super.close();
        SystemUtil.gc();
    }
}
