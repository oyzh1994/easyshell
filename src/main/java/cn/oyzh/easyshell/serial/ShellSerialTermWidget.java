package cn.oyzh.easyshell.serial;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;

import java.io.IOException;

/**
 * 串口终端组件
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialTermWidget extends ShellStreamTermWidget {

    /**
     * 创建Tty连接器
     *
     * @param client 串口客户端
     * @return Tty连接器
     * @throws IOException IO异常
     */
    public ShellSerialTtyConnector createTtyConnector(ShellSerialClient client) throws IOException {
        return new ShellSerialTtyConnector(client);
    }

    @Override
    public ShellSerialTtyConnector getTtyConnector() {
        return (ShellSerialTtyConnector) super.getTtyConnector();
    }
}
