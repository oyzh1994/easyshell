package cn.oyzh.easyshell.ssh2;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;
import com.jediterm.core.util.TermSize;

/**
 * ssh终端组件，负责创建ssh终端的tty连接器
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSSHTermWidget extends ShellStreamTermWidget {

    /**
     * 创建ssh终端tty连接器
     *
     * @param client ssh客户端
     * @return tty连接器
     * @throws Exception 异常
     */
    public ShellSSHTtyConnector createTtyConnector(ShellSSHClient client) throws Exception {
        ShellSSHTtyConnector connector = new ShellSSHTtyConnector(client);
        // 监听终端大小
        connector.terminalSizeProperty().addListener((observable) -> this.initPtySize());
        return connector;
    }

    @Override
    public ShellSSHTtyConnector getTtyConnector() {
        return (ShellSSHTtyConnector) super.getTtyConnector();
    }

    /**
     * 获取ssh客户端
     *
     * @return ssh客户端
     */
    public ShellSSHClient client() {
        ShellSSHTtyConnector connector = this.getTtyConnector();
        return connector == null ? null : connector.getClient();
    }

    /**
     * 初始化终端大小
     */
    public void initPtySize() {
        ShellSSHClient client = this.client();
        if (client == null) {
            return;
        }
        TermSize termSize = this.getTermSize();
        if (termSize == null) {
            return;
        }
        int sizeW = (int) this.getTerminalPanel().getWidth();
        int sizeH = (int) this.getTerminalPanel().getHeight();
        client.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }
}
