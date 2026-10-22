package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;
import com.jediterm.core.util.TermSize;

public class ShellSSHTermWidget extends ShellStreamTermWidget {

    public ShellSSHTtyConnector createTtyConnector(ShellSSHClient client) throws Exception {
        ShellSSHTtyConnector connector = new ShellSSHTtyConnector(client);
        connector.terminalSizeProperty().addListener(observable -> this.initPtySize());
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

    public void initPtySize() {
        ShellSSHClient client = this.client();
        if (client == null) {
            return;
        }
        TermSize termSize = this.getTermSize();
        if (termSize == null) {
            return;
        }
        int width = (int) this.getTerminalPanel().getWidth();
        int height = (int) this.getTerminalPanel().getHeight();
        client.setPtySize(termSize.getColumns(), termSize.getRows(), width, height);
    }
}
