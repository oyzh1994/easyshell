package cn.oyzh.easyshell.tabs.telnet;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;
import cn.oyzh.easyshell.telnet.ShellTelnetTermWidget;
import cn.oyzh.easyshell.telnet.ShellTelnetTtyConnector;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.easyshell.zmodem.ZModemTtyConnector;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * shell连接telnet内容组件
 *
 * @author oyzh
 * @since 2025/04/24
 */
public class ShellTelnetTabController extends RichTabController {

    /**
     * 终端组件
     */
    @FXML
    private ShellTelnetTermWidget widget;

    /**
     * telnet客户端
     */
    private ShellTelnetClient client;

    public ShellTelnetClient getClient() {
        return client;
    }

    private ShellConnect shellConnect;

    public ShellConnect shellConnect() {
        return shellConnect;
    }


    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 初始化组件
     *
     * @throws IOException io异常
     */
    private void initWidget() throws IOException {
        Charset charset = this.client.getCharset();
        ShellTelnetTtyConnector connector = this.widget.createTtyConnector(charset);
        // ZModemTtyConnector zModemTtyConnector= this.widget.createZModemTtyConnector(connector);
        // this.widget.openSession(zModemTtyConnector);
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        // 初始化一次pty大小
        this.widget.initPtySize();
        // 初始化
        connector.init(this.client);
    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        ShellConnect connect = this.client.getShellConnect();
        FXTerminalPanel terminalPanel = this.widget.getTerminalPanel();
        // 处理背景
        ShellConnectUtil.initBackground(connect, terminalPanel);
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.shellConnect = connect;
        this.client = new ShellTelnetClient(connect);
        StageManager.showMask(() -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                // 收起左侧
                if (this.setting.isHiddenLeftAfterConnected()) {
                    ShellEventUtil.layout1();
                }
                // 初始化组件
                this.initWidget();
                // 异步加载背景
                ThreadUtil.startVirtual(this::initBackground);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.getClient().close();
        this.widget.close();
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }
}
