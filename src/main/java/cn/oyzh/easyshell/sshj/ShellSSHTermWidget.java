package cn.oyzh.easyshell.sshj;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.jediterm.core.util.TermSize;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellSSHTermWidget extends ShellDefaultTermWidget {

    @Override
    public ShellSSHTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        ShellSSHTtyConnector connector = new ShellSSHTtyConnector(process, charset, Arrays.asList(command));
        // 监听终端大小
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> this.initPtySize());
        return connector;
    }

    @Override
    public ShellSSHTtyConnector getTtyConnector() {
        return (ShellSSHTtyConnector) super.getTtyConnector();
    }

    public cn.oyzh.easyshell.ssh.ShellSSHClient client() {
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
        ShellSSHShell shell = client.getShell();
        TermSize termSize = this.getTermSize();
        int sizeW = (int) this.getTerminalPanel().getWidth();
        int sizeH = (int) this.getTerminalPanel().getHeight();
        shell.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }
}
