package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;
import com.jediterm.core.util.TermSize;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellTelnetTermWidget extends ShellStreamTermWidget {

    public ShellTelnetTtyConnector createTtyConnector(ShellTelnetClient client) throws IOException {
        ShellTelnetTtyConnector connector = new ShellTelnetTtyConnector(client);
        // 监听终端大小
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> this.initPtySize());
        return connector;
    }

    @Override
    public ShellTelnetTtyConnector getTtyConnector() {
        return (ShellTelnetTtyConnector) super.getTtyConnector();
    }

    public ShellTelnetClient client() {
        ShellTelnetTtyConnector connector = this.getTtyConnector();
        return connector == null ? null : connector.getClient();
    }

    /**
     * 初始化终端大小
     */
    public void initPtySize() {
        ShellTelnetClient client = this.client();
        if (client == null) {
            return;
        }
        TermSize termSize = this.getTermSize();
        if (termSize == null) {
            return;
        }
        client.setPtySize(termSize.getColumns(), termSize.getRows());
    }
}
