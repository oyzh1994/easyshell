package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.ssh.SSHTermWidget;
import cn.oyzh.easyssh.ssh.SSHTtyConnector;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.techsenger.jeditermfx.core.util.TermSize;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class SSHTermTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    /**
     * 终端组件
     */
    private SSHTermWidget widget;

    private void initWidget(ChannelShell shell) throws IOException {
        this.widget = new SSHTermWidget(new DefaultSettingsProvider());
        SSHTtyConnector connector = (SSHTtyConnector) this.widget.createTtyConnector();
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
        ChannelShell shell = this.client().openShell();
        shell.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }

    public void init() throws IOException, JSchException {
        SSHClient client = this.client();
        ChannelShell shell = client.openShell();
        this.initWidget(shell);
        shell.connect(client.connectTimeout());
        if (!shell.isConnected()) {
            MessageBox.warn(I18nHelper.connectFail());
        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client().close();
        this.widget.close();
    }

    @Override
    public SSHConnectTabController parent() {
        return (SSHConnectTabController) super.parent();
    }

    public SSHClient client() {
        return this.parent().client();
    }
}
