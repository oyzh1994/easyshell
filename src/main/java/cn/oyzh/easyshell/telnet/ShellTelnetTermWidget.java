package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.jediterm.core.util.TermSize;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellTelnetTermWidget extends ShellDefaultTermWidget {

    @Override
    public ShellTelnetTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        ShellTelnetTtyConnector connector = new ShellTelnetTtyConnector(process, charset, Arrays.asList(command));
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
        client.setPtySize(termSize.getColumns(), termSize.getRows());
    }
}
