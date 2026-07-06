package cn.oyzh.easyshell.mosh;

import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.jediterm.core.util.TermSize;
import com.pty4j.PtyProcess;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellMoshTermWidget extends ShellDefaultTermWidget {

    @Override
    public ShellMoshTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        ShellMoshTtyConnector connector = new ShellMoshTtyConnector(process, charset, Arrays.asList(command));
        // 监听终端大小
        connector.terminalSizeProperty().addListener((observable) -> this.initPtySize());
        return connector;
    }

    @Override
    public ShellMoshTtyConnector getTtyConnector() {
        return (ShellMoshTtyConnector) super.getTtyConnector();
    }

    public ShellMoshClient client() {
        ShellMoshTtyConnector connector = this.getTtyConnector();
        return connector == null ? null : connector.getClient();
    }

    /**
     * 初始化终端大小
     */
    public void initPtySize() {
        ShellMoshClient client = this.client();
        if (client == null) {
            return;
        }
        TermSize termSize = this.getTermSize();
        int sizeW = (int) this.getTerminalPanel().getWidth();
        int sizeH = (int) this.getTerminalPanel().getHeight();
        client.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }

    @Override
    public void initNode() {
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            byte[] seq = ShellMoshHelper.mapKeyToAnsiSequence(event);
            if (seq != null && this.client() != null) {
                this.client().sendUserInput(seq);
            }
        });
        super.initNode();
    }


}
