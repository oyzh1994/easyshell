package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellTelnetClient extends org.apache.commons.net.telnet.TelnetClient implements BaseClient {

    private final ShellConnect shellConnect;

    public ShellTelnetClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    @Override
    public void start(int timeout) throws IOException {
        this.setConnectTimeout(timeout);
        this.setCharset(BaseClient.super.getCharset());
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

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }
}
