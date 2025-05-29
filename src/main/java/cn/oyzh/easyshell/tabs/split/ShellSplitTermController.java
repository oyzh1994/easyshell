package cn.oyzh.easyshell.tabs.split;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.connect.ShellSSHConnectComboBox;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.ssh.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXHyperlinkFilter;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 分屏-终端tab内容组件
 *
 * @author oyzh
 * @since 2025/05/29
 */
public class ShellSplitTermController extends SubTabController {

    /**
     * ssh客户端
     */
    private ShellSSHClient client;

    /**
     * 终端组件
     */
    @FXML
    private ShellSSHTermWidget widget;

    /**
     * 终端容器
     */
    @FXML
    private FXHBox termBox;

    /**
     * ssh连接组件
     */
    @FXML
    private ShellSSHConnectComboBox connectionBox;

    /**
     * 初始化组件
     *
     * @throws IOException 异常
     */
    private void initWidget() throws IOException {
        this.widget = new ShellSSHTermWidget();
        this.widget.setFlexWidth("100%");
        this.widget.setFlexHeight("100%");
        Charset charset = this.client.getCharset();
        ShellSSHTtyConnector connector = this.widget.createTtyConnector(charset);
        connector.initShell(this.client);
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new FXHyperlinkFilter());
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> this.initShellSize());
    }

    /**
     * 初始化终端大小
     */
    private void initShellSize() {
        int sizeW = (int) this.widget.getTerminalPanel().getWidth();
        int sizeH = (int) this.widget.getTerminalPanel().getHeight();
        TermSize termSize = this.widget.getTermSize();
        ShellSSHShell shell = this.client.getShell();
        shell.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }

    /**
     * 执行初始化
     *
     * @throws Exception 异常
     */
    private void init() throws Exception {
        ShellSSHShell shell = this.client.openShell();
        this.initWidget();
        shell.connect(this.client.connectTimeout());
        if (!shell.isConnected()) {
            MessageBox.warn(I18nHelper.connectFail());
            return;
        }
        ShellConnect shellConnect = this.client.getShellConnect();
        // macos需要初始化部分参数
        if (this.client.isMacos() && shellConnect.getCharset() != null) {
            TtyConnector connector = this.widget.getTtyConnector();
            connector.write("export LANG=en_US." + shellConnect.getCharset() + "\n");
        }
        this.initShellSize();
        this.termBox.addChild(this.widget);
    }

    /**
     * 销毁
     */
    private void destroy() {
        if (this.widget != null) {
            this.widget.close();
            this.widget = null;
        }
        if (this.client != null) {
            this.client.close();
            this.client = null;
        }
        this.termBox.clearChild();
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.destroy();
    }

    /**
     * 执行连接
     */
    @FXML
    private void doConnect() {
        this.destroy();
        if (!this.connectionBox.validate()) {
            return;
        }
        ShellConnect connect = this.connectionBox.getSelectedItem();
        this.client = new ShellSSHClient(connect);
        StageManager.showMask(() -> {
            try {
                this.client.start();
                if (this.client.isConnected()) {
                    this.init();
                }
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }
}
