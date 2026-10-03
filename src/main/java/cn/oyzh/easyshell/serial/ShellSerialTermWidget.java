package cn.oyzh.easyshell.serial;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialTermWidget extends ShellStreamTermWidget {

    public ShellSerialTtyConnector createTtyConnector(ShellSerialClient client) throws IOException {
        return new ShellSerialTtyConnector(client);
    }

    @Override
    public ShellSerialTtyConnector getTtyConnector() {
        return (ShellSerialTtyConnector) super.getTtyConnector();
    }
}
