package cn.oyzh.easyshell.tabs.split;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.connect.ShellConnectTextField;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.local.ShellLocalClient;
import cn.oyzh.easyshell.local.ShellLocalTermWidget;
import cn.oyzh.easyshell.mosh.ShellMoshClient;
import cn.oyzh.easyshell.mosh.ShellMoshTermWidget;
import cn.oyzh.easyshell.rlogin.ShellRLoginClient;
import cn.oyzh.easyshell.rlogin.ShellRLoginTermWidget;
import cn.oyzh.easyshell.serial.ShellSerialClient;
import cn.oyzh.easyshell.serial.ShellSerialTermWidget;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh2.ShellSSHTtyConnector;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;
import cn.oyzh.easyshell.telnet.ShellTelnetTermWidget;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.tty.TtyTermWidget;
import cn.oyzh.fx.tty.TtyTerminalUtil;
import com.jediterm.terminal.TtyConnector;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * 终端分屏-终端tab内容组件
 *
 * @author oyzh
 * @since 2025/05/29
 */
public class ShellSplitTermController extends SubTabController {

    /**
     * 客户端
     */
    private ShellBaseClient client;

    /**
     * 终端组件
     */
    private TtyTermWidget widget;

    /**
     * 终端容器
     */
    @FXML
    private FXHBox termBox;

    /**
     * 连接组件
     */
    @FXML
    private ShellConnectTextField connect;

    /**
     * 初始化组件
     *
     * @throws IOException 异常
     */
    private void initWidget() throws Exception {
        TtyConnector ttyConnector = null;
        if (this.client instanceof ShellSSHClient sshClient) {
            ShellSSHTermWidget widget = new ShellSSHTermWidget();
            ShellSSHTtyConnector connector = widget.createTtyConnector(sshClient);
            if (sshClient.getShellConnect().isEnableZModem()) {
                ttyConnector = TtyTerminalUtil.createZModemTtyConnector(widget, connector);
            } else {
                ttyConnector = connector;
            }
            this.widget = widget;
        } else if (this.client instanceof ShellRLoginClient rLoginClient) {
            ShellRLoginTermWidget widget = new ShellRLoginTermWidget();
            ttyConnector = widget.createTtyConnector(rLoginClient);
            this.widget = widget;
        } else if (this.client instanceof ShellTelnetClient telnetClient) {
            ShellTelnetTermWidget widget = new ShellTelnetTermWidget();
            ttyConnector = widget.createTtyConnector(telnetClient);
            this.widget = widget;
        } else if (this.client instanceof ShellSerialClient serialClient) {
            ShellSerialTermWidget widget = new ShellSerialTermWidget();
            ttyConnector = widget.createTtyConnector(serialClient);
            this.widget = widget;
        } else if (this.client instanceof ShellMoshClient moshClient) {
            ShellMoshTermWidget widget = new ShellMoshTermWidget();
            ttyConnector = widget.createTtyConnector(moshClient);
            this.widget = widget;
        } else if (this.client instanceof ShellLocalClient localClient) {
            ShellLocalTermWidget widget = new ShellLocalTermWidget();
            ttyConnector = widget.createTtyConnector(localClient);
            this.widget = widget;
        }
        // 初始化退格码
        this.widget.initBackspaceCode(this.shellConnect().getBackspaceType());
        // 设置alt修饰
        this.widget.setAltSendsEscape(this.shellConnect().isAltSendsEscape());
        // this.widget.setAlwaysShowThumbs(true);
        this.widget.openSession(ttyConnector);
        this.widget.setFlexWidth("100%");
        this.widget.setFlexHeight("100%");
        // this.widget.onTermination(exitCode -> this.widget.close());
    }

    /**
     * 执行初始化
     *
     * @throws Exception 异常
     */
    private void init() throws Exception {
        // if (this.client instanceof ShellSSHClient sshClient) {
        // ChannelShell shell = sshClient.openShell();
        // this.initWidget();
        // shell.connect(this.client.connectTimeout());
        // if (!shell.isConnected()) {
        //     MessageBox.warn(I18nHelper.connectFail());
        //     return;
        // }
        // } else {
        this.initWidget();
        // }
        this.termBox.addChild(this.widget);
    }

    // @Override
    // public void destroy() {
    //     if (this.widget != null) {
    //         this.widget.close();
    //         this.widget = null;
    //     }
    //     if (this.client != null) {
    //         IOUtil.close(this.client);
    //         this.client = null;
    //     }
    //     this.termBox.clearChild();
    //     super.destroy();
    // }

    @Override
    public void onTabClosed(Event event) {
        // if (this.widget != null) {
        //     this.widget.close();
        //     this.widget = null;
        // }
        // if (this.client != null) {
        //     IOUtil.close(this.client);
        //     this.client = null;
        // }
        this.destroyTerm();
        super.onTabClosed(event);
        // this.destroy();
    }

    /**
     * 销毁终端
     */
    private void destroyTerm() {
        if (this.widget != null) {
            this.widget.close();
            this.widget = null;
        }
        if (this.client != null) {
            IOUtil.close(this.client);
            this.client = null;
        }
        this.termBox.clearChild();
    }

    /**
     * 执行连接
     */
    @FXML
    private void doConnect() {
        if (!this.connect.validate()) {
            return;
        }
        this.destroyTerm();
        // this.destroy();
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
                this.connect.selectItem(connect);
            }
            this.client = ShellClientUtil.newClient(connect);
            this.client.start();
            if (this.client.isConnected()) {
                this.init();
            } else {
                this.destroyTerm();
            }
        } catch (Throwable ex) {
            MessageBox.exception(ex);
            this.destroyTerm();
        }
    }

    /**
     * 运行片段
     *
     * @param content 内容
     */
    public void runSnippet(String content) throws IOException {
        this.widget.getTtyConnector().write(content);
    }

    private ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    //    @Override
    //    public void destroy() {
    //        if (this.widget != null) {
    //            this.widget.destroy();
    //        }
    //        super.destroy();
    //    }
}
