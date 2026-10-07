package cn.oyzh.easyshell.mosh;

import cn.oyzh.easyshell.terminal.ShellStreamTermWidget;
import com.jediterm.core.util.TermSize;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

/**
 * mosh终端组件，负责创建mosh终端的tty连接器
 *
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellMoshTermWidget extends ShellStreamTermWidget {

    /**
     * 创建mosh终端tty连接器
     *
     * @param client mosh客户端
     * @return tty连接器
     * @throws IOException 异常
     */
    public ShellMoshTtyConnector createTtyConnector(ShellMoshClient client) throws IOException {
        ShellMoshTtyConnector connector = new ShellMoshTtyConnector(client);
        // 监听终端大小
        connector.terminalSizeProperty().addListener((observable) -> this.initPtySize());
        return connector;
    }

    @Override
    public ShellMoshTtyConnector getTtyConnector() {
        return (ShellMoshTtyConnector) super.getTtyConnector();
    }

    /**
     * 获取mosh客户端
     *
     * @return mosh客户端
     */
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
        if (termSize == null) {
            return;
        }
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
                event.consume();
            }
        });
        //        this.addEventFilter(KeyEvent.KEY_TYPED, event -> {
        //            String ch = event.getCharacter();
        //            if (ch != null && !ch.isEmpty() && this.client() != null) {
        //                this.client().sendUserInput(ch.getBytes());
        //            }
        //            event.consume();
        //        });
        super.initNode();
    }


}
