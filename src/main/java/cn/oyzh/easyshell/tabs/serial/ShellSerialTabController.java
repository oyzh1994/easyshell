package cn.oyzh.easyshell.tabs.serial;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.serial.ShellSerialClient;
import cn.oyzh.easyshell.serial.ShellSerialTermWidget;
import cn.oyzh.easyshell.serial.ShellSerialTtyConnector;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.tabs.ShellSnippetAdapter;
import cn.oyzh.easyshell.terminal.ShellTermWidgetUtil;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

/**
 * shell连接串口内容组件
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialTabController extends ShellBaseTabController implements ShellSnippetAdapter {

    /**
     * 终端组件
     */
    @FXML
    private ShellSerialTermWidget widget;

    /**
     * 终端大小
     */
    @FXML
    private FXText termSize;

    /**
     * serial客户端
     */
    private ShellSerialClient client;

    // /**
    //  * 设置
    //  */
    // private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellSerialClient getClient() {
        return client;
    }

    /**
     * 获取连接
     *
     * @return 连接
     */
    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    /**
     * 初始化组件
     *
     * @throws IOException io异常
     */
    private void initWidget() throws IOException {
        this.initWidget(this.widget, this.client);
    }

    /**
     * 初始化终端组件
     *
     * @param widget 终端组件
     * @param client 客户端
     * @throws IOException io异常
     */
    private void initWidget(ShellSerialTermWidget widget, ShellSerialClient client) throws IOException {
        ShellSerialTtyConnector connector = widget.createTtyConnector(client);
        // 监听窗口大小
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.termSize.text(newValue.getRows() + "x" + newValue.getColumns());
            }
        });
        // 初始化退格码
        widget.initBackspaceCode(this.shellConnect().getBackspaceType());
        // 设置alt修饰
        widget.setAltSendsEscape(this.shellConnect().isAltSendsEscape());
        // 打开回话
        widget.openSession(connector);
        // 聚焦
        widget.requestFocus();
    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        FXTerminalPanel terminalPanel = this.widget.getTerminalPanel();
        // 处理背景
        //        ShellConnectUtil.initBackground(connect, terminalPanel);
        ShellConnectUtil.initTermBackground(terminalPanel);
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = ShellClientUtil.newClient(connect);
        this.initClientListener(this.client);
        StageManager.showMask(this.getWindow(), () -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail()
                            + " errCode: " + this.client.getLastErrorCode()
                            + " errLocation: " + this.client.getLastErrorLocation(), this.getWindow());
                    this.closeTab();
                    return;
                }
                // 默认发送一次换行符，因为串口可能未默认返回内容
                this.client.write("\r");
                // 收起左侧
                // if (this.setting.isHiddenLeftAfterConnected()) {
                //     ShellEventUtil.layout1();
                // }
                this.hideLeft();
                // 初始化组件
                this.initWidget();
                // 异步加载背景
                ThreadUtil.start(this::initBackground);
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
                this.closeTab();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        if (this.widget != null) {
            this.widget.close();
        }
        IOUtil.closeAsync(this.client);
        // // 展开左侧
        // if (this.setting.isHiddenLeftAfterConnected()) {
        //     ShellEventUtil.layout2();
        // }
    }

    /**
     * 刷新
     *
     * @param event 事件
     */
    @FXML
    private void refresh(MouseEvent event) {
        StageManager.showMask(this.getWindow(), this::refreshWidget);
    }

    /**
     * 原位刷新终端，保留当前标签页。
     */
    private void refreshWidget() {
        ShellSerialClient oldClient = this.client;
        ShellSerialClient newClient = null;
        try {
            ShellConnect connect = this.shellConnect();
            ShellSerialTermWidget oldWidget = this.widget;
            if (oldWidget != null) {
                ShellSerialTtyConnector oldConnector = oldWidget.getTtyConnector();
                if (oldConnector != null) {
                    oldConnector.close();
                }
                ShellTermWidgetUtil.close(oldWidget);
            }
            newClient = ShellClientUtil.newClient(connect);
            this.initClientListener(newClient);
            if (!newClient.isConnected()) {
                newClient.start();
            }
            if (!newClient.isConnected()) {
                throw new IllegalStateException(I18nHelper.connectFail()
                        + " errCode: " + newClient.getLastErrorCode()
                        + " errLocation: " + newClient.getLastErrorLocation());
            }
            newClient.write("\r");
            this.client = newClient;
            this.replaceWidget(oldWidget, newClient);
            ThreadUtil.start(this::initBackground);
        } catch (Throwable ex) {
            IOUtil.close(newClient);
            this.client = oldClient;
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 监听客户端状态。
     *
     * @param client 客户端
     */
    private void initClientListener(ShellSerialClient client) {
        client.addStateListener((observableValue, shellConnState, t1) -> {
            if (t1 == ShellConnState.INTERRUPTED) {
                MessageBox.warn("[" + client.connectName() + "] " + I18nHelper.connectSuspended(), this.getWindow());
            }
        });
    }

    /**
     * 在 JavaFX 线程创建并替换终端组件。
     *
     * @param oldWidget 旧终端组件
     * @param client    新客户端
     */
    private void replaceWidget(ShellSerialTermWidget oldWidget, ShellSerialClient client) {
        FXUtil.runWait(() -> {
            ShellSerialTermWidget replacement = new ShellSerialTermWidget();
            try {
                this.initWidget(replacement, client);
                ShellTermWidgetUtil.replace(oldWidget, replacement);
                this.widget = replacement;
            } catch (Throwable ex) {
                ShellTermWidgetUtil.close(replacement);
                throw new RuntimeException(ex);
            }
        });
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

    //    @Override
    //    public void destroy() {
    //        this.widget.destroy();
    //        super.destroy();
    //    }
}
