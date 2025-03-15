package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.ssh.ShellClient;
import cn.oyzh.easyshell.ssh.SSHShell;
import cn.oyzh.easyshell.ssh.ShellTermWidget;
import cn.oyzh.easyshell.ssh.ShellTtyConnector;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.JSchException;
import com.techsenger.jeditermfx.core.util.TermSize;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellTermTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    /**
     * 终端组件
     */
    private ShellTermWidget widget;

    private void initWidget(SSHShell shell) throws IOException {
        this.widget = new ShellTermWidget();
        ShellTtyConnector connector = (ShellTtyConnector) this.widget.createTtyConnector();
        connector.initShell(shell);
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new DefaultHyperlinkFilter());
        this.root.setChild(this.widget.getPane());
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> this.initShellSize());
    }

    private void initShellSize() {
        int sizeW = (int) this.widget.getWidth();
        int sizeH = (int) this.widget.getHeight();
        TermSize termSize = this.widget.getTermSize();
        SSHShell shell = this.client().openShell();
        shell.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }

    public void init() throws IOException, JSchException {
        ShellClient client = this.client();
        SSHShell shell = client.openShell();
        this.initWidget(shell);
        shell.connect(client.connectTimeout());
        if (!shell.isConnected()) {
            MessageBox.warn(I18nHelper.connectFail());
        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.widget.close();
    }

    @Override
    public ShellConnectTabController parent() {
        return (ShellConnectTabController) super.parent();
    }

    public ShellClient client() {
        return this.parent().client();
    }
}
