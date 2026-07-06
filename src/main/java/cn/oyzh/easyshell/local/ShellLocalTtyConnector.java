package cn.oyzh.easyshell.local;

import cn.oyzh.fx.tty.TtyDefaultTtyConnector;
import com.pty4j.PtyProcess;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellLocalTtyConnector extends TtyDefaultTtyConnector {

    public void init(ShellLocalClient client) {

    }

    public ShellLocalTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

}