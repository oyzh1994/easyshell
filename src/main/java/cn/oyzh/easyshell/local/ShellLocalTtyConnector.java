package cn.oyzh.easyshell.local;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.fx.tty.TtyProcessTtyConnector;
import com.pty4j.PtyProcess;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellLocalTtyConnector extends TtyProcessTtyConnector {

    private ShellLocalClient client;

    public ShellLocalClient getClient() {
        return client;
    }

    public ShellLocalTtyConnector(ShellLocalClient client, PtyProcess process, List<String> commandLines) {
        super(process, client.getCharset(), commandLines);
        this.client = client;
    }

    @Override
    public String getName() {
        return "local-tty";
    }

    @Override
    public void close() {
        super.close();
        IOUtil.close(this.client);
        this.client = null;
    }
}