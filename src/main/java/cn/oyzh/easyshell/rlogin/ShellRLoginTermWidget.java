package cn.oyzh.easyshell.rlogin;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-05-27
 */
public class ShellRLoginTermWidget extends ShellStreamTermWidget {

    public ShellRLoginTtyConnector createTtyConnector(ShellRLoginClient client) throws IOException {
        return new ShellRLoginTtyConnector(client);
    }

    @Override
    public ShellRLoginTtyConnector getTtyConnector() {
        return (ShellRLoginTtyConnector) super.getTtyConnector();
    }
}
