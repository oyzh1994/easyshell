package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.domain.ShellConnect;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class TelnetClient extends org.apache.commons.net.telnet.TelnetClient implements AutoCloseable {

    private final ShellConnect shellConnect;

    public TelnetClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    public void start() throws IOException {
        super.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort());
    }

    @Override
    public void close() {
        try {
            this.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ShellConnect getShellConnect() {
        return shellConnect;
    }
}
