package cn.oyzh.easyshell.rlogin;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;

import java.io.IOException;

/**
 * rlogin终端组件，负责创建rlogin终端的tty连接器
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellRLoginTermWidget extends ShellStreamTermWidget {

    /**
     * 创建rlogin终端tty连接器
     *
     * @param client rlogin客户端
     * @return tty连接器
     * @throws IOException 异常
     */
    public ShellRLoginTtyConnector createTtyConnector(ShellRLoginClient client) throws IOException {
        return new ShellRLoginTtyConnector(client);
    }

    @Override
    public ShellRLoginTtyConnector getTtyConnector() {
        return (ShellRLoginTtyConnector) super.getTtyConnector();
    }
}
