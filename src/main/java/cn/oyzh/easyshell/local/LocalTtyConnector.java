package cn.oyzh.easyshell.local;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.pty4j.PtyProcess;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class LocalTtyConnector extends ShellDefaultTtyConnector {

    public void initLocal(ShellConnect connect) {

    }

    public LocalTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

}