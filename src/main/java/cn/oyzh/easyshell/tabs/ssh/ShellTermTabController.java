package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.ssh.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.jeditermfx.terminal.ui.FXHyperlinkFilter;
import com.jcraft.jsch.JSchException;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.ui.FXFXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * shell终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/11
 */
public class ShellTermTabController extends SubTabController {

    /**
     * 终端组件
     */
    @FXML
    private ShellSSHTermWidget widget;

    /**
     * 初始化组件
     *
     * @throws IOException 异常
     */
    private void initWidget() throws IOException {
        ShellSSHShell shell = this.client().getShell();
        Charset charset = this.client().getCharset();
        ShellSSHTtyConnector connector = this.widget.createTtyConnector(charset);
        connector.initShell(shell);
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new FXHyperlinkFilter());
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> this.initShellSize());
    }

    private void initShellSize() {
        int sizeW = (int) this.widget.getTerminalPanel().getWidth();
        int sizeH = (int) this.widget.getTerminalPanel().getHeight();
        TermSize termSize = this.widget.getTermSize();
        ShellSSHShell shell = this.client().getShell();
        shell.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        ShellConnect connect = this.client().getShellConnect();
        FXFXTerminalPanel terminalPanel = this.widget.getTerminalPanel();
        // 处理背景
        ShellConnectUtil.initBackground(connect, terminalPanel);
    }

    public void init() throws IOException, JSchException {
        ShellSSHClient client = this.client();
        ShellSSHShell shell = client.openShell();
        this.initWidget();
        shell.connect(client.connectTimeout());
        if (!shell.isConnected()) {
            MessageBox.warn(I18nHelper.connectFail());
            this.closeTab();
            return;
        }
        // 异步加载背景
        ThreadUtil.startVirtual(this::initBackground);
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.widget.close();
    }

    @Override
    public ShellSSHTabController parent() {
        return (ShellSSHTabController) super.parent();
    }

    public ShellSSHClient client() {
        return this.parent().getClient();
    }
}
