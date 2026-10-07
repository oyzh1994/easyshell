package cn.oyzh.easyshell.local;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.ShellProcessTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.util.List;

/**
 * 本地终端组件，负责创建本地终端的tty连接器
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellLocalTermWidget extends ShellProcessTermWidget {

    /**
     * 创建本地终端tty连接器
     *
     * @param client 本地客户端
     * @return tty连接器
     * @throws IOException 异常
     */
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
