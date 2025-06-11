package cn.oyzh.easyshell.tabs.split;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.connect.ShellSSHConnectTextField;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.ssh.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 终端分屏-终端tab内容组件
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
    private ShellSSHConnectTextField connect;

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
        if (!this.connect.validate()) {
            return;
        }
        this.destroy();
        ShellConnect connect = this.connect.getSelectedItem();
        StageManager.showMask(() -> this.doConnect(connect));
    }

    /**
     * 执行连接
     *
     * @param connect 连接
     */
    public void doConnect(ShellConnect connect) {
        try {
            // 外部选择的连接
            if (this.connect.getSelectedItem() != connect) {
                this.connect.select(connect);
            }
            this.client = new ShellSSHClient(connect);
            this.client.start();
            if (this.client.isConnected()) {
                this.init();
            } else {
                this.destroy();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
            this.destroy();
        }
    }
}
