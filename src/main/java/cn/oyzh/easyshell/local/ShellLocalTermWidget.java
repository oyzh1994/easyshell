package cn.oyzh.easyshell.local;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.ShellProcessTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellLocalTermWidget extends ShellProcessTermWidget {

    public ShellLocalTtyConnector createTtyConnector(ShellLocalClient client) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        ShellConnect shellConnect = client.getShellConnect();
        // 初始化部分参数
        if (shellConnect.getTermType() != null) {
            this.putEnvironment("TERM", shellConnect.getTermType());
        } else {
            this.putEnvironment("TERM", "xterm-256color");
        }
        if (client.getCharset() != null) {
            this.putEnvironment("LANG", "en_US." + client.getCharset());
        }
        return new ShellLocalTtyConnector(client, process, List.of(command));
    }

    @Override
    public ShellLocalTtyConnector getTtyConnector() {
        return (ShellLocalTtyConnector) super.getTtyConnector();
    }
}
