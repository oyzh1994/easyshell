package cn.oyzh.easyshell.ssh2;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;
import com.jediterm.core.util.TermSize;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellSSHTermWidget extends ShellStreamTermWidget {

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
