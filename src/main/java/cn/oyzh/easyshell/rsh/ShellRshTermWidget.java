package cn.oyzh.easyshell.rsh;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;

import java.io.IOException;

/**
 * rsh终端组件，负责创建rsh终端的tty连接器
 *
 * @author oyzh
 * @since 2026-11-14
 */
public class ShellRshTermWidget extends ShellStreamTermWidget {

    /**
     * 创建rsh终端tty连接器
     *
     * @param client rsh客户端
     * @return tty连接器
     * @throws IOException 异常
     */
    public ShellRshTtyConnector createTtyConnector(ShellRshClient client) throws IOException {
        return new ShellRshTtyConnector(client);
    }

    @Override
    public ShellRshTtyConnector getTtyConnector() {
        return (ShellRshTtyConnector) super.getTtyConnector();
    }
}
