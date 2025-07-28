package cn.oyzh.easyshell.tabs.rlogin;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.rlogin.ShellRLoginClient;
import cn.oyzh.easyshell.rlogin.ShellRLoginTermWidget;
import cn.oyzh.easyshell.rlogin.ShellRLoginTtyConnector;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.tabs.ShellSnippetAdapter;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * shell连接rlogin内容组件
 *
 * @author oyzh
 * @since 2025/04/24
 */
public class ShellRLoginTabController extends RichTabController  implements ShellSnippetAdapter{

    /**
     * 终端组件
     */
    @FXML
    private ShellRLoginTermWidget widget;

    /**
     * rlogin客户端
     */
    private ShellRLoginClient client;

    public ShellRLoginClient getClient() {
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
        ShellRLoginTtyConnector connector = this.widget.createTtyConnector(charset);
        this.widget.openSession(connector);
        // this.widget.onTermination(exitCode -> this.widget.close());
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
        this.client = new ShellRLoginClient(connect);
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
            } catch (Throwable ex) {
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

    /**
     * 片段列表
     *
     * @param event 事件
     */
    @FXML
    private void snippet(MouseEvent event) {
        ShellSnippetAdapter.super.snippetList((Node) event.getSource());
    }

    @Override
    public void runSnippet(String content) throws IOException {
        this.widget.getTtyConnector().write(content);
    }
}
