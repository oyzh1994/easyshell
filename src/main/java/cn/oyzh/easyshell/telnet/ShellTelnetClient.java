package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellTelnetClient extends TelnetClient implements BaseClient {

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

    private WindowSizeOptionHandler sizeHandler;

    public void setPtySize(int cols, int rows) {
        try {
            if (this.sizeHandler != null) {
                super.deleteOptionHandler(this.sizeHandler.getOptionCode());
                this.sizeHandler = null;
            }
            this.sizeHandler = new WindowSizeOptionHandler(cols, rows, true, true, true, true);
            super.addOptionHandler(this.sizeHandler);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }
}
